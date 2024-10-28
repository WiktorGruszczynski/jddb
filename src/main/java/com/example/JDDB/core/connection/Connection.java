package com.example.JDDB.core.connection;



import com.example.JDDB.core.connection.components.Chunk;
import com.example.JDDB.core.connection.components.MessageEntityIds;
import com.example.JDDB.core.query.Filter;
import com.example.JDDB.core.query.Query;
import com.example.JDDB.core.query.QueryManager;
import com.example.JDDB.core.query.Sorter;
import com.example.JDDB.data.enums.query.DML;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;


import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;



public class Connection<T> extends ConnectionInitializer<T>{
    private final Integer MAX_CHUNK_SIZE = 1024*64;
    private Message latestChunk;


    public Connection(Class<?> entityType) {
        super(entityType);

        latestChunk = getLatestChunk(tableChannel);

        loadCacheWithTimeMeasure();
    }


    private List<Message> getAllMessages(){
        try {
            return tableChannel.getIterableHistory().takeAsync(1000).get();
        } catch (InterruptedException | ExecutionException e) {
            return new ArrayList<>();
        }
    }

    public void loadCache(){
        List<Chunk> chunks = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (Message message: getAllMessages()){
            String url = message.getAttachments().get(0).getUrl();

            Thread thread = new Thread(() -> {
                String result = urlReader.fetch(url);
                chunks.add(
                        new Chunk(message.getId(), result.trim())
                );
            });

            thread.start();
            threads.add(thread);
        }

        threadManager.awaitThreadPool(threads);


        for (Chunk chunk: chunks){
            for (String row: chunk.getRows()){
                T entity = codec.decode(chunk.getMessageIdLong(), row);


                cache.add(entity);
            }
        }
    }

    public void loadCacheWithTimeMeasure(){
        long t0 = new Date().getTime();
        loadCache();
        long t1 = new Date().getTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        ZonedDateTime now = ZonedDateTime.now();
        String formattedDate = now.format(formatter);

        System.out.println(formattedDate + "  INFO : " + entityType.getName() + " - loaded cache in " + (t1-t0) + " ms");
    }


    // Modifying methods

    private void updateLatestChunk(){
        tableChannel.getHistory().retrievePast(1).queue(p -> {
            if (!p.isEmpty()){
                latestChunk = p.get(0);
            }
            else {
                latestChunk = null;
            }
        });
    }

    private Message getLatestChunk(TextChannel textChannel){
        List<Message> messageList = textChannel.getHistory().retrievePast(1).complete();

        if (!messageList.isEmpty()){
            return messageList.get(0);
        }
        else {
            return null;
        }
    }

    private FileUpload createChunk(String data){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                data.getBytes(StandardCharsets.UTF_8)
        );

        return FileUpload.fromData(inputStream, "chunk.txt");
    }

    private void appendDataToChunk(String inputData, Message message){
        Message.Attachment attachment = message.getAttachments().get(0);
        String data = urlReader.fetch(attachment.getUrl());

        FileUpload fileUpload = createChunk(data + inputData);

        message.editMessageAttachments(fileUpload).queue(p -> updateLatestChunk());
    }

    private void insertChunkDataWithoutUpdate(String data, Message message){
        FileUpload fileUpload = createChunk(data);
        message.editMessageAttachments(fileUpload).queue();
    }

    private void insertChunkData(String data, Message message){
        FileUpload fileUpload = createChunk(data);
        message.editMessageAttachments(fileUpload).queue(p -> updateLatestChunk());
    }

    private T saveNewChunk(T entity, String encodedEntity){
        // convert string to file
        FileUpload file = createChunk(encodedEntity);

        // send file
        Message message = tableChannel.sendFiles(file).complete();
        String msgHexId = Long.toHexString(message.getIdLong());

        // edit entity id
        entityManager.injectId(entity, msgHexId+"."+entityManager.getPrimaryKey(entity));

        updateLatestChunk();

        return entity;
    }

    private T editExistingChunk(T entity, String encodedEntity, Message message){
        String msgHexId = Long.toHexString(message.getIdLong());

        new Thread(
                () -> appendDataToChunk(encodedEntity, message)
        ).start();

        entityManager.injectId(entity, msgHexId+"."+entityManager.getPrimaryKey(entity));

        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return entity;
    }

    private List<T> saveNewChunk(List<T> entities, String encodedData){
        FileUpload file = createChunk(encodedData);
        Message message = tableChannel.sendFiles(file).complete();

        String msgHexId = Long.toHexString(message.getIdLong());

        for (T entity: entities){
            entityManager.injectId(
                    entity,
                    msgHexId + "." + entityManager.getPrimaryKey(entity)
            );
        }

        return entities;
    }

    private List<T> saveNewChunks(List<T> entities){
        List<T> savedEntities = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        int start = 0;

        for (int i=0; i<entities.size(); i++){
            T entity = entities.get(i);

            buffer.append(codec.encode(entity));


            if (buffer.toString().length() > MAX_CHUNK_SIZE){
                savedEntities.addAll(
                        saveNewChunk(entities.subList(start, i), buffer.toString())
                );

                buffer = new StringBuilder();
                start = i;
            }
        }

        if (start == 0){
            return saveNewChunk(entities, buffer.toString());
        }
        else {
            savedEntities.addAll(
                    saveNewChunk(entities.subList(start, entities.size()), buffer.toString())
            );
        }

        updateLatestChunk();

        return savedEntities;
    }

    private List<T> editExistingChunk(List<T> entities, String encodedData, Message message){
        FileUpload file = createChunk(encodedData);

        message = message.editMessageAttachments(file).complete();

        String msgHexId = Long.toHexString(message.getIdLong());

        for (T entity: entities){
            entityManager.injectId(
                    entity, msgHexId + "." + entityManager.getPrimaryKey(entity)
            );
        }

        return entities;
    }

    private List<T> editChunks(List<T> entities, Message message){
        String chunkContent = urlReader.fetch(message.getAttachments().get(0).getUrl());
        StringBuilder buffer = new StringBuilder(chunkContent);
        List<T> savedEntities = new ArrayList<>();

        int start = 0;

        for (int i=0; i<entities.size(); i++){
            T entity = entities.get(i);

            buffer.append(codec.encode(entity));



            if (buffer.toString().length() > MAX_CHUNK_SIZE){


                if (start == 0){
                    savedEntities.addAll(
                            editExistingChunk(entities.subList(0, i), buffer.toString(), message)
                    );
                }
                else {
                    savedEntities.addAll(
                            saveNewChunk(entities.subList(start, i), buffer.toString())
                    );
                }

                start = i;
                buffer = new StringBuilder();
            }
        }

        if (!buffer.isEmpty()){
            savedEntities.addAll(
                    saveNewChunk(entities.subList(start, entities.size()), buffer.toString())
            );
        }

        updateLatestChunk();

        return savedEntities;
    }

    private void removeEntitiesFromChunk(MessageEntityIds<T> group){
        long messageId = group.getMessageId();
        List<Long> idsToDelete = group.getEntityIds();


        tableChannel.retrieveMessageById(messageId).queue(message -> {
            String attachmentUrl = message.getAttachments().get(0).getUrl();
            String data = urlReader.fetch(attachmentUrl);
            StringBuilder buffer = new StringBuilder();

            String[] lines = data.split("\n");

            for (String line : lines){
                T decodedEntity = codec.decode(messageId, line);
                long entityChunkId = entityManager.getChunkId(decodedEntity);


                if (!idsToDelete.contains(entityChunkId)){
                    buffer
                            .append(line)
                            .append("\n");
                }
            }

            insertChunkDataWithoutUpdate(buffer.toString(), message);
        });
    }

    private void removeEntitiesFromChunks(List<String> ids){
        List<Long> messageIds = new ArrayList<>();
        List<MessageEntityIds<T>> messageEntitiesList = new ArrayList<>();

        for (String id: ids){
            String msgHexId = id.split("\\.")[0];
            String entityHexId = id.split("\\.")[1];
            long messageId = Long.parseLong(msgHexId, 16);
            long entityChunkId = Long.parseLong(entityHexId, 16);

            if (!messageIds.contains(messageId)){
                messageIds.add(messageId);
                messageEntitiesList.add(new MessageEntityIds<>(messageId));
            }

            for (MessageEntityIds<T> msgEntities: messageEntitiesList) {
                if (msgEntities.getMessageId() == messageId){
                    msgEntities.add(entityChunkId);
                }
            }
        }

        for (MessageEntityIds<T> msgEntities: messageEntitiesList){
            removeEntitiesFromChunk(msgEntities);
        }

        updateLatestChunk();
    }


    private T save(T entity, Message message){
        // generate unique hex value
        String hexId = generator.generateHex();

        //  set id
        entityManager.injectId(entity, hexId);

        String encodedEntity = codec.encode(entity);

        if (message==null){
            return saveNewChunk(entity, encodedEntity);
        }
        else {
            int contentSizeAfterUpload = message.getAttachments().get(0).getSize() + encodedEntity.length();

            if (contentSizeAfterUpload <= MAX_CHUNK_SIZE ){
                return editExistingChunk(entity, encodedEntity, message);
            }
            else {
                return save(entity, null);
            }
        }
    }

    private List<T> saveAll(List<T> entities, Message message){
        entityManager.injectIds(entities, generator);

        if (message == null){
            return saveNewChunks(entities);
        }
        else {
            return editChunks(entities, message);
        }
    }

    // public saving methods

    public T save(T entity){
        entity = save(entity, latestChunk);

        cache.add(entity);

        return entity;
    }

    public List<T> saveAll(List<T> entities) {
        entities = saveAll(entities, latestChunk);

        cache.addAll(entities);

        return entities;
    }

    // public finding methods

    public List<T> findAll() {
        return cache.getAll();
    }

    public List<T> findAllByIds(List<String> ids) {
        return cache.getAllByIds(ids);
    }

    public Optional<T> findEntityById(String id) {
        try {
             T entity = cache.getBy("id", id);

             if (entity==null){
                 return Optional.empty();
             }
             else {
                 return Optional.of(entity);
             }

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

//    Additional methods

    public boolean existsById(String id){
        return findEntityById(id).isPresent();
    }

    public long countEntities(){
        return cache.size();
    }

//    Delete methods

    public void deleteAll(){
        TextChannel copy = tableChannel.createCopy().complete();

        tableChannel.delete().queue(r -> {
            tableChannel = copy;
            latestChunk = null;
            cache.deleteAll();
        });
    }

    public void deleteById(String id) {
        new Thread(() -> {
            String msgHexId = id.split("\\.")[0];
            long msgId = Long.valueOf(msgHexId, 16);

            Message message = tableChannel.retrieveMessageById(msgId).complete();
            Message.Attachment attachment = message.getAttachments().get(0);

            String data = urlReader.fetch(attachment.getUrl());
            String[] lines = data.split("\n");

            StringBuilder buffer = new StringBuilder();
            boolean found = false;

            for (String line : lines) {
                T entity = codec.decode(msgId, line);
                if (!entityManager.getPrimaryKey(entity).equals(id)) {
                    buffer
                            .append(line)
                            .append("\n");
                } else {
                    found = true;
                }
            }

            if (!found) return;

            insertChunkData(buffer.toString(), message);

        }).start();

        cache.deleteOneById(id);
    }

    public void delete(T entity) {
        deleteById(
                entityManager.getPrimaryKey(entity)
        );
    }

    public void deleteAll(List<T> entities) {
        List<String> ids = new ArrayList<>();

        for (T entity: entities){
            ids.add(
                    entityManager.getPrimaryKey(entity)
            );
        }

        removeEntitiesFromChunks(ids);
        cache.deleteAllByIds(ids);
    }

    public void deleteAllByIds(List<String> ids) {
        removeEntitiesFromChunks(ids);
        cache.deleteAllByIds(ids);
    }


//    Custom Query section

    private QueryManager<T> getQueryManager(Query<T, ?> query){
        try {
            Field field = query.getClass().getDeclaredField("queryManager");
            field.setAccessible(true);

            return (QueryManager<T>) field.get(query);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public <R> List<R> executeQuery(Query<T, R> query) {
        List<Object> resultList = new ArrayList<>();
        QueryManager<T> queryManager = getQueryManager(query);

        DML dml = queryManager.getDml();
        String column = queryManager.getAffectedArea();
        Filter<T> filter = queryManager.getFilter();
        Sorter sorter = queryManager.getSorter();

        long offset = queryManager.getOffset();
        long limit = queryManager.getLimit();

        long counter = 0;

        filter.setEntityManager(entityManager);



        for (T entity: cache.getAll()){
            if (counter == limit) break;

            if (filter.matches(entity)){
                if (offset>0){
                    offset--;
                    continue;
                }

                counter++;


                if (column.equals("*")){
                    resultList.add(entity);
                }
                else {
                    resultList.add(
                            entityManager.getValueByColumnName(entity, column)
                    );
                }
            }
        }


        if (dml.equals(DML.SELECT)){
            return (List<R>) resultList;
        }
        if (dml.equals(DML.DELETE)){
            try {
                deleteAll((List<T>) resultList);
            }
            catch (ClassCastException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

        return null;
    }
}