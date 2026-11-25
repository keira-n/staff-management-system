package com.org.staffmanagement.configs;

import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "com.org.staffmanagement.repository",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.org\\.staffmanagement\\.repository\\.master\\..*"
        ),
        mongoTemplateRef = "tenantMongoTemplate"
)
public class TenantMongoConfig {

    @Bean
    public TenantMongoDatabaseFactory tenantMongoDatabaseFactory(
            MongoClient mongoClient,
            MongoTemplate masterMongoTemplate,
            @Value("${app.mongodb.master-database}") String masterDatabase
    ) {
        return new TenantMongoDatabaseFactory(
                mongoClient,
                masterMongoTemplate,
                masterDatabase
        );
    }

    @Bean(name = "tenantMongoTemplate")
    public MongoTemplate tenantMongoTemplate(
            TenantMongoDatabaseFactory tenantMongoDatabaseFactory
    ) {
        return new MongoTemplate(tenantMongoDatabaseFactory);
    }
}
