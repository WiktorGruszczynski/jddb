package com.example.JDDB.core.query;

public class Result <R>{
    private final R value;

    public Result(R value) {
        this.value = value;
    }

    public R get(){
        return value;
    }
}
