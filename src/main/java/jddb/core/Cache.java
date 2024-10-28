package jddb.core;


import jddb.data.annotations.Id;
import jddb.data.exceptions.NoPrimaryKeyException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class Cache<T>{
    private final List<T> elements = new ArrayList<>();
    private final Class<?> persistanceClass;
    private final EntityManager<T> entityManager;
    private final Field primaryKeyField;

    public Cache(Class<?> persistanceClass){
        this.persistanceClass = persistanceClass;
        this.entityManager = new EntityManager<>(persistanceClass);
        this.primaryKeyField = getPrimaryKeyField();
    }

    private Field getPrimaryKeyField(){
        for (Field field: persistanceClass.getDeclaredFields()){
            if (field.isAnnotationPresent(Id.class)){
                return field;
            }
        }

        throw new NoPrimaryKeyException();
    }

    public long size(){
        return elements.size();
    }

    public void add(T entity){
        elements.add(entity);
    }

    public void addAll(List<T> inputArray){
        elements.addAll(inputArray);
    }

    public void deleteAll(){
        elements.clear();
    }

    public List<T> getAll(){
        return elements;
    }

    public T getBy(String keyName, Object value) throws NoSuchFieldException {
        Field field = persistanceClass.getDeclaredField(keyName);
        field.setAccessible(true);

        try {
            for (T element : elements){
                if (field.get(element).equals(value)){
                    return element;
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public void deleteManyBy(String keyName, Object value) throws NoSuchFieldException{
        Field field = persistanceClass.getDeclaredField(keyName);
        field.setAccessible(true);

        elements.removeIf(element -> {
            try {
                return field.get(element).equals(value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void deleteOneBy(String keyName, Object value) throws NoSuchFieldException{
        Field field = persistanceClass.getDeclaredField(keyName);
        field.setAccessible(true);

        try {
            for (int i=0; i< elements.size(); i++){
                T element = elements.get(i);

                if (field.get(element).equals(value)){
                    elements.remove(i);
                    return;
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteOneById(String value){
        elements.removeIf(element -> {
            try {
                return primaryKeyField.get(element).equals(value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void deleteAllByIds(List<String> ids) {
        elements.removeIf(entity ->
                ids.contains(
                        entityManager.getPrimaryKey(entity)
                )
        );
    }

    public List<T> getAllByIds(List<String> ids) {
        List<T> result = new ArrayList<>();

        for (T entity : elements){
            if (ids.contains(
                    entityManager.getPrimaryKey(entity)
            )){
                result.add(entity);
            }
        }

        return result;
    }
}
