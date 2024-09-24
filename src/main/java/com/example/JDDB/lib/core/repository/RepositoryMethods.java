package com.example.JDDB.lib.core.repository;


import com.example.JDDB.lib.core.database.Query;

import java.util.List;
import java.util.Optional;

public interface RepositoryMethods<T>{
    T save(T entity);

    Iterable<T> saveAll(List<T> entities);

    Optional<T> findById(String id);

    boolean existsById(String id);

    Iterable<T> findAll();

    long count();

    void deleteById(String id);

    void delete(T entity);

    void deleteAll();

    void deleteAll(Iterable<T> entities);

    Query<?> executeQuery(Query<T> query);
}
