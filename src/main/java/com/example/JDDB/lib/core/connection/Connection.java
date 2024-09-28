package com.example.JDDB.lib.core.connection;


import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class Connection<T> extends ConnectionInitializer<T>{
    private final Integer MAX_CHUNK_SIZE = 8192;
    private Message latestChunk;

    public Connection(Class<?> entityType) {
        super(entityType);

        latestChunk = getLatestChunk(tableChannel);
    }

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

    private void appendDataToChunk(Message message, String inputData){
        Message.Attachment attachment = message.getAttachments().get(0);
        String data = urlReader.fetch(attachment.getUrl());

        FileUpload fileUpload = createChunk(data + "\n" + inputData);
        message.editMessageAttachments(fileUpload)
                .queue(p -> updateLatestChunk());
    }


    private T saveNewChunk(T entity){
        // generate unique hexadecimal id
        String hexId = generator.generateHex();

        // set id
        entityManager.injectId(entity, hexId);

        // convert string to file
        FileUpload file = createChunk(codec.encode(entity));

        // send file
        Message message = tableChannel.sendFiles(file).complete();
        String msgHexId = Long.toHexString(message.getIdLong());

        // edit entity id
        entityManager.injectId(entity, msgHexId+"."+hexId);

        updateLatestChunk();


        return entity;
    }

    private T editExistingChunk(T entity, Message message){
        // generate unique hexadecimal id
        String hexId = generator.generateHex();
        String msgHexId = Long.toHexString(message.getIdLong());

        // set id
        entityManager.injectId(entity, hexId);

        String encodedEntity = codec.encode(entity);

        new Thread(
                () -> appendDataToChunk(message, encodedEntity)
        ).start();


        entityManager.injectId(entity, msgHexId+"."+hexId);


        return entity;
    }

    private T save(T entity, Message message){
        if (message==null){
            return saveNewChunk(entity);
        }

        if (message.getAttachments().get(0).getSize() <= MAX_CHUNK_SIZE ){
            return editExistingChunk(entity, message);
        }
        else {
            return save(entity, null);
        }


    }

    private void updateLatestChunk(){
        new Thread(() -> {
            latestChunk = getLatestChunk(tableChannel);
        }).start();
    }

    public T save(T entity){
        return save(entity, latestChunk);
    }

    public List<T> saveAll(List<T> entities) {
        return List.of(
                save(entities.get(0))
        );
    }

    public void deleteAll(){
        TextChannel copy = tableChannel.createCopy().complete();

        tableChannel.delete().queue(r -> {
            tableChannel = copy;
            latestChunk = getLatestChunk(tableChannel);
        });
    }
}
