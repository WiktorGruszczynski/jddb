package com.example.JDDB.lib;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class DiscordBot {
    @Value("${discord.token}")
    private String token;

    @Value("${discord.guild_id}")
    private Long guildId;

    private Guild guild;
    private List<Category> categories;
    private List<TextChannel> textChannels;

    @PostConstruct
    public void init() throws InterruptedException {
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

        categories = guild.getCategories();
        textChannels = guild.getTextChannels();
    }

    public Category getCategory(String name){
        for (Category category: categories){
            if (category.getName().equals(name)){
                return category;
            }
        }

        return null;
    }

    public boolean isCategory(String name){
        return getCategory(name)!=null;
    }

    public Category createCategory(String name){
        if (!isCategory(name)){
            return guild.createCategory(name).complete();
        }
        else{
            return getCategory(name);
        }
    }

    public boolean isTextChannel(Category category, String channelName){
        return getTextChannel(category, channelName) != null;
    }

    public TextChannel createTextChannel(Category category, String channelName){
        return category.createTextChannel(channelName).complete();
    }

    public TextChannel getTextChannel(Category category, String channelName){
        for (TextChannel textChannel: textChannels){
            if (textChannel.getParentCategory().equals(category) &&
                    textChannel.getName().equals(channelName)){
                return textChannel;
            }
        }

        return null;
    }

    public Message getMessageById(Long id, Long textChannelId){
        return guild.getTextChannelById(textChannelId)
                .getHistory()
                .getMessageById(id);
    }

    public void sendPlainMessage(TextChannel textChannel, String text){
        textChannel.sendMessage(text).complete();
    }
}