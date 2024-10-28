package jddb.core.query;

import jddb.data.enums.query.DML;
import jddb.data.enums.query.SortDirection;
import jddb.data.exceptions.InvalidQueryException;


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

    public Query<T, R> ORDER_BY(String columnName, SortDirection sortDirection){
        queryManager.setSorter(
                new Sorter(columnName, sortDirection)
        );

        return this;
    }



    public Query<T, R> LIMIT(long offset, long limit){
        if (limit <= 0){
            throw new InvalidQueryException(
                    "Limit must be greater than or equal to 0"
            );
        }

        if (offset < 0){
            throw new InvalidQueryException(
                    "Offset must be greater than 0"
            );
        }

        queryManager.setOffset(offset);
        queryManager.setLimit(limit);
        return this;
    }

    public Query<T, R> LIMIT(long limit){
        LIMIT(0, limit);
        return this;
    }

    public Query<T, R> AND(){
        return this;
    }

    public Query<T, R> OR(){
        return this;
    }
}
