package com.example.JDDB.data.enums;

import com.sun.jdi.InvalidTypeException;

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
    DATE(Date.class),
    BOOLEAN_P(boolean.class),
    BYTE_P(byte.class),
    SHORT_P(short.class),
    INTEGER_P(int.class),
    LONG_P(long.class),
    FLOAT_P(float.class),
    DOUBLE_P(double.class),
    CHARACTER_P(char.class);

    private Class<?> type;

    DataType(Class<?> type){
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }


    public static String getTypeName(Class<?> type){
        for (DataType dataType: values()){
            if (dataType.getType() == type){
                return dataType.name();
            }
        }

        throw new RuntimeException(
                new InvalidTypeException(type.getName())
        );
    }
}
