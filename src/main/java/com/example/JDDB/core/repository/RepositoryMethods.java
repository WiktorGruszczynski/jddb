package com.example.JDDB.core.repository;




import com.example.JDDB.core.query.Query;


import java.util.List;
import java.util.Optional;

public interface RepositoryMethods<T>{
    T save(T entity);

    Iterable<T> saveAll(List<T> entities);

    Optional<T> findById(String id);

    boolean existsById(String id);

    List<T> findAll();

    long count();

    void deleteById(String id);

    void delete(T entity);

    void deleteAll();

    void deleteAll(List<T> entities);

    <R> R executeQuery(Query<T, R> query);
}
