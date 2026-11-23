package com.org.staffmanagement.repository;

import com.org.staffmanagement.models.Organisation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrganisationRepository extends MongoRepository<Organisation, String> {
    Optional<Organisation> findByNameIgnoreCase(String name);
}
