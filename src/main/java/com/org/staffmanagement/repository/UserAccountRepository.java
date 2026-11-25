package com.org.staffmanagement.repository;

import com.org.staffmanagement.models.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {

    Optional<UserAccount> findByEmailAndOrganisationId(
            String email,
            String organisationId
    );

    boolean existsByEmailAndOrganisationId(
            String email,
            String organisationId
    );

    Optional<UserAccount> findByIdAndOrganisationId(
            String id,
            String organisationId
    );

    List<UserAccount> findByOrganisationId(String organisationId);
}
