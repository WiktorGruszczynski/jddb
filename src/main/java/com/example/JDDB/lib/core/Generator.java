package com.example.JDDB.lib.core;

import com.example.JDDB.lib.data.GeneratorType;

import java.util.Date;
import java.util.Random;


public class Generator {
    private final GeneratorType generatorType;
    private final Random random;

    public Generator(GeneratorType generatorType){
        this.generatorType = generatorType;
        this.random = new Random();
    }

    private long getCurrentTimestamp(){
        return new Date().getTime();
    }

    public Long generate(){
        return Long.valueOf(
                getCurrentTimestamp() +
                        String.valueOf(random.nextInt(1000,9999))
        );
    }

    public String generateHex(){
        return Long.toHexString(generate());
    }
}
