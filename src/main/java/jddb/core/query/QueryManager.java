package jddb.core.query;


import jddb.data.enums.query.DML;
import jddb.data.exceptions.InvalidQueryException;


public class QueryManager<T> {
    private DML dml;
    private Filter<T> filter;
    private Sorter sorter;
    private String affectedArea;
    private long limit;
    private long offset;

    public QueryManager(){
        this.filter = new Filter<>();
        this.limit = Long.MAX_VALUE;
        this.offset = 0;
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
        if (dml.equals(DML.DELETE)){
            return "*";
        }
        return affectedArea;
    }

    public Sorter getSorter() {
        return sorter;
    }

    public void setAffectedArea(String affectedArea) {
        this.affectedArea = affectedArea;
    }

    public void updateFilter(String statement) {
        this.filter.update(statement);
    }

    public void setSorter(Sorter sorter) {
        this.sorter = sorter;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getLimit() {
        return limit;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }
}