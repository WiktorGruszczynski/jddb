package com.example.JDDB.core.query;


import com.example.JDDB.data.exceptions.InvalidQueryException;

public class Comparison {
    private String column;
    private String comparator = null;
    private String value;

    public Comparison(String comparisonString){
        String[] comparators = {
                ">=",
                "<=",
                "!=",
                "=",
                ">",
                "<"
        };

        for (String comparator: comparators){
            if (comparisonString.contains(comparator)){
                this.comparator = comparator;
                break;
            }
        }

        if (this.comparator == null){
            throw new RuntimeException(
                    new InvalidQueryException()
            );
        }

        String[] arguments = comparisonString.split(comparator);

        column = arguments[0].trim();
        value = arguments[1].trim();
    }

    public String getConditionValue() {
        return value;
    }

    public String getColumn() {
        return column;
    }

    public String getComparator(){
        return comparator;
    }
}
