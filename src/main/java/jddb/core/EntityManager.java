package jddb.core;

import jddb.data.annotations.Column;
import jddb.data.annotations.GeneratedValue;
import jddb.data.annotations.Id;
import jddb.data.annotations.Table;
import jddb.data.enums.GeneratorType;
import jddb.utils.Generator;
import org.jetbrains.annotations.NotNull;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EntityManager<T>{
    private final Class<?> entityType;
    private final GeneratorType generatorType;
    private final String tableName;
    private final HashMap<String, Field> columnsMap;


    public EntityManager(Class<?> entityType) {
        this.entityType = entityType;
        this.generatorType = initGeneratorType();
        this.tableName = initTableName();
        this.columnsMap = initColumns();
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

    private HashMap<String, Field> initColumns(){
        HashMap<String, Field> columns = new HashMap<>();

        for (Field field : entityType.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)){
                columns.put(field.getAnnotation(Column.class).name(), field);
            }
            else {
                columns.put(field.getName(), field);
            }
        }

        return columns;
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

    private Object[] generateConstructorParams(Constructor<?> constructor){
        int paramsCount = constructor.getParameterCount();
        Object[] params = new Object[paramsCount];
        Class<?>[] types = constructor.getParameterTypes();

        for (int i=0; i<paramsCount; i++){
            Class<?> type = types[i];

            if (type.isPrimitive()){
                if (type==boolean.class){
                    params[i] = false;
                }
                else {
                    params[i] = 0;
                }
            }
            else {
                params[i] = null;
            }
        }

        return params;
    }

    public T newEntity(){
        Constructor<?> constructor = entityType.getConstructors()[0];

        try {
            return (T) constructor.newInstance(
                    generateConstructorParams(constructor)
            );
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
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

    public void injectIds(List<T> entities, Generator generator){
        for (T entity: entities){
            injectId(entity, generator.generateHex());
        }
    }

    public void injectField(T entity, Field field, String strValue) throws IllegalAccessException {
        Class<?> type = field.getType();


        if (type == String.class){
            field.set(entity, strValue);
        }
        else if (type == Character.class || type == char.class){
            field.set(entity, strValue.charAt(0));
        }
        else if (type == Boolean.class || type == boolean.class){
            field.set(entity, strValue.equals("1"));
        }
        else if (type == Byte.class || type == byte.class){
            field.set(entity, Byte.valueOf(strValue));
        }
        else if (type == Short.class || type == short.class){
            field.set(entity, Short.valueOf(strValue));
        }
        else if (type == Integer.class || type == int.class){
            field.set(entity, Integer.valueOf(strValue));
        }
        else if (type == Long.class || type == long.class){
            field.set(entity, Long.valueOf(strValue));
        }
        else if (type == Float.class || type == float.class){
            field.set(entity, Float.valueOf(strValue));
        }
        else if (type == Double.class || type == double.class){
            field.set(entity, Double.valueOf(strValue));
        }
        else if (type == Date.class){
            if (strValue.isEmpty()){
                field.set(entity, null);
            }
            else {
                field.set(entity, new Date(
                        Long.parseLong(strValue))
                );
            }
        }
        else {
            throw new RuntimeException("Type not allowed - " + type.getSimpleName());
        }
    }

    private Field getIdField(T entity){
        for (Field field: entityType.getDeclaredFields()){
            if (field.isAnnotationPresent(Id.class)){
                field.setAccessible(true);

                return field;
            }
        }

        throw new RuntimeException("Missing @Id annotation");
    }


    // primaryKey = hexMessageId + "." + hexChunkId
    public String getPrimaryKey(T entity) {
        Field idField = getIdField(entity);

        try {
            return (String) idField.get(entity);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public long getChunkId(T entity) {
        String primaryKey = getPrimaryKey(entity);
        String hexChunkId = primaryKey.split("\\.")[1];

        return Long.parseLong(hexChunkId, 16);
    }

    public String getColumnName(Field field){
        if (field.isAnnotationPresent(Column.class)){
            return field.getAnnotation(Column.class).name();
        }
        else {
            return field.getName();
        }
    }

    public Object getValueByColumnName(Object entity, String columnName) {
        Field column = columnsMap.get(columnName);

        if (column != null){
            try {
                column.setAccessible(true);

                return column.get(entity);

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException(
                new NoSuchFieldException()
        );
    }

    public boolean isColumnNullable(Field field){
        if (isPrimaryKey(field)){
            return false;
        }
        else {
            if (field.isAnnotationPresent(Column.class)){
                return field.getAnnotation(Column.class).nullale();
            }
            else {
                return true;
            }
        }
    }

    public boolean isPrimaryKey(Field field) {
        return field.isAnnotationPresent(Id.class);
    }
}