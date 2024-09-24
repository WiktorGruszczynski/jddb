package com.example.JDDB.lib.core.database;


import com.example.JDDB.lib.annotations.Id;
import com.example.JDDB.lib.core.DiscordBot;
import com.example.JDDB.lib.core.Codec;
import net.dv8tion.jda.api.entities.Message;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;


public class Connection<T> extends TableInitializer{
    private final Class<T> entityType;
    private final Codec<T> codec;

    public Connection(Class<T> entityType){
        super(entityType);
        this.entityType = entityType;
        this.codec = new Codec<>(entityType);
    }

    private void updateId(T entity, String id){
        for (Field field: entity.getClass().getDeclaredFields()){
            if (field.isAnnotationPresent(Id.class)){
                field.setAccessible(true);

                try {
                    field.set(entity, id);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public T saveEntity(T entity){
        Message message = DiscordBot.sendPlainMessage(
                codec.encode(entity),
                table
        );

        updateId(entity, message.getId());

        return entity;
    }

    public List<T> saveEntities(List<T> entities){
        return null;
    }

    public Optional<T> findEntityById(String id) {
        return Optional.of(
            codec.decode(
                    DiscordBot.getMessageById(id, table)
            )
        );
    }

    public boolean existsById(String id) {
        return DiscordBot.getMessageById(id, table) != null;
    }

    public List<T> findAllEntities() {
        return null;
    }

    public long countEntities(){
        return 0;
    }

    public void deleteById(String id) {
    }
}
