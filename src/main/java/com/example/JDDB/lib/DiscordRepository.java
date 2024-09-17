package com.example.JDDB.lib;

import org.springframework.stereotype.Repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

@Repository
public class DiscordRepository<T, ID>{
    public DiscordRepository(){

    }

    public void test(T t){
        System.out.println(t.getClass());
    }

    public Optional<T> findById(ID id){
        return null;
    }
}
