package com.org.staffmanagement.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "leave_balances")
@CompoundIndex(
        name = "staff_organisation_unique",
        def = "{'staffId': 1, 'organisationId': 1}",
        unique = true
)
public class LeaveBalance {

    @Id
    private String id;

    private String organisationId;
    private String staffId;
    private String employeeEmail;

    private int annualSickLeave = 10; // Default
    private int usedSickLeave = 0;

    private int annualPaidVacation = 20; // Default
    private int usedPaidVacation = 0;

    public LeaveBalance() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public int getAnnualSickLeave() {
        return annualSickLeave;
    }

    public void setAnnualSickLeave(int annualSickLeave) {
        this.annualSickLeave = annualSickLeave;
    }

    public int getUsedSickLeave() {
        return usedSickLeave;
    }

    public void setUsedSickLeave(int usedSickLeave) {
        this.usedSickLeave = usedSickLeave;
    }

    public int getAnnualPaidVacation() {
        return annualPaidVacation;
    }

    public void setAnnualPaidVacation(int annualPaidVacation) {
        this.annualPaidVacation = annualPaidVacation;
    }

    public int getUsedPaidVacation() {
        return usedPaidVacation;
    }

    public void setUsedPaidVacation(int usedPaidVacation) {
        this.usedPaidVacation = usedPaidVacation;
    }

    public int getRemainingSickLeave() {
        return Math.max(0, annualSickLeave - usedSickLeave);
    }

    public int getRemainingPaidVacation() {
        return Math.max(0, annualPaidVacation - usedPaidVacation);
    }
}