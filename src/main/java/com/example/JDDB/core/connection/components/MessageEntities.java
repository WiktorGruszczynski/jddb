package com.example.JDDB.core.connection.components;

import java.util.ArrayList;
import java.util.List;

public class MessageEntities<T>{
    private final String messageId;
    private final List<T> entities = new ArrayList<>();

    public MessageEntities(String messageId){
        this.messageId = messageId;
    }

    public String getMessageId(){
        return messageId;
    }

    public void add(T entity){
        entities.add(entity);
    }

    public List<T> getEntities(){
        return entities;
    }

    public boolean has(T entity){
        return entities.contains(entity);
    }

}
