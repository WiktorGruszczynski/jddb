package com.example.JDDB.lib.core.codec;

import com.example.JDDB.lib.annotations.Column;
import com.example.JDDB.lib.utils.AllowedType;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Codec<T> {
    private final Class<T> type;

    public Codec(Class<T> type) {
        this.type = type;
    }

    private boolean isTypeAllowed(Class<?> type){
        return AllowedType.containsType(type);
    }

    private boolean isTypeAllowed(Object obj) {
        return isTypeAllowed(obj.getClass());
    }

    private String getStringValueOfObject(Object obj){
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

    private List<String> getTokens(@NotNull String content) {
        List<String> tokens = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        int bufferSize = 0;
        int offset = 48;
        boolean readMode = false;

       for (char chr: content.toCharArray()){
           if (readMode){
               buffer.append(chr);
               bufferSize-=1;

               if (bufferSize == 0) {
                   readMode = false;
                   tokens.add(buffer.toString());
                   buffer = new StringBuilder();
               }
           }
           else {
               if (chr == '$'){
                   if (bufferSize==0){
                       tokens.add("");
                   }
                   else {
                       readMode = true;
                   }
               }
               else {
                   bufferSize = bufferSize * 10 + chr - offset;
               }
           }
       }

        return tokens;
    }

    private T newEntity(){
        Constructor<?> constructor = type.getConstructors()[0];
        int paramsCount = constructor.getParameterCount();

        try {
            return (T) constructor.newInstance(new Object[paramsCount]);
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void injectId(T entity, Field field, String id) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type == String.class){
            field.set(entity, id);
        }
        else {
            throw new RuntimeException("Id type must be type of String");
        }
    }

    private void injectField(T entity, Field field, String strValue) throws IllegalAccessException {
        Class<?> type = field.getType();
        boolean nullValue = strValue.isEmpty();

        if (!isTypeAllowed(type)){
            throw new RuntimeException("Type not allowed - " + type.getSimpleName());
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
            if (nullValue){
                field.set(entity, null);
            }
            else {
                field.set(entity, new Date(Long.parseLong(strValue)));
            }
        }
    }


    public String encode(T entity){
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
                    if (!isTypeAllowed(obj)){
                        throw new IllegalArgumentException();
                    }

                    String value = getStringValueOfObject(obj);

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

    public T decode(@NotNull String id, @NotNull String data){
        List<String> tokens = getTokens(data);
        T entity = newEntity();
        int iterator = 0;

        for (Field field: type.getDeclaredFields()){
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(Id.class)){
                    injectId(entity, field, id);
                }
                else {
                    injectField(entity, field, tokens.get(iterator));
                    iterator++;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return entity;
    }

    public T decode(Message message){
        return decode(
                message.getId(), message.getContentRaw()
        );
    }

}