package com.example.JDDB.lib.core.repository;

import com.example.JDDB.lib.core.database.Query;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

public class DiscordRepository<T>
        extends RepositoryInitializer<T>
        implements RepositoryMethods<T> {


    @Override
    public T save(T entity) {
        return connection.saveEntity(entity);
    }

    @Override
    public Iterable<T> saveAll(List<T> entities) {
        return connection.saveEntities(entities);
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
        return connection.findAllEntities();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String id) {
        connection.deleteById(id);
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
    public Query<?> executeQuery(Query<T> query) {
        return null;
    }
}
