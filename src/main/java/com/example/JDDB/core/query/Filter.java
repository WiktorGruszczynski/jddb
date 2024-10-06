package com.example.JDDB.core.query;

import com.example.JDDB.core.EntityManager;


import java.util.ArrayList;
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

    public boolean matches(T entity){
        for (Comparison comparison: comparisons){

            Object firstValue = entityManager.getValueByColumnName(entity, comparison.getColumn());
            String secondValue = comparison.getValue();


            if (firstValue instanceof Number numValue){
                if (numValue instanceof Short
                        || numValue instanceof Integer
                        || numValue instanceof Long){

                    long num1 = Long.parseLong(String.valueOf(numValue));
                    long num2 = Long.parseLong(secondValue);

                    if (num1>num2){
                        return true;
                    }

                }

            }
        }

        return false;
    }

    public void update(String statement) {
        comparisons.add(
                new Comparison(statement)
        );
    }
}
