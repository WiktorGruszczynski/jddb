package com.example.JDDB.core;


import com.example.JDDB.utils.PropertyManager;
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
        if (!isCategory(TABLES)){
            guild.createCategory(TABLES).queue();
        }
    }

    public static Guild getGuild(){
        return guild;
    }

    public static Category getCategoryByName(String name){
        for (Category category: categories){
            if (category.getName().equals(name)){
                return category;
            }
        }

        return null;
    }

    public static TextChannel getTextChannel(Category category, String name){
        for (TextChannel textChannel: textChannels){
            if (textChannel.getParentCategory() != null
                    && textChannel.getParentCategory().equals(category)
                    && textChannel.getName().equals(name)){
                return textChannel;
            }
        }

        return null;
    }
}