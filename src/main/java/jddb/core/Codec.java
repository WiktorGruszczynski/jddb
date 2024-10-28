package jddb.core;

import jddb.data.annotations.Column;
import jddb.data.annotations.Id;


import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

public class Codec<T> {
    private final Class<?> type;
    private final EntityManager<T> entityManager;
    private final char SEPARATOR = '$';
    private final int fieldsCount;


    public Codec(Class<?> type){
        this.type = type;
        this.entityManager = new EntityManager<>(type);
        this.fieldsCount = getFieldsCount();
    }

    private int getFieldsCount(){
        return type.getDeclaredFields().length;
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


    public List<String> getTokens(@NotNull String content) {
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
                    buffer.setLength(0);
                }
            }
            else {
                if (chr == '$'){
                    if (bufferSize == 0){
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

    public String encode(T entity){
        StringBuilder buffer = new StringBuilder();

        for (Field field: entity.getClass().getDeclaredFields()){
            try {
                field.setAccessible(true);
                Object value = field.get(entity);

                if (field.isAnnotationPresent(Id.class)){
                    if (value instanceof String strValue){
                        buffer
                                .append(strValue.length())
                                .append(SEPARATOR)
                                .append(strValue);
                    }
                    else {
                        throw new RuntimeException("Invalid id type");
                    }
                }
                else {
                    if (value == null || (String.valueOf(value).isEmpty())){
                        if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).nullale()){
                            throw new RuntimeException("Nullable Field with NULL value");
                        }
                        else {
                            buffer.append(SEPARATOR);
                        }
                    }
                    else {
                        String strValue = getStringValueOfObject(value);
                        buffer
                                .append(strValue.length())
                                .append(SEPARATOR)
                                .append(strValue);
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        buffer.append("\n");

        return buffer.toString();
    }



    public T decode(long messageId, @NotNull String data){
        List<String> tokens = getTokens(data);

        T entity = entityManager.newEntity();
        Field[] fields = type.getDeclaredFields();

        for (int i=0; i<fields.length; i++){
            Field field = fields[i];
            field.setAccessible(true);

            try {
                if (field.isAnnotationPresent(Id.class)){
                    String msgHexId = Long.toHexString(
                            messageId
                    );
                        entityManager.injectField(entity, field, msgHexId+ "." +tokens.get(i));
                }
                else {
                    entityManager.injectField(entity, field, tokens.get(i));
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }

        return entity;
    }
}