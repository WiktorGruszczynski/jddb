package com.example.JDDB.core.connection;


import com.example.JDDB.core.connection.components.Chunk;
import com.example.JDDB.core.connection.components.MessageEntities;
import com.example.JDDB.core.query.Filter;
import com.example.JDDB.core.query.Query;
import com.example.JDDB.core.query.QueryManager;
import com.example.JDDB.data.exceptions.NoPrimaryKeyException;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
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

        awaitThreadPool(threads);


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
        System.out.println(entityType.getName() + " - loaded cache in " + (t1-t0) + " ms");
    }


    // Saving data private methods
    private void updateLatestChunk(){
        new Thread(() -> latestChunk = getLatestChunk(tableChannel)).start();
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

    private void insertChunkData(String data, Message message){
        FileUpload fileUpload = createChunk(data );
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

    private List<T> saveAll(List<T> entities, Message message){
        entityManager.injectIds(entities, generator);

        if (message == null){
            return saveNewChunks(entities);
        }
        else {
            return editChunks(entities, message);
        }
    }

    // public methods

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

    public List<T> findAll() {
        return cache.getAll();
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

    public boolean existsById(String id){
        return findEntityById(id).isPresent();
    }


    public void deleteAll(){
        TextChannel copy = tableChannel.createCopy().complete();

        tableChannel.delete().queue(r -> {
            tableChannel = copy;
            latestChunk = null;
            cache.deleteAll();
        });
    }

    public long countEntities(){
        return cache.size();
    }

    private void awaitThreadPool(Iterable<Thread> threads){
        while (true){
            boolean runningThread = false;

            for (Thread thread: threads){
                if (thread.isAlive()){
                    runningThread = true;
                    break;
                }
            }

            if (!runningThread) break;
        }
    }


    private void removeEntitiesFromChunk(MessageEntities<T> group){
        String msgHexId = group.getMessageId();
        long messageId = Long.parseLong(msgHexId, 16);


        tableChannel.retrieveMessageById(messageId).queue(message -> {
            StringBuilder buffer = new StringBuilder();

            Message.Attachment attachment = message.getAttachments().get(0);
            String data = urlReader.fetch(attachment.getUrl());

            for (String line: data.split("\n")){
                T decodedEntity = codec.decode(messageId, line);

                if (!group.has(decodedEntity)){
                    buffer
                            .append(line)
                            .append("\n");
                }
            }

            insertChunkData(buffer.toString(), message);
            updateLatestChunk();
        });
    }

    private void removeEntitiesFromChunks(List<T> entities){
        List<String> registeredMessageIds = new ArrayList<>();
        List<MessageEntities<T>> groups = new ArrayList<>();

        for (T entity: entities){
            String id = entityManager.getPrimaryKey(entity);
            String msgHexId = id.split("\\.")[0];

            if (!registeredMessageIds.contains(msgHexId)){
                registeredMessageIds.add(msgHexId);
                groups.add(new MessageEntities<>(msgHexId));
            }

            for (MessageEntities<T> group: groups){
                if (group.getMessageId().equals(msgHexId)){
                    group.add(entity);
                    break;
                }
            }
        }

        for (MessageEntities<T> group: groups){
            removeEntitiesFromChunk(group);
        }
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

        try {
            cache.deleteOneBy("id", id);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(
                    new NoPrimaryKeyException()
            );
        }
    }

    public void delete(T entity) {
        deleteById(
                entityManager.getPrimaryKey(entity)
        );
    }

    public void deleteAll(List<T> entities) {
        removeEntitiesFromChunks(entities);
        cache.deleteAll(entities);
    }

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
        List<R> resultList = new ArrayList<>();

        QueryManager<T> queryManager = getQueryManager(query);
        String column = queryManager.getAffectedArea();
        Filter<T> filter = queryManager.getFilter();

        filter.setEntityManager(entityManager);

        for (T entity: cache.getAll()){
            if (filter.matches(entity)){
                if (column.equals("*")){
                    resultList.add((R) entity);
                }
                else {
                    resultList.add(
                            entityManager.getValueByColumnName(entity, column)
                    );
                }
            }
        }

        return resultList;
    }
}