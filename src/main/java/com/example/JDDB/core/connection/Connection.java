package com.example.JDDB.core.connection;


import com.example.JDDB.core.Chunk;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class Connection<T> extends ConnectionInitializer<T>{
    private final Integer MAX_CHUNK_SIZE = 8192;
    private Message latestChunk;
    private long counterValue;
    private List<Message> allMessages;

    public Connection(Class<?> entityType) {
        super(entityType);

        latestChunk = getLatestChunk(tableChannel);
        counterValue = getCounterValue();
        allMessages = getAllMessages();
    }

    private List<Message> getAllMessages(){
        try {
            return tableChannel.getIterableHistory().takeAsync(1000).get();
        } catch (InterruptedException | ExecutionException e) {
            return new ArrayList<>();
        }
    }

    private void updateAllMessages(){
        new Thread(() -> {
            allMessages = getAllMessages();
        }).start();
    }

    // Counting methods
    private void updateCounter(int difference){
        Message message = getLatestChunk(counterChannel);

        if (message==null){
            counterChannel.sendMessage(String.valueOf(difference)).queue();
        }
        else {
            String content = message.getContentRaw();
            long currentValue = Long.parseLong(content);

            currentValue+=difference;

            message.editMessage(Long.toString(currentValue)).queue();
        }

        counterValue = counterValue + difference;
    }

    private void resetCounter(){
        Message message = getLatestChunk(counterChannel);

        if (message == null){
            counterChannel.sendMessage("0").queue();
        }
        else {
            message.editMessage("0").queue();
        }

        counterValue = 0;
    }

    private long getCounterValue(){
        Message message = getLatestChunk(counterChannel);

        if (message == null){
            return 0;
        }
        else {
            return Long.parseLong(message.getContentRaw());
        }
    }

    private void incrementCounter(int difference){
        updateCounter(difference);
    }

    private void decrementCounter(int difference){
        updateCounter(-difference);
    }


    // Saving data private methods
    private void updateLatestChunk(){
        new Thread(() -> {
            latestChunk = getLatestChunk(tableChannel);
        }).start();
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


        FileUpload fileUpload = createChunk(data + "\n" + inputData);

        message.editMessageAttachments(fileUpload).queue(p -> {
            updateLatestChunk();
        });
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

            if (i<entities.size()-1){
                buffer.append("\n");
            }

            if (buffer.toString().length() > MAX_CHUNK_SIZE){
                buffer.append("\n");

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
        StringBuilder buffer = new StringBuilder(chunkContent+"\n");
        List<T> savedEntities = new ArrayList<>();

        int start = 0;

        for (int i=0; i<entities.size(); i++){
            T entity = entities.get(i);

            buffer.append(codec.encode(entity));

            if (i<entities.size()-1){
                buffer.append("\n");
            }

            if (buffer.toString().length() > MAX_CHUNK_SIZE){
                buffer.append("\n");

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
        incrementCounter(1);
        updateAllMessages();
        return save(entity, latestChunk);
    }

    public List<T> saveAll(List<T> entities) {
        incrementCounter(entities.size());
        updateAllMessages();
        return saveAll(entities, latestChunk);
    }

    public Optional<T> findEntityById(String id) {
        String[] parts = id.split("\\.");

        if (parts.length!=2){
            return Optional.empty();
        }

        String msgHexId = parts[0];
        String msgId =  String.valueOf(Long.parseLong(msgHexId, 16));
        Message message;

        try {
            message = tableChannel.retrieveMessageById(msgId).complete();
        }
        catch (Exception e){
            return Optional.empty();
        }


        if (message == null){
            return Optional.empty();
        }

        String attachmentUrl = message.getAttachments().get(0).getUrl();

        String encodedEntities = urlReader.fetch(attachmentUrl);

        for (String encodedEntity: encodedEntities.split("\n")){
            T entity = codec.decode(msgId, encodedEntity);

            if (entityManager.getPrimaryKey(entity).equals(id)){
                return Optional.of(entity);
            }
        }

        return Optional.empty();
    }

    public boolean existsById(String id){
        return findEntityById(id).isPresent();
    }


    public void deleteAll(){
        TextChannel copy = tableChannel.createCopy().complete();

        tableChannel.delete().queue(r -> {
            tableChannel = copy;
            latestChunk = null;
            resetCounter();
            updateAllMessages();
        });
    }

    public long countEntities(){
        return counterValue;
    }

    public List<T> findAll() {
        List<Chunk> chunks = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        List<T> entities = new ArrayList<>();

        for (Message message: allMessages){
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

        for (Chunk chunk: chunks){
            for (String row: chunk.getRows()){
                T entity = codec.decode(chunk.getMessageId(), row);
                entities.add(entity);
            }
        }

        return entities;
    }

    public void deleteById(String id) {
    }

    public void delete(T entity) {
    }

    public void deleteAll(List<T> entities) {
    }
}
