package com.example.JDDB.data.exceptions;

public class InvalidQueryException extends RuntimeException {
    public InvalidQueryException(){

    }

    public InvalidQueryException(String message){
        super(message);
    }
}
