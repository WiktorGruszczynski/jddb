package com.example.JDDB.lib;


public class Database {
    private final DiscordBot discordBot = new DiscordBot();


    public void createTable(String tableName){
        discordBot.createTextChannel(
                discordBot.getCategory("tables"),
                tableName
        );
    }

    public boolean tableExists(String tableName){
        return discordBot.isTextChannel(
                discordBot.getCategory("tables"), tableName
        );
    }

    public void insertEntity(Object entity){
//        discordBot.sendPlainMessage();
    }
}
