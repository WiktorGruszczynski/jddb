package com.example.JDDB.lib.core.repository;



import java.util.List;
import java.util.Optional;


public class DiscordRepository<T> extends RepositoryInitializer<T> implements RepositoryMethods<T>{

    @Override
    public T save(T entity) {
        return connection.save(entity);
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        return connection.saveAll(entities);
    }

    @Override
    public Optional<T> findById(String id) {
        return connection.findEntityById(id);
    }

    @Override
    public boolean existsById(String id) {
        return connection.existsById(id);
    }

    @Override
    public List<T> findAll() {
        return connection.findAll();
    }

    @Override
    public long count() {
        return connection.countEntities();
    }

    @Override
    public void deleteById(String id) {
        connection.deleteById(id);
    }

    @Override
    public void delete(T entity) {
        connection.delete(entity);
    }

    @Override
    public void deleteAll() {
        connection.deleteAll();
    }

    @Override
    public void deleteAll(List<T> entities) {
        connection.deleteAll(entities);
    }
}
