package com.example.JDDB.core.query;


import com.example.JDDB.data.enums.query.DML;
import com.example.JDDB.data.exceptions.InvalidQueryException;

public class QueryManager<T> {
    private DML dml;
    private Filter<T> filter;
    private String affectedArea;

    public QueryManager(){
        this.filter = new Filter<>();
    }



    public DML getDml() {
        return dml;
    }

    public void setDml(DML dml) {
        if (this.dml != null){
            throw new RuntimeException(
                    new InvalidQueryException("two DML commands occurred in the same query")
            );
        }

        this.dml = dml;
    }

    public Filter<T> getFilter() {
        return filter;
    }

    public String getAffectedArea() {
        return affectedArea;
    }

    public void setAffectedArea(String affectedArea) {
        this.affectedArea = affectedArea;
    }

    public void updateFilter(String statement) {
        this.filter.update(statement);
    }
}