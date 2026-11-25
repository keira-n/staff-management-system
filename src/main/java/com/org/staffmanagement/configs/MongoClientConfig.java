package com.org.staffmanagement.configs;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoClientConfig {

    @Bean
    public MongoClient mongoClient(
            @Value("${app.mongodb.uri}") String mongoUri
    ) {
        return MongoClients.create(mongoUri);
    }

    @Bean(name = "masterMongoTemplate")
    @Primary
    public MongoTemplate masterMongoTemplate(
            MongoClient mongoClient,
            @Value("${app.mongodb.master-database}") String masterDatabase
    ) {
        return new MongoTemplate(mongoClient, masterDatabase);
    }
}