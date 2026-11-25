package com.org.staffmanagement.controller;

import com.org.staffmanagement.models.Organisation;
import com.org.staffmanagement.service.CurrentUserService;
import com.org.staffmanagement.service.OrganisationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organisations")
public class OrganisationController {

    private final OrganisationService organisationService;
    private final CurrentUserService currentUserService;

    public OrganisationController(
            OrganisationService organisationService,
            CurrentUserService currentUserService
    ) {
        this.organisationService = organisationService;
        this.currentUserService = currentUserService;
    }

    // Get only the authenticated user's organisation
    @GetMapping
    public List<Organisation> getAllOrganisations() {

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        return organisationService.getOrganisationById(organisationId)
                .map(List::of)
                .orElse(List.of());
    }

    // Get organisation by ID, only if it belongs to the current user
    @GetMapping("/{id}")
    public ResponseEntity<Organisation> getOrganisationById(
            @PathVariable String id) {

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        if (!organisationId.equals(id)) {
            return ResponseEntity.notFound().build();
        }

        return organisationService.getOrganisationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Organisation creation is handled through /api/auth/register
    @PostMapping
    public ResponseEntity<Map<String, String>> addOrganisation() {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error",
                        "Please use the organisation registration endpoint"
                ));
    }

    // Update the current organisation (ADMIN only)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrganisation(
            @PathVariable String id,
            @RequestBody Organisation organisation) {

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        if (!organisationId.equals(id)) {
            return ResponseEntity.notFound().build();
        }

        if (!"ADMIN".equals(currentUserService.getCurrentUserRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Administrator access required"));
        }

        organisation.setId(id);

        return organisationService.updateOrganisation(id, organisation)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete the current organisation (ADMIN only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrganisation(
            @PathVariable String id) {

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        if (!organisationId.equals(id)) {
            return ResponseEntity.notFound().build();
        }

        if (!"ADMIN".equals(currentUserService.getCurrentUserRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Administrator access required"));
        }

        if (organisationService.getOrganisationById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        organisationService.deleteOrganisation(id);

        return ResponseEntity.noContent().build();
    }
}
