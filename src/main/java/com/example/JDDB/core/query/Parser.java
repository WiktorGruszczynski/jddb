package com.example.JDDB.core.query;

public class Parser {

    public Tokenizer getTokenizer(String nativeQuery){
        return new Tokenizer(nativeQuery);
    }
}
