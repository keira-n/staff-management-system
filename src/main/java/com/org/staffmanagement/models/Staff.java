package com.org.staffmanagement.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "Staff")
public class Staff {

    @Id
    private String id;

    // Personal Information
    private String name;
    private String email;
    private String phone;

    // Organisation-specific Information
    private String organisationName;
    private String organisationType;
    private String organisationId;
    private String employeeCode;
    private String department;
    private String position;

    // Linked login account
    private String userAccountId;

    // Flexible industry-specific fields
    private Map<String, Object> customFields = new HashMap<>();

    public Staff() {
    }

    public Staff(String name, String email, String department, String position, String phone) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.position = position;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(String organisationType) {
        this.organisationType = organisationType;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public Map<String, Object> getCustomFields() {
        return customFields;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCustomFields(Map<String, Object> customFields) {
        this.customFields = customFields;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(String userAccountId) {
        this.userAccountId = userAccountId;
    }
}
