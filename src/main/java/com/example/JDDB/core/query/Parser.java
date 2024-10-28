package com.example.JDDB.core.query;

import com.example.JDDB.data.enums.query.DML;
import com.example.JDDB.data.exceptions.InvalidQueryException;

import java.util.List;

public class Parser<T> {
    private final List<String> tokens;

    public Parser(String queryString){
        this.tokens = new Tokenizer(queryString).getTokens();
    }


    public DML getDML(){
        for (String token: tokens){
            token = token.toUpperCase();

            if (token.equals("SELECT")){
                return DML.SELECT;
            }
            if (token.equals("INSERT")){
                return DML.INSERT;
            }
            if (token.equals("DELETE")){
                return DML.DELETE;
            }
            if(token.equals("UPDATE")){
                return DML.UPDATE;
            }
        }

        throw new InvalidQueryException("Unknown dml type");
    }

    public Filter<T> getFilter(){
        boolean started = false;

        for (String token: tokens){
            if (token.toUpperCase().equals("WHERE")){
                started = true;
            }

            System.out.println(1);

            if (started){
                System.out.println(token);
            }
        }

        return new Filter<>();
    }

    public String getSelectedColumn(){
        return tokens.get(1);
    }



}
