package com.org.staffmanagement.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@CompoundIndex(
        name = "email_organisation_unique",
        def = "{'email': 1, 'organisationId': 1}",
        unique = true
)
public class UserAccount {
    @Id
    private String id;

    private String email;
    private String passwordHash;

    private String organisationId;
    private String department;
    private String role;

    private boolean enabled = true;

    public UserAccount() {
    }

    public UserAccount(
            String email,
            String passwordHash,
            String organisationId,
            String role
    ) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.organisationId = organisationId;
        this.role = role;
        this.enabled = true;
    }

    public UserAccount(
            String email,
            String passwordHash,
            String organisationId,
            String department,
            String role
    ) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.organisationId = organisationId;
        this.department = department;
        this.role = role;
        this.enabled = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}