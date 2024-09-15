package com.example.JDDB.lib.repository;


import java.util.Optional;

public interface DiscordRepositoryMethods<ID, T> {
    Iterable<T> findAll();
    Optional<T> findById(ID id);
    void deleteById(ID id);
    T save(T entity);
    Iterable<T> saveAll(Iterable<T> entities);
}
