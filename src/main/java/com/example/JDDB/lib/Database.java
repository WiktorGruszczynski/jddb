package com.example.JDDB.lib;


import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Component;

@Component
public class Database {
    private final DiscordBot discordBot = new DiscordBot();


    public Guild getGuild(){
        return discordBot.getGuild();
    }

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
