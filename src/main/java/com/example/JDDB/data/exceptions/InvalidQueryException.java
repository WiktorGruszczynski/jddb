package com.example.JDDB.data.exceptions;

public class InvalidQueryException extends Exception{
    public InvalidQueryException(){

    }

    public InvalidQueryException(String message){
        super(message);
    }
}
