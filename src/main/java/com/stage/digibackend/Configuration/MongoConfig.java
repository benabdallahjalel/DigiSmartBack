package com.stage.digibackend.Configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "Test";
    }
    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        // Configure MongoDB connection settings
        builder.applyConnectionString(new ConnectionString("mongodb+srv://wissemhammouda:wissemh18@cluster0.jn5c3zo.mongodb.net/?retryWrites=true&w=majority"));
    }
}
