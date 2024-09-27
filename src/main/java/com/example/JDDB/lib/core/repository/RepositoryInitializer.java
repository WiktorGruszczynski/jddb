package com.example.JDDB.lib.core.repository;


import com.example.JDDB.lib.core.connection.Connection;

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
