package com.example.JDDB.lib.utils;

import java.util.Date;

public enum AllowedType {
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

    AllowedType(Class<?> type){
        this.type = type;
    }

    public Class<?> getType(){
        return type;
    }

    public static boolean containsType(Class<?> type){
        for (AllowedType allowedType: values()){
            if (allowedType.getType() == type){
                return true;
            }
        }

        return false;
    }

}
