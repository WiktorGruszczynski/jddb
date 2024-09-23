package com.example.JDDB.lib.core;


import com.example.JDDB.lib.core.codec.Codec;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Field;
import java.util.*;


public class DbConnection<T> {
    private final Class<T> type;
    private final Codec<T> codec;
    private final Category tablesCategory;


    public DbConnection(Class<T> type) {
        this.type = type;
        this.codec = new Codec<>(type);
        this.tablesCategory = DiscordBot.getCategory("tables");
    }


    public void createTable(String tableName){
        DiscordBot.createTextChannel(
                tablesCategory,
                tableName
        );
    }


    public boolean tableExists(String tableName){
        return DiscordBot.isTextChannel(
                DiscordBot.getCategory("tables"), tableName
        );
    }


    private T updateEntityId(Long id, T entity){
        for (Field field: entity.getClass().getDeclaredFields()){
            try {
                if (field.isAnnotationPresent(Id.class)){
                    field.setAccessible(true);

                    Class<?> type = field.getType();

                    if (type == Long.class){
                        field.set(entity, id);
                    }
                    else if (type == String.class){
                        field.set(entity, String.valueOf(id));
                    }
                    else {
                        throw new IllegalArgumentException("Id must be either Long or String");
                    }

                    break;
                }
            }
            catch (IllegalAccessException e){
                throw new RuntimeException(e);
            }
        }

        return entity;
    }


    public T saveEntity(String tableName, T entity){
        TextChannel textChannel = DiscordBot.getTextChannel(tablesCategory, tableName);
        Message message = DiscordBot.sendPlainMessage(textChannel, codec.encode(entity));

        return updateEntityId(message.getIdLong(), entity);
    }


    public List<T> saveEntities(String tableName, List<T> entities){
        List<Thread> queue = new ArrayList<>();
        T[] savedEntities = (T[]) new Object[entities.size()];

        for (int i=0; i<entities.size(); i++){
            T entity = entities.get(i);

            final int index = i;

            Thread thread = new Thread(
                    () -> savedEntities[index] = saveEntity(tableName, entity)
            );

            thread.start();

            queue.add(thread);
        }

        while (true){
            boolean finished = true;

            for (Thread thread: queue){
                if (thread.isAlive()){
                    finished = false;
                }
            }

            if (finished){
                break;
            }
        }

        return List.of(savedEntities);
    }


    public Optional<T> findEntityById(String tableName, String id){
        TextChannel textChannel = DiscordBot.getTextChannel(
                tablesCategory,
                tableName
        );

        if (textChannel == null){
            System.out.println("[Warning] textChannel not found");
            return Optional.empty();
        }

        Message message = DiscordBot.getMessageById(String.valueOf(id), textChannel);

        if (message == null){
            return Optional.empty();
        }

        return Optional.of(
                codec.decode(message)
        );
    }

    public List<T> findAllEntities(String tableName) {
        TextChannel textChannel = DiscordBot.getTextChannel(tablesCategory, tableName);
        List<T> entities = new ArrayList<>();

        for (Message message: DiscordBot.getAllMessages(textChannel)){
            entities.add(
                    codec.decode(message)
            );
        }

        return entities;
    }

    public boolean existsById(String tableName, String id){
        return DiscordBot.getMessageById(
                id,
                Objects.requireNonNull(
                        DiscordBot.getTextChannel(tablesCategory, tableName)
                )
        ) != null;
    }

    public void deleteById(String tableName, String id) {
        TextChannel textChannel = DiscordBot.getTextChannel(tablesCategory, tableName);

        if (textChannel!=null){
            DiscordBot.deleteMessageById(textChannel, id);
        }
    }
}
