package com.example.JDDB.lib.core;

import com.example.JDDB.lib.tools.PropertyManager;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.requests.GatewayIntent;


import java.util.ArrayList;
import java.util.List;


public class DiscordBot {
    private static String token;
    private static Long guildId;
    private final static String TABLES = "tables";

    private static Guild guild;
    private final static List<Category> categories = new ArrayList<>();
    private final static List<TextChannel> textChannels = new ArrayList<>();

    static {
        PropertyManager propertyManager = new PropertyManager();

        token = propertyManager.get("discord.token");
        guildId = propertyManager.getLong("discord.guild_id");

        try {
            connect();
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }


    public static void connect() throws InterruptedException {
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


        if (!isCategory(TABLES)){
            createCategory(TABLES);
        }
    }




    public static boolean isCategory(String categoryName){
        for (Category category: categories){
            if (category.getName().equals(categoryName)){
                return true;
            }
        }

        return false;
    }

    public static Category getCategory(String categoryName){
        for (Category category: categories){
            if (category.getName().equals(categoryName)){
                return category;
            }
        }

        return null;
    }

    public static Category createCategory(String categoryName){
        Category category = guild.createCategory(categoryName).complete();

        categories.add(category);

        return category;
    }


    public static boolean isTextChannel(Category category, String channelName){
        for (TextChannel textChannel: textChannels){
            String name = textChannel.getName();
            Category parentCategory = textChannel.getParentCategory();
            if (name.equals(channelName) && parentCategory!=null && parentCategory.equals(category)){
                return true;
            }
        }

        return false;
    }

    public static TextChannel createTextChannel(Category category, String channelName){
        TextChannel textChannel = category.createTextChannel(channelName).complete();

        textChannels.add(textChannel);

        return textChannel;
    }

    public static TextChannel getTextChannel(Category category, String channelName){
        for (TextChannel textChannel: textChannels){
            String name = textChannel.getName();
            Category parentCategory = textChannel.getParentCategory();

            if (name.equals(channelName) && parentCategory!=null && parentCategory.equals(category)){
                return textChannel;
            }
        }

        return null;
    }

    public static Message getMessageById(@Nonnull String id, @Nonnull TextChannel textChannel){
        return textChannel.retrieveMessageById(id).complete();
    }

    public static Message sendPlainMessage(TextChannel textChannel, String text){
        return textChannel.sendMessage(text).complete();
    }

    public static <T> Iterable<T> getAllMessages() {
        return null;
    }
}