package com.org.staffmanagement.repository;

import com.org.staffmanagement.models.Staff;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends MongoRepository<Staff, String> {

    List<Staff> findByOrganisationTypeIgnoreCase(String organisationType);

    List<Staff> findByOrganisationId(String organisationId);

    Optional<Staff> findByEmailAndOrganisationId(
            String email,
            String organisationId
    );

    boolean existsByOrganisationIdAndEmployeeCode(
            String organisationId,
            String employeeCode
    );
}
