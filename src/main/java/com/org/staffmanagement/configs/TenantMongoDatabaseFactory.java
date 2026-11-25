package com.org.staffmanagement.configs;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.org.staffmanagement.models.Organisation;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

public class TenantMongoDatabaseFactory extends SimpleMongoClientDatabaseFactory {

    private final MongoClient mongoClient;
    private final MongoTemplate masterMongoTemplate;

    public TenantMongoDatabaseFactory(
            MongoClient mongoClient,
            MongoTemplate masterMongoTemplate,
            String masterDatabase
    ) {
        super(mongoClient, masterDatabase);

        this.mongoClient = mongoClient;
        this.masterMongoTemplate = masterMongoTemplate;
    }

    @Override
    public MongoDatabase getMongoDatabase() {

        String organisationId = TenantContext.getCurrentTenant();

        if (organisationId == null || organisationId.isBlank()) {
            throw new IllegalStateException(
                    "No organisation has been assigned to this request."
            );
        }

        Organisation organisation = masterMongoTemplate.findById(
                organisationId,
                Organisation.class
        );

        if (organisation == null) {
            throw new IllegalStateException(
                    "Organisation could not be found."
            );
        }

        if (organisation.getDatabaseName() == null
                || organisation.getDatabaseName().isBlank()) {
            throw new IllegalStateException(
                    "Organisation database has not been configured."
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(organisation.getDatabaseStatus())) {
            throw new IllegalStateException(
                    "Organisation database is not active."
            );
        }

        return mongoClient.getDatabase(organisation.getDatabaseName());
    }
}
