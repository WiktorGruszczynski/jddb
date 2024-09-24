package com.example.JDDB.lib.core.repository;


import com.example.JDDB.lib.core.database.Connection;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public class RepositoryInitializer<T> {
    protected Connection<T> connection;

    public RepositoryInitializer(){
        this.connection = new Connection<>(
                (Class<T>) (
                        ((ParameterizedType) (this.getClass().getGenericSuperclass())).getActualTypeArguments()[0]
                )
        );
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




}
