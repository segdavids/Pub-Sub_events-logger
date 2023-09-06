package com.ef.stateeventslogger.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.sql.Timestamp;
import java.util.Date;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * The type Mongo config.
 */
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
    /**
     * The Database.
     */
    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.host:localhost}:${spring.data.mongodb.port:27017}")
    private String host;

    @NotNull
    @Override
    protected String getDatabaseName() {
        return this.database;
    }

    @NotNull
    @Override
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString("mongodb://" + host);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    @Override
    public void configureConverters(MongoCustomConversions.MongoConverterConfigurationAdapter adapter) {
        adapter.registerConverter(new MongoTimestampFromDateConverter());
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }

    /**
     * The type Mongo local date time from string converter.
     */
    private static final class MongoTimestampFromDateConverter implements Converter<Date, Timestamp> {
        @Override
        public Timestamp convert(Date date) {
            return new Timestamp(date.getTime());
        }
    }
}