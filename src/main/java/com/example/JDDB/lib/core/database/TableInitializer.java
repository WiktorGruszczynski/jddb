package com.example.JDDB.lib.core.database;

import com.example.JDDB.lib.annotations.Id;
import com.example.JDDB.lib.annotations.Table;
import com.example.JDDB.lib.core.DiscordBot;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.lang.reflect.Field;

public class TableInitializer{
    protected TextChannel table;
    protected TextChannel counter;

    private final Category tablesCategory;
    private final Category countersCategory;
    private final String tableName;

    public TableInitializer(Class<?> type){
        if (!hasPrimaryKey(type)){
            throw new RuntimeException("Primary Key missing - " + type.getName());
        }

        tableName = getTableName(type);
        tablesCategory = DiscordBot.getCategory("tables");
        countersCategory = DiscordBot.getCategory("counters");

        initTables();
    }

    private void initTables(){
        if (!tableExists(tablesCategory, tableName)){
            table = createTable(tablesCategory, tableName);
        }
        else {
            table = DiscordBot.getTextChannel(tablesCategory, tableName);
        }

        if (!tableExists(countersCategory, tableName)){
            counter = createTable(countersCategory, tableName);
        }
        else {
            counter = DiscordBot.getTextChannel(countersCategory, tableName);
        }
    }


    private boolean hasPrimaryKey(Class<?> type){
        for (Field field: type.getDeclaredFields()){
            if (field.isAnnotationPresent(Id.class)){
                return true;
            }
        }

        return false;
    }

    private boolean tableExists(Category category, String tableName){
        return DiscordBot.getTextChannel(category, tableName) != null;
    }

    private TextChannel createTable(Category category, String tableName){
        return DiscordBot.createTextChannel(category, tableName);
    }


    private String getTableName(Class<?> type){
        if (type.isAnnotationPresent(Table.class)){
            return type.getAnnotation(Table.class).name();
        }
        else {
            return getSQLName(type.getSimpleName());
        }
    }

    private String getSQLName(String className){
        StringBuilder buffer = new StringBuilder();

        for (int i=0; i<className.length(); i++){
            char chr = className.charAt(i);

            if (i==0){
                buffer.append(
                        Character.toLowerCase(chr)
                );
            }
            else {
                if (Character.isUpperCase(chr)){
                    buffer
                            .append("_")
                            .append(Character.toLowerCase(chr));
                }
                else {
                    buffer.append(chr);
                }
            }
        }

        return buffer.toString();
    }
}
