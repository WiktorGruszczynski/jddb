package com.example.JDDB.lib.core.connection;

import com.example.JDDB.lib.core.Codec;
import com.example.JDDB.lib.core.DiscordBot;
import com.example.JDDB.lib.core.EntityManager;
import com.example.JDDB.lib.utils.Generator;
import com.example.JDDB.lib.utils.UrlReader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ConnectionInitializer<T>{
    protected final Class<?> entityType;
    protected final EntityManager<T> entityManager;
    protected final Codec<T> codec;
    protected final Guild guild;
    protected final Generator generator;
    protected TextChannel tableChannel;
    protected final TextChannel counterChannel;
    protected final UrlReader urlReader;


    public ConnectionInitializer(Class<?> entityType){
        this.entityType = entityType;
        this.entityManager = new EntityManager<>(entityType);
        this.codec = new Codec<>(entityType);
        this.guild = DiscordBot.getGuild();
        this.generator = initGenerator();
        this.tableChannel = initTableChannel();
        this.counterChannel = initCounterChannel();
        this.urlReader = new UrlReader();
    }

    private Generator initGenerator(){
        return new Generator(
                entityManager.getGeneratorType()
        );
    }

    private TextChannel initTableChannel(){
        return initTextChannel("tables", entityManager.getTableName());
    }

    private TextChannel initCounterChannel(){
        return initTextChannel("counters", entityManager.getTableName());
    }

    private TextChannel initTextChannel(String categoryName, String channelName){
        Category category = DiscordBot.getCategoryByName(categoryName);
        TextChannel textChannel = DiscordBot.getTextChannel(category, channelName);

        if (textChannel == null){
            return guild.createTextChannel(channelName, category).complete();
        }
        else {
            return textChannel;
        }
    }
}
