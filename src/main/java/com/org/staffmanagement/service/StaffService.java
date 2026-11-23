package com.org.staffmanagement.service;

import com.org.staffmanagement.models.Staff;
import com.org.staffmanagement.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    // Get all staff
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    // Get staff by organisation type
    public List<Staff> getStaffByOrganisationType(String organisationType) {
        return staffRepository.findByOrganisationTypeIgnoreCase(organisationType);
    }

    // Get staff by organisation ID
    public List<Staff> getStaffByOrganisationId(String organisationId) {
        return staffRepository.findByOrganisationId(organisationId);
    }

    // Get staff by ID
    public Optional<Staff> getStaffById(String id) {
        return staffRepository.findById(id);
    }

    // Add staff in each Organi
    public Staff addStaff(Staff staff) {
        if (staff.getOrganisationId() != null
                && !staff.getOrganisationId().isBlank()
                && staff.getEmployeeCode() != null
                && !staff.getEmployeeCode().isBlank()) {

            boolean exists =
                    staffRepository.existsByOrganisationIdAndEmployeeCode(
                            staff.getOrganisationId(),
                            staff.getEmployeeCode()
                    );

            if (exists) {
                throw new IllegalArgumentException(
                        "Employee code already exists in this organisation"
                );
            }
        }

        return staffRepository.save(staff);
    }

    // Update staff
    public Optional<Staff> updateStaff(String id, Staff updatedStaff) {

        return staffRepository.findById(id).map(existingStaff -> {

            String organisationId = existingStaff.getOrganisationId();
            String employeeCode = updatedStaff.getEmployeeCode();

            // Check for duplicate employee codes within the same organisation
            if (organisationId != null
                    && !organisationId.isBlank()
                    && employeeCode != null
                    && !employeeCode.isBlank()) {

                boolean duplicate = staffRepository
                        .findByOrganisationId(organisationId)
                        .stream()
                        .anyMatch(staff ->
                                !staff.getId().equals(id)
                                        && employeeCode.equals(staff.getEmployeeCode())
                        );

                if (duplicate) {
                    throw new IllegalArgumentException(
                            "Employee code already exists in this organisation"
                    );
                }
            }

            // Update personal information
            existingStaff.setName(updatedStaff.getName());
            existingStaff.setEmail(updatedStaff.getEmail());
            existingStaff.setDepartment(updatedStaff.getDepartment());
            existingStaff.setPosition(updatedStaff.getPosition());
            existingStaff.setPhone(updatedStaff.getPhone());

            // Update employee code
            existingStaff.setEmployeeCode(employeeCode);

            // Update custom fields
            existingStaff.setCustomFields(updatedStaff.getCustomFields());

            // Organisation ownership remains unchanged
            return staffRepository.save(existingStaff);
        });
    }


    // Delete staff
    public void deleteStaff(String id) {
        staffRepository.deleteById(id);
    }
}
