package com.example.JDDB.core.query;



import com.example.JDDB.data.enums.query.DML;
import com.example.JDDB.data.exceptions.InvalidQueryException;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private List<String> tokens = new ArrayList<>();

    public Tokenizer(String nativeQuery){
        boolean isQuoted = false;
        StringBuilder buffer = new StringBuilder();

        for (char chr: nativeQuery.toCharArray()){
            if (chr == '"'){
                isQuoted = !isQuoted;
            }

            if (!isQuoted){
                if (chr == ';'){
                    break;
                }

                if (chr == ' ' || chr=='='){
                    if (!buffer.isEmpty()){
                        tokens.add(buffer.toString());
                    }

                    buffer.setLength(0);
                    continue;
                }


            }

            buffer.append(chr);

        }

        tokens.add(buffer.toString());
    }

    public DML getDML(){
        for (String token: tokens){
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

        throw new RuntimeException(
                new InvalidQueryException()
        );
    }
}
