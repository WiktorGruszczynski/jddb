package com.example.JDDB.lib.core;


import com.example.JDDB.lib.utils.PropertyManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;


public class DiscordBot {
    private static String token;
    private static Long guildId;

    private static Guild guild;
    private static List<Category> categories = new ArrayList<>();
    private static List<TextChannel> textChannels = new ArrayList<>();

    private static final String TABLES = "tables";
    private static final String COUNTERS = "counters";
    private static final String GENERATORS = "generators";

    static {
        PropertyManager propertyManager = new PropertyManager();

        token = propertyManager.get("discord.token");
        guildId = propertyManager.getLong("discord.guild_id");

        try {
            connect();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private static void connect() throws InterruptedException {
        JDA jda = JDABuilder
                .createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build()
                .awaitReady();


        guild = jda.getGuildById(guildId);

        if (guild == null){
            throw new RuntimeException(
                    "Failed to initialize guild with specified ID - " + guildId
            );
        }

        categories.addAll(guild.getCategories());
        textChannels.addAll(guild.getTextChannels());

        createCategories();
    }

    private static boolean isCategory(String categoryName){


        for (Category category: categories){
            if (category.getName().equals(categoryName)){
                return true;
            }
        }

        return false;
    }

    private static void createCategories(){
        if (!isCategory("tables")){
            guild.createCategory("tables").queue();
        }

        if (!isCategory("counters")){
            guild.createCategory("counters").queue();
        }

        if (!isCategory("generators")){
            guild.createCategory("generators").queue();
        }
    }

    public static Guild getGuild(){
        return guild;
    }
}