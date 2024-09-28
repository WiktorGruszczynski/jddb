package com.example.JDDB.data.enums;

import java.util.Date;

public enum DataType {
    BOOLEAN(Boolean.class),
    BYTE(Byte.class),
    SHORT(Short.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    CHARACTER(Character.class),
    STRING(String.class),
    DATE(Date.class);

    private Class<?> type;

    DataType(Class<?> type){
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public static boolean contains(Class<?> type){
        for (DataType dataType: values()){
            if (dataType.getType() == type){
                return true;
            }
        }

        return false;
    }
}
