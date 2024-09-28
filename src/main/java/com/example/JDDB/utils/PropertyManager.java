package com.example.JDDB.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class PropertyManager {
    private final Map<String, String> properties;
    private final String APPLICATION_PROPERTIES = "src/main/resources/application.properties";

    public PropertyManager(){
        this.properties = new HashMap<>();
        initialize();
    }

    private void initialize(){
        try {
            Scanner scanner = new Scanner(
                    new File(APPLICATION_PROPERTIES)
            );

            while (scanner.hasNextLine()){
                String[] parts = scanner.nextLine().split("=");

                if (parts.length==2){
                    properties.put(
                            parts[0],
                            parts[1]
                    );
                }
            }

        }
        catch (FileNotFoundException exception){
            throw new RuntimeException(exception);
        }
    }

    public String get(String propertyName){
        return properties.get(propertyName);
    }

    public Long getLong(String propertyName){
        return Long.valueOf(
                properties.get(propertyName)
        );
    }

    public Set<String> getKeySet(){
        return properties.keySet();
    }
}