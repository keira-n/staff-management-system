package com.org.staffmanagement.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Locale;

@Document(collection = "user_accounts")
public class CentralUserAccount {

    @Id
    private String id;

    @Indexed(name = "normalised_email_unique", unique = true)
    private String normalisedEmail;

    private String organisationId;
    private String databaseName;
    private String userId;

    private boolean enabled = true;

    public CentralUserAccount() {
    }

    public CentralUserAccount(
            String email,
            String organisationId,
            String databaseName,
            String userId
    ) {
        this.normalisedEmail = normaliseEmail(email);
        this.organisationId = organisationId;
        this.databaseName = databaseName;
        this.userId = userId;
        this.enabled = true;
    }

    private String normaliseEmail(String email) {
        return email == null
                ? null
                : email.trim().toLowerCase(Locale.ROOT);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNormalisedEmail() {
        return normalisedEmail;
    }

    public void setNormalisedEmail(String normalisedEmail) {
        this.normalisedEmail = normaliseEmail(normalisedEmail);
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
