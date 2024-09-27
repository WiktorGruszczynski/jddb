package com.example.JDDB.lib.core.repository;



import java.util.List;
import java.util.Optional;


public class DiscordRepository<T> extends RepositoryInitializer<T> implements RepositoryMethods<T>{

    @Override
    public T save(T entity) {
        return connection.save(entity);
    }

    @Override
    public Iterable<T> saveAll(List<T> entities) {
        return null;
    }

    @Override
    public Optional<T> findById(String id) {
        return connection.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return false;
    }

    @Override
    public Iterable<T> findAll() {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    public void delete(T entity) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public void deleteAll(List<T> entities) {

    }
}
