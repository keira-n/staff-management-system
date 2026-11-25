package com.org.staffmanagement.repository;

import com.org.staffmanagement.models.LeaveRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends MongoRepository<LeaveRequest, String> {

    List<LeaveRequest> findByOrganisationIdOrderBySubmittedAtDesc(
            String organisationId
    );

    List<LeaveRequest> findByOrganisationIdAndEmployeeEmailOrderBySubmittedAtDesc(
            String organisationId,
            String employeeEmail
    );

    List<LeaveRequest> findByOrganisationIdAndDepartmentOrderBySubmittedAtDesc(
            String organisationId,
            String department
    );

    List<LeaveRequest> findByOrganisationIdAndStatusOrderBySubmittedAtDesc(
            String organisationId,
            String status
    );

    Optional<LeaveRequest> findByIdAndOrganisationId(
            String id,
            String organisationId
    );
}