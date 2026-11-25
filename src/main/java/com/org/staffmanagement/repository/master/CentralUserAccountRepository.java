package com.org.staffmanagement.repository.master;

import com.org.staffmanagement.models.CentralUserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CentralUserAccountRepository
        extends MongoRepository<CentralUserAccount, String> {

    Optional<CentralUserAccount> findByNormalisedEmail(
            String normalisedEmail
    );

    boolean existsByNormalisedEmail(String normalisedEmail);
}
