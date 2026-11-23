package com.org.staffmanagement.repository;

import com.org.staffmanagement.models.StaffContext;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StaffContextRepository
        extends MongoRepository<StaffContext, String> {

    Optional<StaffContext> findByKey(String key);
}
