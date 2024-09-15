package com.example.JDDB.lib.repository;


import com.example.JDDB.lib.Database;
import com.example.JDDB.lib.annotations.Table;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DiscordRepository<ID, T> implements DiscordRepositoryMethods<ID, T>{
    private final Database database;

    public DiscordRepository(){
        database = new Database();
    }

    public Iterable<T> findAll(){
        return null;
    }

    public Optional<T> findById(ID id){
        return Optional.empty();
    }

    @Override
    public void deleteById(ID id) {
        return;
    }

    private String formatSimpleName(String name){
        StringBuilder buffer = new StringBuilder();

        for (int i=0; i<name.length(); i++){
            char character = name.charAt(i);

            if (i == 0) {
                buffer.append(
                        String.valueOf(character).toLowerCase()
                );

                continue;
            }

            if (Character.isUpperCase(character)){
                buffer.append(
                        "_"+String.valueOf(character).toLowerCase()
                );
            }
            else {
                buffer.append(character);
            }
        }

        return buffer.toString();
    }


    private String getTableName(T entity) {
        Class<?> clazz = entity.getClass();



        if (clazz.isAnnotationPresent(Table.class)) {
            Table annotation = clazz.getAnnotation(Table.class);

            return annotation.name();
        }
        else{
            return formatSimpleName(
                    clazz.getSimpleName()
            );
        }
    }


    @Override
    public T save(T entity) {
        String tableName = getTableName(entity);

        if (!database.tableExists(tableName)){
            database.createTable(tableName);
        }


        return null;
    }

    @Override
    public Iterable<T> saveAll(Iterable<T> entities) {
        return List.of();
    }


}
