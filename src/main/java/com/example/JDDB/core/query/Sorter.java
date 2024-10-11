package com.example.JDDB.core.query;

import com.example.JDDB.data.enums.query.SortDirection;

public class Sorter {
    private String columnName;
    private SortDirection sortDirection;

    public Sorter(String columnName, SortDirection sortDirection) {
        this.columnName = columnName;
        this.sortDirection = sortDirection;
    }

    public String getColumnName() {
        return columnName;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }
}
