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
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    // Get staff belonging to the authenticated organisation
    @GetMapping
    public List<Staff> getAllStaff(
            @RequestParam(required = false) String organizationType,
            @RequestParam(required = false) String organisationId) {

        // Organisation ownership is determined by the authenticated user.
        // Client-supplied organisationId is never trusted.
        if (organizationType != null && !organizationType.isBlank()) {
            return staffService.getStaffByOrganisationType(organizationType);
        }

        return staffService.getAllStaff();
    }

    // Get staff by ID within the authenticated organisation
    @GetMapping("/{id}")
    public ResponseEntity<Staff> getStaffById(@PathVariable String id) {

        return staffService.getStaffById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add staff to the authenticated organisation
    @PostMapping
    public ResponseEntity<Staff> addStaff(@RequestBody Staff staff) {

        Staff savedStaff = staffService.addStaff(staff);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedStaff);
    }

    // Update staff belonging to the authenticated organisation
    @PutMapping("/{id}")
    public ResponseEntity<Staff> updateStaff(
            @PathVariable String id,
            @RequestBody Staff staff) {

        return staffService.updateStaff(id, staff)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete staff belonging to the authenticated organisation
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
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", exception.getMessage()));
    }
}
