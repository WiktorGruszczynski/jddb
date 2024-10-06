package com.example.JDDB.core.query;

import com.example.JDDB.data.enums.query.DML;

public class Query<T, R>{
    private QueryManager<T> queryManager;

    public Query(){
        this.queryManager = new QueryManager<>();
    }



    public Query<T, R> SELECT(String column){
        queryManager.setDml(DML.SELECT);
        queryManager.setAffectedArea(column);
        return this;
    }

    public Query<T, R> DELETE(){
        queryManager.setDml(DML.DELETE);
        return this;
    }

    public Query<T, R> INSERT(){
        queryManager.setDml(DML.INSERT);
        return this;
    }

    public Query<T, R> UPDATE(){
        queryManager.setDml(DML.UPDATE);
        return this;
    }

    public Query<T, R> WHERE(String statement){
        queryManager.updateFilter(statement);
        return this;
    }

    public Query<T, R> ORDER_BY(){
        return this;
    }

    public Query<T, R> AND(){
        return this;
    }

    public Query<T, R> OR(){
        return this;
    }
}
