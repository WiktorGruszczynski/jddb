package jddb.core.connection;

import jddb.core.Cache;
import jddb.core.Codec;
import jddb.core.DiscordBot;
import jddb.core.EntityManager;
import jddb.data.enums.DataType;
import jddb.utils.Generator;
import jddb.utils.ThreadManager;
import jddb.utils.UrlReader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.lang.reflect.Field;
import java.util.List;


public class ConnectionInitializer<T>{
    protected final Class<?> entityType;
    protected final EntityManager<T> entityManager;
    protected final Codec<T> codec;
    protected final Guild guild;
    protected final Generator generator;
    protected TextChannel tableChannel;
    protected final UrlReader urlReader;
    protected final Cache<T> cache;
    protected final ThreadManager threadManager;


    public ConnectionInitializer(Class<?> entityType){
        this.entityType = entityType;
        this.entityManager = new EntityManager<>(entityType);
        this.codec = new Codec<>(entityType);
        this.guild = DiscordBot.getGuild();
        this.urlReader = new UrlReader();
        this.threadManager = new ThreadManager();
        this.cache = new Cache<>(entityType);
        this.generator = initGenerator();
        this.tableChannel = initTableChannel();

        initSchemaChannel();
    }

    private Generator initGenerator(){
        return new Generator(
                entityManager.getGeneratorType()
        );
    }

    private TextChannel initTableChannel(){
        return initTextChannel("tables", entityManager.getTableName());
    }

    private void initSchemaChannel(){
        TextChannel schemaChannel = initTextChannel("schemas", entityManager.getTableName());
        List<Message> history = schemaChannel.getHistory().retrievePast(1).complete();
        String content = generateSchemaMessageContent();

        if (history.isEmpty()){
            schemaChannel.sendMessage(content).queue();
        }
        else {
            Message msg = history.get(0);

            if (!msg.getContentRaw().equals(content)){
                msg.editMessage(content).queue();
            }
        }
    }

    private String generateSchemaMessageContent(){
        StringBuilder builder = new StringBuilder();

        for (Field field : entityType.getDeclaredFields()) {
            String columnName = entityManager.getColumnName(field);
            String type = DataType.getTypeName(field.getType());
            boolean nullable = entityManager.isColumnNullable(field);

            builder
                    .append(columnName)
                    .append(":")
                    .append("  type=")
                    .append(type)
                    .append(",  nullable=")
                    .append(nullable)
            ;

            if (entityManager.isPrimaryKey(field)){
                builder.append(",  PK");
            }

            builder.append("\n");
        }

        return builder.toString();
    }


    private TextChannel initTextChannel(String categoryName, String channelName){
        Category category = DiscordBot.getCategoryByName(categoryName);
        TextChannel textChannel = DiscordBot.getTextChannel(category, channelName);

        if (textChannel == null){
            return guild.createTextChannel(channelName, category).complete();
        }
        else {
            return textChannel;
        }
    }
}
