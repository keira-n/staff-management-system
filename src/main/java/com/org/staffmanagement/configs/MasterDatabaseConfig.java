package com.org.staffmanagement.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "com.org.staffmanagement.repository.master",
        mongoTemplateRef = "masterMongoTemplate"
)
public class MasterDatabaseConfig {
}
