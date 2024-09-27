package com.example.JDDB.lib.core;

import com.example.JDDB.lib.annotations.GeneratedValue;
import com.example.JDDB.lib.annotations.Id;
import com.example.JDDB.lib.annotations.Table;
import com.example.JDDB.lib.data.DataType;
import com.example.JDDB.lib.data.GeneratorType;
import org.jetbrains.annotations.NotNull;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

public class EntityManager<T>{
    private final Class<?> entityType;
    private final GeneratorType generatorType;
    private final String tableName;


    public EntityManager(Class<?> entityType) {
        this.entityType = entityType;
        this.generatorType = initGeneratorType();
        this.tableName = initTableName();
    }

    @NotNull
    private String initTableName(){
        if (entityType.isAnnotationPresent(Table.class)){
            return entityType.getAnnotation(Table.class).name();
        }
        else {
            return getSqlName(entityType.getSimpleName());
        }
    }

    private String getSqlName(String name){
        StringBuilder buffer = new StringBuilder();

        for (int i=0; i<name.length(); i++){
            char chr = name.charAt(i);

            if (i==0){
                buffer.append(Character.toLowerCase(chr));
            }
            else {
                if (Character.isUpperCase(chr)){
                    buffer
                            .append("_")
                            .append(Character.toLowerCase(chr));
                }
                else {
                    buffer.append(chr);
                }
            }
        }

        return buffer.toString();
    }

    private GeneratorType initGeneratorType(){
        for (Field field: entityType.getDeclaredFields()){
            field.setAccessible(true);

            if (field.isAnnotationPresent(Id.class)){
                if (field.isAnnotationPresent(GeneratedValue.class)){
                    return field.getAnnotation(GeneratedValue.class).value();
                }
                else {
                    return GeneratorType.SEQUENCE;
                }
            }
        }

        throw new RuntimeException("Missing @Id annotation");
    }

    public String getTableName(){
        return tableName;
    }

    public GeneratorType getGeneratorType(){
        return generatorType;
    }

    public T newEntity(){
        Constructor<?> constructor = entityType.getConstructors()[0];
        int paramsCount = constructor.getParameterCount();

        try {
            return (T) constructor.newInstance(new Object[paramsCount]);
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isTypeAllowed(Class<?> type){
        return DataType.contains(type);
    }

    public void injectId(T entity, Field field, String id) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type == String.class){
            field.set(entity, id);
        }
        else {
            throw new RuntimeException("Id type must be type of String");
        }
    }

    public void injectId(T entity, String id){
        for (Field field: entityType.getDeclaredFields()){
            if (field.isAnnotationPresent(Id.class)){
                field.setAccessible(true);

                try {
                    field.set(entity, id);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void injectField(T entity, Field field, String strValue) throws IllegalAccessException {
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

    public long getPrimaryKey(T entity) {
        for (Field field: entityType.getDeclaredFields()){
            if (field.isAnnotationPresent(Id.class)){
                field.setAccessible(true);
                try {
                    return Long.parseLong(String.valueOf(field.get(entity)));

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        throw new RuntimeException("Primary key missing");
    }

    public String getHexPrimaryKey(T entity){
        return Long.toHexString(
                getPrimaryKey(entity)
        );
    }
}
