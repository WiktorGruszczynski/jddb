package com.example.JDDB.core.connection;

import com.example.JDDB.core.*;
import com.example.JDDB.core.query.Parser;
import com.example.JDDB.core.query.Tokenizer;
import com.example.JDDB.utils.Generator;
import com.example.JDDB.utils.UrlReader;
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
    protected final UrlReader urlReader;
    protected final Cache<T> cache;


    public ConnectionInitializer(Class<?> entityType){
        this.entityType = entityType;
        this.entityManager = new EntityManager<>(entityType);
        this.codec = new Codec<>(entityType);
        this.guild = DiscordBot.getGuild();
        this.generator = initGenerator();
        this.tableChannel = initTableChannel();
        this.urlReader = new UrlReader();
        this.cache = new Cache<>(entityType);
    }

    private Generator initGenerator(){
        return new Generator(
                entityManager.getGeneratorType()
        );
    }

    private TextChannel initTableChannel(){
        return initTextChannel("tables", entityManager.getTableName());
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
