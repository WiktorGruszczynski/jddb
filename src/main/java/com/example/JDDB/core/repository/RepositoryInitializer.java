package com.example.JDDB.core.repository;


import com.example.JDDB.core.connection.Connection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RepositoryInitializer<T> {
    protected final Connection<T> connection;

    public RepositoryInitializer(){
        Type[] typeArguments =  ((ParameterizedType) (this.getClass().getGenericSuperclass())).getActualTypeArguments();
        Class<?> persistenceClass = (Class<?>) typeArguments[0];

        this.connection = new Connection<>(persistenceClass);
    }
}
