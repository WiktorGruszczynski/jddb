package jddb.core.repository;






import java.util.List;
import java.util.Optional;

public interface RepositoryMethods<T>{
    T save(T entity);

    Iterable<T> saveAll(List<T> entities);

    Optional<T> findById(String id);

    boolean existsById(String id);

    List<T> findAll();

    List<T> findAllByIds(List<String> ids);

    long count();

    void deleteById(String id);

    void delete(T entity);

    void deleteAll();

    void deleteAll(List<T> entities);

    void deleteAllById(List<String> ids);
}
