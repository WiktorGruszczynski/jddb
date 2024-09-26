package com.example.JDDB.lib.data;

public enum PrimaryKeyType {
    BYTE(Byte.class),
    SHORT(Short.class),
    INTEGER(Integer.class),
    LONG(Integer.class);

    private Class<?> type;

    PrimaryKeyType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType(){
        return type;
    }

    public static boolean containsType(Class<?> type){
        for (PrimaryKeyType primaryKeyType: values()){
            if (primaryKeyType.getType() == type){
                return true;
            }
        }

        return false;
    }
}
