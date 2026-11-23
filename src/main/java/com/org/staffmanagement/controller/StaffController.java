package com.org.staffmanagement.controller;

import com.org.staffmanagement.models.Staff;
import com.org.staffmanagement.service.StaffService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    // Get all staff by organisation
    @GetMapping
    public List<Staff> getAllStaff(
            @RequestParam(required = false) String organizationType,
            @RequestParam(required = false) String organisationId) {

        if (organisationId != null && !organisationId.isBlank()) {
            return staffService.getStaffByOrganisationId(organisationId);
        }

        if (organizationType != null && !organizationType.isBlank()) {
            return staffService.getStaffByOrganisationType(organizationType);
        }

        return staffService.getAllStaff();
    }

    // Get staff by ID
    @GetMapping("/{id}")
    public ResponseEntity<Staff> getStaffById(@PathVariable String id) {

        return staffService.getStaffById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add staff
    @PostMapping
    public Staff addStaff(@RequestBody Staff staff) {
        return staffService.addStaff(staff);
    }

    // Update staff
    @PutMapping("/{id}")
    public ResponseEntity<Staff> updateStaff(
            @PathVariable String id,
            @RequestBody Staff staff) {

        return staffService.updateStaff(id, staff)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete staff
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable String id) {

        if (staffService.getStaffById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        staffService.deleteStaff(id);

        return ResponseEntity.noContent().build();
    }

    // Handle duplicate employee codes
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmployeeCode(
            IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", exception.getMessage()));
    }
}