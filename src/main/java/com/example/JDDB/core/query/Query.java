package com.example.JDDB.core.query;

import com.example.JDDB.data.enums.query.DML;
import com.example.JDDB.data.enums.query.OrderDirection;



public class Query<T, R>{
    private DML dml;
    private OrderDirection orderDirection;
    private boolean filter;
    private String tableName;
    private String[] columns;


    public DML getDml() {
        return dml;
    }

    public OrderDirection getOrderDirection() {
        return orderDirection;
    }

    public boolean isFilter() {
        return filter;
    }

    public String getTableName() {
        return tableName;
    }

    public String[] getColumns() {
        return columns;
    }

    public Query(){
        this.filter = true;
    }

    public Query<T, R> select(String... columns){
        this.dml = DML.SELECT;
        this.columns = columns;
        return this;
    }

    public Query<T, R> insert(String... columns){
        this.dml = DML.INSERT;
        this.columns = columns;
        return this;
    }

    public Query<T, R> delete(String... columns){
        this.columns = columns;
        this.dml = DML.DELETE;
        return this;
    }

    public Query<T, R> from(String tableName){
        this.tableName = tableName;
        return this;
    }

    public Query<T, R> where(){
        filter = true;
        return this;
    }

    public Query<T, R> whereNot(){
        filter = true;
        return this;
    }

    public Query<T ,R> and(){
        return this;
    }

    public Query<T, R> or(){
        return this;
    }

    public Query<T, R> orderBy(OrderDirection direction){
        this.orderDirection = direction;
        return this;
    }
}
