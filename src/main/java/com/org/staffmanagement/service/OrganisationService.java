package com.org.staffmanagement.service;

import com.org.staffmanagement.models.Organisation;
import com.org.staffmanagement.repository.OrganisationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    public OrganisationService(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    // Get all organizations
    public List<Organisation> getAllOrganisations() {
        return organisationRepository.findAll();
    }

    // Get organisation by ID
    public Optional<Organisation> getOrganisationById(String id) {
        return organisationRepository.findById(id);
    }

    // Create organisation
    public Organisation addOrganisation(Organisation organisation) {
        return organisationRepository.save(organisation);
    }

    // Update organisation
    public Optional<Organisation> updateOrganisation(
            String id, Organisation updatedOrganisation) {

        return organisationRepository.findById(id).map(existing -> {
            existing.setName(updatedOrganisation.getName());
            existing.setType(updatedOrganisation.getType());
            existing.setDescription(updatedOrganisation.getDescription());

            return organisationRepository.save(existing);
        });
    }

    // Delete organisation
    public void deleteOrganisation(String id) {
        organisationRepository.deleteById(id);
    }
}
