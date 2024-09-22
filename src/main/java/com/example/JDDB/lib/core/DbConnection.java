package com.example.JDDB.lib.core;

import com.example.JDDB.lib.annotations.Column;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;


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
    private final Category tablesCategory;


    public DbConnection(Class<T> clazz) {
        this.clazz = clazz;
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


    private boolean isAllowedType(Class<?> clazz){
        return allowedTypes.contains(clazz);
    }


    private boolean isAllowedType(Object value){
        return isAllowedType(value.getClass());
    }

    private String getStringValue(Object obj){
        if (obj instanceof Boolean value){
            if (value){
                return "1";
            }
            else {
                return "0";
            }
        }
        else if (obj instanceof Date date){
            return String.valueOf(
                    date.getTime()
            );
        }
        else {
            return String.valueOf(obj);
        }
    }

    private String generateMessageContent(T entity){
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

                    String value = getStringValue(obj);

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

    private void injectId(T entity, Field field, String id) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type == Long.class){
            field.set(entity, Long.valueOf(id));
        }
        else if (type == String.class){
            field.set(entity, id);
        }
        else {
            throw new RuntimeException("Id type must be either Long or String");
        }
    }

    private void injectField(T entity, Field field, String strValue) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (!isAllowedType(type)){
            throw new RuntimeException("Type not allowed");
        }

        if (type == String.class){
            field.set(entity, strValue);
        }
        else if (type == Character.class){
            field.set(entity, strValue.charAt(0));
        }
        else if (type == Boolean.class){
            field.set(entity, strValue.equals("1"));
        }
        else if (type == Byte.class){
            field.set(entity, Byte.valueOf(strValue));
        }
        else if (type == Short.class){
            field.set(entity, Short.valueOf(strValue));
        }
        else if (type == Integer.class){
            field.set(entity, Integer.valueOf(strValue));
        }
        else if (type == Long.class){
            field.set(entity, Long.valueOf(strValue));
        }
        else if (type == Float.class){
            field.set(entity, Float.valueOf(strValue));
        }
        else if (type == Double.class){
            field.set(entity, Double.valueOf(strValue));
        }
        else if (type == Date.class){
            field.set(entity, new Date(Long.parseLong(strValue)));
        }
    }

    private T decodeEntity(Message message){
        List<String> tokens = getTokens(message.getContentRaw());
        Constructor<?> constructor = clazz.getConstructors()[0];
        int iterator = 0;

        try {
            T newEntity = (T) constructor.newInstance(new Object[constructor.getParameterCount()]);

            for (Field field: clazz.getDeclaredFields()){
                field.setAccessible(true);

                if (field.isAnnotationPresent(Id.class)){
                    injectId(newEntity, field, message.getId());
                }
                else{
                    injectField(newEntity, field, tokens.get(iterator));
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

        Message message = DiscordBot.getMessageById(String.valueOf(id), textChannel);

        if (message == null){
            return Optional.empty();
        }

        return Optional.of(
                decodeEntity(message)
        );
    }

    public List<T> findAllEntities(String tableName) {
        TextChannel textChannel = DiscordBot.getTextChannel(tablesCategory, tableName);
        List<T> entities = new ArrayList<>();

        for (Message message: DiscordBot.getAllMessages(textChannel)){
            entities.add(
                    decodeEntity(message)
            );
        }

        return entities;
    }

    public boolean existsById(String tableName, ID id){
        return DiscordBot.getMessageById(
                String.valueOf(id),
                Objects.requireNonNull(
                        DiscordBot.getTextChannel(tablesCategory, tableName)
                )
        ) != null;
    }

    public void deleteById(String tableName, ID id) {
        TextChannel textChannel = DiscordBot.getTextChannel(tablesCategory, tableName);

        if (textChannel!=null){
            DiscordBot.deleteMessageById(textChannel, String.valueOf(id));
        }
    }
}
