package com.example.JDDB.lib.core.repository;


import com.example.JDDB.lib.core.Connection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RepositoryInitializer<T, ID> {
    protected final Connection<T, ID> connection;

    public RepositoryInitializer(){
        Type[] typeArguments =  ((ParameterizedType) (this.getClass().getGenericSuperclass())).getActualTypeArguments();

        Class<?> persistenceClass = (Class<?>) typeArguments[0];
        Class<?> idClass = (Class<?>) typeArguments[1];

        this.connection = new Connection<>(persistenceClass, idClass);
    }
}
