package com.org.staffmanagement.controller;

import com.org.staffmanagement.models.Organisation;
import com.org.staffmanagement.service.OrganisationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organisations")
@CrossOrigin(origins = "*")
public class OrganisationController {

    private final OrganisationService organisationService;

    public OrganisationController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    // Get all organisations
    @GetMapping
    public List<Organisation> getAllOrganisations() {
        return organisationService.getAllOrganisations();
    }

    // Get organisation by ID
    @GetMapping("/{id}")
    public ResponseEntity<Organisation> getOrganisationById(
            @PathVariable String id) {

        return organisationService.getOrganisationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create organisation
    @PostMapping
    public Organisation addOrganisation(
            @RequestBody Organisation organisation) {

        return organisationService.addOrganisation(organisation);
    }

    // Update organisation
    @PutMapping("/{id}")
    public ResponseEntity<Organisation> updateOrganisation(
            @PathVariable String id,
            @RequestBody Organisation organisation) {

        return organisationService.updateOrganisation(id, organisation)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete organisation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganisation(
            @PathVariable String id) {

        if (organisationService.getOrganisationById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        organisationService.deleteOrganisation(id);

        return ResponseEntity.noContent().build();
    }
}
