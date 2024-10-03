package com.example.JDDB.core.connection.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Chunk {
    private final String messageId;
    private final List<String> rows = new ArrayList<>();

    public Chunk(String messageId, String content) {
        this.messageId = messageId;

        rows.addAll(
                Arrays.asList(
                        content.trim().split("\n")
                )
        );
    }

    public String getMessageId(){
        return messageId;
    }

    public long getMessageIdLong(){
        return Long.parseLong(messageId);
    }

    public List<String> getRows(){
        return rows;
    }
}
