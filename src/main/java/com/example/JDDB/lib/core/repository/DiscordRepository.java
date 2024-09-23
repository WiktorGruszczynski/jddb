package com.example.JDDB.lib.core.repository;

import java.util.List;
import java.util.Optional;

public class DiscordRepository<T>
        extends RepositoryInitializer<T>
        implements RepositoryMethods<T> {

    public DiscordRepository(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public T save(T entity) {
        return dbConnection.saveEntity(TABLE_NAME, entity);
    }

    @Override
    public Iterable<T> saveAll(List<T> entities) {
        return dbConnection.saveEntities(TABLE_NAME, entities);
    }

    @Override
    public Optional<T> findById(String id) {
        return dbConnection.findEntityById(TABLE_NAME, id);
    }

    @Override
    public boolean existsById(String id) {
        return dbConnection.existsById(TABLE_NAME, id);
    }

    @Override
    public List<T> findAll() {
        return dbConnection.findAllEntities(TABLE_NAME);
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String id) {
        dbConnection.deleteById(TABLE_NAME, id);
    }

    @Override
    public void delete(T entity) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public void deleteAll(Iterable<T> entities) {

    }

    @Override
    public <R> R executeQuery(String query) {
        return null;
    }


}
