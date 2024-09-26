package com.example.JDDB.lib.core;

import net.dv8tion.jda.api.entities.Guild;

public class Connection<T, ID>{
    private final Class<?> entityType;
    private final Class<?> idType;
    private final Guild guild;


    public Connection(Class<?> entityType, Class<?> idType) {
        this.entityType = entityType;
        this.idType = idType;
        this.guild = DiscordBot.getGuild();
    }
}
