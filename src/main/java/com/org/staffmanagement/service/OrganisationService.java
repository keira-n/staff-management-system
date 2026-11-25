package com.org.staffmanagement.service;

import com.org.staffmanagement.models.Organisation;
import com.org.staffmanagement.repository.master.OrganisationRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final CurrentUserService currentUserService;

    public OrganisationService(
            OrganisationRepository organisationRepository,
            CurrentUserService currentUserService
    ) {
        this.organisationRepository = organisationRepository;
        this.currentUserService = currentUserService;
    }

    // Get only the authenticated user's organisation
    public List<Organisation> getAllOrganisations() {

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        return organisationRepository.findById(organisationId)
                .map(List::of)
                .orElse(List.of());
    }

    // Get organisation only if it belongs to the authenticated user
    public Optional<Organisation> getOrganisationById(String id) {

        String currentOrganisationId =
                currentUserService.getCurrentOrganisationId();

        if (!currentOrganisationId.equals(id)) {
            return Optional.empty();
        }

        return organisationRepository.findById(id);
    }

    // Update the authenticated user's organisation
    public Optional<Organisation> updateOrganisation(
            String id,
            Organisation updatedOrganisation
    ) {
        verifyAdminAccess();

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        if (!organisationId.equals(id)) {
            return Optional.empty();
        }

        return organisationRepository.findById(id).map(existing -> {

            existing.setName(updatedOrganisation.getName());
            existing.setType(updatedOrganisation.getType());
            existing.setDescription(updatedOrganisation.getDescription());

            return organisationRepository.save(existing);
        });
    }

    // Delete the authenticated user's organisation
    public void deleteOrganisation(String id) {

        verifyAdminAccess();

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        if (!organisationId.equals(id)) {
            throw new AccessDeniedException(
                    "You cannot delete another organisation"
            );
        }

        organisationRepository.findById(id).ifPresent(
                organisationRepository::delete
        );
    }

    // Only organisation administrators can manage organisation settings
    private void verifyAdminAccess() {

        String role = currentUserService.getCurrentUserRole();

        if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException(
                    "Administrator access required"
            );
        }
    }
}