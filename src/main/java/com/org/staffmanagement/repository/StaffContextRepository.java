
package com.org.staffmanagement.repository;

import com.org.staffmanagement.models.StaffContext;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StaffContextRepository extends MongoRepository<StaffContext, String> {
    // Find contexts belonging to an organisation
    List<StaffContext> findByOrganisationId(String organisationId);

    // Find a specific context within an organisation
    Optional<StaffContext> findByKeyAndOrganisationId(
            String key,
            String organisationId
    );

    // Check whether a context key already exists within an organisation
    boolean existsByKeyAndOrganisationId(
            String key,
            String organisationId
    );

    // Find a context by ID and organisation ownership
    Optional<StaffContext> findByIdAndOrganisationId(
            String id,
            String organisationId
    );
}
