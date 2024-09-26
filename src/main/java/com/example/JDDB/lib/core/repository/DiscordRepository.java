package com.example.JDDB.lib.core.repository;



import java.util.List;
import java.util.Optional;


public class DiscordRepository<T, ID> extends RepositoryInitializer<T, ID> implements RepositoryMethods<T, ID>{

    @Override
    public T save(T entity) {
        return null;
    }

    @Override
    public Iterable<T> saveAll(List<T> entities) {
        return null;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(ID id) {
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
    public void deleteById(ID id) {

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
