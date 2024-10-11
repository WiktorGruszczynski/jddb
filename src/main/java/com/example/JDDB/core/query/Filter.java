package com.example.JDDB.core.query;

import com.example.JDDB.core.EntityManager;
import com.example.JDDB.data.exceptions.InvalidQueryException;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Filter<T> {
    private final List<Comparison> comparisons;
    private EntityManager<T> entityManager;

    public Filter(){
        this.comparisons = new ArrayList<>();
    }

    public void setEntityManager(EntityManager<T> entityManager){
        this.entityManager = entityManager;
    }


    private boolean matchesString(String value, String conditionValue, String comparator){
        switch (comparator){
            case "=" -> {
                return value.equals(conditionValue);
            }
            case "!=" -> {
                return !value.equals(conditionValue);
            }
            default -> {
                int valueLength = value.length();
                int conditionValueLength = conditionValue.length();

                switch (comparator) {
                    case ">=" -> {
                        return valueLength >= conditionValueLength;
                    }
                    case "<=" -> {
                        return valueLength <= conditionValueLength;
                    }
                    case ">" -> {
                        return valueLength > conditionValueLength;
                    }
                    case "<" -> {
                        return valueLength < conditionValueLength;
                    }
                }
            }
        }

        throw new RuntimeException(
                new InvalidQueryException("unknown comparator")
        );
    }

    public boolean matchesBoolean(boolean value, String conditionValue, String comparator){
        boolean boolConditionValue;

        if (conditionValue.equals("true") || conditionValue.equals("1")){
            boolConditionValue = true;
        }
        else if (conditionValue.equals("false") || conditionValue.equals("0")){
            boolConditionValue = false;
        }
        else {
            throw new RuntimeException(
                    new InvalidQueryException("invalid condition")
            );
        }

        if (comparator.equals("=")) {
            return value==boolConditionValue;
        }
        else if (comparator.equals("!=")){
            return value!=boolConditionValue;
        }
        else {
            throw new RuntimeException(
                    new InvalidQueryException("invalid comparator for boolean values")
            );
        }
    }

    public boolean matchesInteger(long value, long conditionValue, String comparator){
        return switch (comparator) {
            case "=" -> value == conditionValue;
            case "!=" -> value != conditionValue;
            case ">=" -> value >= conditionValue;
            case "<=" -> value <= conditionValue;
            case ">" -> value > conditionValue;
            case "<" -> value < conditionValue;
            default -> throw new RuntimeException(
                    new InvalidQueryException("unknown comparator")
            );
        };
    }

    public boolean matchesDouble(double value, double conditionValue, String comparator){
        switch (comparator){
            case "=" -> {
                return value==conditionValue;
            }
            case "!=" -> {
                return value!=conditionValue;
            }
            case ">=" -> {
                return value>=conditionValue;
            }
            case "<=" -> {
                return value<=conditionValue;
            }
            case ">" -> {
                return value>conditionValue;
            }
            case "<" -> {
                return value<conditionValue;
            }
            default -> throw new RuntimeException(
                new InvalidQueryException("unknown comparator")
            );
        }
    }


    public boolean matchesFloat(float value, float conditionValue, String comparator){
        switch (comparator){
            case "=" -> {
                return value==conditionValue;
            }
            case "!=" -> {
                return value!=conditionValue;
            }
            case ">=" -> {
                return value>=conditionValue;
            }
            case "<=" -> {
                return value<=conditionValue;
            }
            case ">" -> {
                return value>conditionValue;
            }
            case "<" -> {
                return value<conditionValue;
            }
            default -> throw new RuntimeException(
                    new InvalidQueryException("unknown comparator")
            );
        }
    }

    public boolean matchesDate(Date date, Date conditionValue, String comparator){
        long timestamp = date.getTime();
        long conditionTimestamp = conditionValue.getTime();

        switch (comparator){
            case "=" -> {
                return timestamp == conditionTimestamp;
            }
            case "!=" -> {
                return timestamp != conditionTimestamp;
            }
            case ">=" -> {
                return timestamp >= conditionTimestamp;
            }
            case "<=" -> {
                return timestamp <= conditionTimestamp;
            }
            case ">" -> {
                return timestamp > conditionTimestamp;
            }
            case "<" -> {
                return timestamp < conditionTimestamp;
            }
        }

        return false;
    }

    public boolean matches(T entity){
        boolean[] results = new boolean[comparisons.size()];

        for (int i=0; i<comparisons.size(); ++i){
            Comparison comparison = comparisons.get(i);

            Object foundValue = entityManager.getValueByColumnName(entity, comparison.getColumn());
            String conditionValue = comparison.getConditionValue();
            String comparator = comparison.getComparator();

            if (foundValue instanceof String strValue){
                results[i] = matchesString(strValue, conditionValue, comparator);
            }
            else if (foundValue instanceof Boolean boolValue){
                results[i] = matchesBoolean(boolValue, conditionValue, comparator);
            }
            else if (foundValue instanceof Number numValue){
                if (
                        (numValue instanceof Byte) ||
                        (numValue instanceof Short) ||
                        (numValue instanceof Integer) ||
                        (numValue instanceof Long)){
                    results[i] = matchesInteger(Long.parseLong(numValue.toString()), Long.parseLong(conditionValue), comparator);
                }
                else if (numValue instanceof Double dValue){
                    results[i] = matchesDouble(dValue, Double.parseDouble(conditionValue), comparator);
                }
                else if (numValue instanceof Float floatValue){
                    results[i] = matchesFloat(floatValue, Float.parseFloat(conditionValue), comparator);
                }
            }
            else if (foundValue instanceof Date date){
                results[i] = matchesDate(date, date, comparator);
            }
            else{
                throw new RuntimeException(
                        new InvalidQueryException("unknown type")
                );
            }
        }

        for (boolean result : results){
            if (!result) return false;
        }

        return true;
    }

    public void update(String statement) {
        comparisons.add(
                new Comparison(statement)
        );
    }
}
