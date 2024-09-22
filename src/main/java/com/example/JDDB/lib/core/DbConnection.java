package com.example.JDDB.lib.core;

import com.example.JDDB.lib.annotations.Column;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.data.annotation.Id;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;


public class DbConnection<T, ID> {
    private final List<Class<?>> allowedTypes = List.of(
            Boolean.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Character.class,
            String.class,
            Date.class
    );
    private Category tablesCategory;

    public DbConnection() {
        this.tablesCategory = DiscordBot.getCategory("tables");
    }


    public void createTable(String tableName){
        DiscordBot.createTextChannel(
                DiscordBot.getCategory("tables"),
                tableName
        );
    }

    public boolean tableExists(String tableName){
        return DiscordBot.isTextChannel(
                DiscordBot.getCategory("tables"), tableName
        );
    }

    private boolean isAllowedType(Object value){
        return allowedTypes.contains(
                value.getClass()
        );
    }

    private <E> String generateMessageContent(E entity){
        StringBuilder buffer = new StringBuilder();

        for (Field field: entity.getClass().getDeclaredFields()){
            if (field.isAnnotationPresent(Id.class)){
                continue;
            }

            try {
                field.setAccessible(true);
                Object obj = field.get(entity);

                if (obj == null){
                    if (field.isAnnotationPresent(Column.class)){
                        if (!field.getAnnotation(Column.class).nullale()){
                            throw new IllegalArgumentException("NULL value not allowed");
                        }
                    }

                    buffer.append("$");
                }
                else{
                    if (!isAllowedType(obj)){
                        throw new IllegalArgumentException();
                    }

                    String value = String.valueOf(obj);

                    buffer
                            .append(value.length())
                            .append("$")
                            .append(value);
                }

            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }

        return buffer.toString();
    }

    private <E> E updateEntityId(Long id, E entity){
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
        String msgContent = generateMessageContent(entity);

        TextChannel textChannel = DiscordBot.getTextChannel(tablesCategory, tableName);
        Message message = DiscordBot.sendPlainMessage(textChannel, msgContent);

        return updateEntityId(message.getIdLong(), entity);
    }

    public List<T> saveEntities(String tableName, List<T> entities){
        entities.replaceAll(e -> saveEntity(tableName, e));

        return entities;
    }

    public T findEntityById(String tableName, ID id){
        return null;
    }

}
