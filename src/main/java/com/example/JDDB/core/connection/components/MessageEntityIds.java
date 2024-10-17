package com.example.JDDB.core.connection.components;

import java.util.ArrayList;
import java.util.List;

public class MessageEntityIds<T>{
    private final long messageId;
    private final List<Long> entityIds = new ArrayList<>();

    public MessageEntityIds(long messageId){
        this.messageId = messageId;
    }

    public long getMessageId(){
        return messageId;
    }

    public void add(long entityId){
        entityIds.add(entityId);
    }

    public List<Long> getEntityIds(){
        return entityIds;
    }
}
