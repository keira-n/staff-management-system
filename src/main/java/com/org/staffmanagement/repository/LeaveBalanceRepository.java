package com.org.staffmanagement.repository;

import com.org.staffmanagement.models.LeaveBalance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends MongoRepository<LeaveBalance, String> {

    Optional<LeaveBalance> findByStaffIdAndOrganisationId(
            String staffId,
            String organisationId
    );

    Optional<LeaveBalance> findByEmployeeEmailAndOrganisationId(
            String employeeEmail,
            String organisationId
    );

    List<LeaveBalance> findByOrganisationId(String organisationId);
}
