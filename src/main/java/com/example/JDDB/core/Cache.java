package com.example.JDDB.core;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class Cache<T>{
    private final List<T> elements = new ArrayList<>();
    private final Class<?> persistanceClass;

    public Cache(Class<?> persistanceClass){
        this.persistanceClass = persistanceClass;
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

    public void deleteBy(String keyName, Object value) throws NoSuchFieldException{
        Field field = persistanceClass.getDeclaredField(keyName);
        field.setAccessible(true);

        try {
            for (int i=0; i< elements.size(); i++){
                T element = elements.get(i);

                if (field.get(element).equals(value)){
                    elements.remove(i);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
}
