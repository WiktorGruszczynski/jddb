package com.example.JDDB.lib.core;

import com.example.JDDB.lib.annotations.Column;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


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
    private final Class<T> clazz;
    private Category tablesCategory;

    public DbConnection(Class<T> clazz) {
        this.clazz = clazz;
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

    private List<String> getTokens(String content){
        List<String> tokens = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        int bufferSize = 0;
        boolean readMode = false;

        for (char chr: content.toCharArray()){
            if (!readMode){
                if (chr == '$'){
                    readMode = true;
                }
                else {
                    bufferSize = bufferSize * 10 + chr -48;
                }
            }
            else {
                buffer.append(chr);
                bufferSize-=1;

                if (bufferSize == 0){
                    readMode = false;
                    tokens.add(buffer.toString());
                    buffer = new StringBuilder();
                }
            }
        }

        return tokens;
    }

    private T decodeEntity(Message message){
        try {
            int iterator = 0;
            List<String> tokens = getTokens(message.getContentRaw());
            Constructor<?> constructor = clazz.getConstructors()[0];
            String id = message.getId();

            T newEntity = (T) constructor.newInstance(new Object[constructor.getParameterCount()]);

            for (Field field: clazz.getDeclaredFields()){
                field.setAccessible(true);
                Class<?> type = field.getType();

                if (field.isAnnotationPresent(Id.class)){
                    if (type == Long.class){
                        field.set(newEntity, Long.valueOf(id));
                    }
                    else if (type == String.class){
                        field.set(newEntity, id);
                    }
                    else {
                        throw new RuntimeException("Id type must be either Long or String");
                    }
                }
                else{
                    String value = tokens.get(iterator);

                    if (type == Integer.class){
                        field.set(
                                newEntity, Integer.valueOf(value)
                        );
                    }
                    else if (type == String.class){
                        field.set(newEntity, tokens.get(iterator));
                    }

                    iterator++;
                }
            }

            return newEntity;
        }
        catch (Exception e){
            throw new RuntimeException();
        }
    }

    public Optional<T> findEntityById(String tableName, ID id){
        TextChannel textChannel = DiscordBot.getTextChannel(
                tablesCategory,
                tableName
        );

        if (textChannel == null){
            System.out.println("[Warning] textChannel not found");
            return Optional.empty();
        }

        Message message = DiscordBot.getMessageById(
                String.valueOf(id),
                textChannel
        );


        if (message == null){
            return Optional.empty();
        }

        return Optional.of(
                decodeEntity(message)
        );
    }

    public Iterable<T> findAllEntities(String tableName) {
        return DiscordBot.getAllMessages();
    }
}
