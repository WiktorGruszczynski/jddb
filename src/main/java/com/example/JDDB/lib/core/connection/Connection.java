package com.example.JDDB.lib.core.connection;


import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;


public class Connection<T> extends ConnectionInitializer<T>{
    public Connection(Class<?> entityType) {
        super(entityType);
    }

    private Message findLastChunkMessage(){
        List<Message> messageList;

        try {
            messageList = tableChannel.getIterableHistory().takeAsync(1).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (!messageList.isEmpty()) {
            Message message = messageList.get(0);

            if (message.getAuthor().isBot()) return message;
        }

        return null;
    }

    private FileUpload createAttachment(String data){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                data.getBytes(StandardCharsets.UTF_8)
        );

        return FileUpload.fromData(inputStream, "chunk.txt");
    }

    private void appendDataToAttachment(Message message, String encodedEntity){
        Message.Attachment attachment = message.getAttachments().get(0);
        String data;

        try {
            data = urlReader.fetch(attachment.getUrl());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        FileUpload fileUpload = createAttachment(data+encodedEntity);
        message.editMessageAttachments(fileUpload).queue();
    }

    public Optional<T> findById(String id){
        return Optional.empty();
    }

    public T save(T entity){
        String entityHexId = entityManager.getHexPrimaryKey(entity);
        entityManager.injectId(entity, entityHexId);

        String encodedEntity = codec.encode(entity);
        Message message = findLastChunkMessage();

        if (message==null){
            FileUpload attachment = createAttachment(encodedEntity);
            message = tableChannel.sendFiles(attachment).complete();
        }
        else {
            final Message messageCopy = message;

            new Thread(() -> appendDataToAttachment(messageCopy, encodedEntity)).start();
        }

        String hexMsgId = Long.toHexString(message.getIdLong());
        entityManager.injectId(entity, hexMsgId + "." + entityHexId);

        return entity;
    }
}
