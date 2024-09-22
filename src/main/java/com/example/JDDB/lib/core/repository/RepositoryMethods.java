package com.example.JDDB.lib.core.repository;


import java.util.List;
import java.util.Optional;

public interface RepositoryMethods<T, ID>{
    T save(T entity);

    Iterable<T> saveAll(List<T> entities);

    Optional<T> findById(ID id);

    boolean existsById(ID id);

    Iterable<T> findAll();

    long count();

    void deleteById(ID id);

    void delete(T entity);

    void deleteAll();

    void deleteAll(Iterable<T> entities);

    <R> R executeQuery(String query);
}
