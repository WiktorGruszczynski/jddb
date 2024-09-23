package com.example.JDDB.lib.core.repository;

import com.example.JDDB.lib.annotations.Table;
import com.example.JDDB.lib.core.DbConnection;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Field;

public class RepositoryInitializer<T> {
    protected final String TABLE_NAME;
    protected final DbConnection<T> dbConnection;

    public RepositoryInitializer(Class<T> clazz){
        dbConnection = new DbConnection<>(clazz);

        if (clazz.isAnnotationPresent(Table.class)){
            TABLE_NAME = clazz
                    .getAnnotation(Table.class)
                    .name();
        }
        else {
            TABLE_NAME = convertToSqlName(
                    clazz.getSimpleName()
            );
        }

        if (isPrimaryKeyPresent(clazz)){
            initTable();
        }
        else {
            throw new RuntimeException("Object missing @Id annotation");
        }

    }

    private boolean isPrimaryKeyPresent(Class<T> clazz){
        Field[] fields = clazz.getDeclaredFields();

        for (Field field: fields){
            if (field.isAnnotationPresent(Id.class)){
                return true;
            }
        }

        return false;
    }

    private void initTable(){
        if (!dbConnection.tableExists(TABLE_NAME)){
            dbConnection.createTable(TABLE_NAME);
        }
    }


    private String convertToSqlName(String className){
        StringBuilder buffer = new StringBuilder();

        for (int i=0; i<className.length(); i++){
            char chr = className.charAt(i);

            if (i==0){
                buffer.append(
                        Character.toLowerCase(chr)
                );
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

}
