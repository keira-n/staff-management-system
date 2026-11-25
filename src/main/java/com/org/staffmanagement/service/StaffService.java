package com.org.staffmanagement.service;

import com.org.staffmanagement.models.Staff;
import com.org.staffmanagement.models.UserAccount;
import com.org.staffmanagement.repository.StaffRepository;
import com.org.staffmanagement.repository.UserAccountRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final UserAccountRepository userAccountRepository;
    private final CurrentUserService currentUserService;

    public StaffService(
            StaffRepository staffRepository,
            UserAccountRepository userAccountRepository,
            CurrentUserService currentUserService
    ) {
        this.staffRepository = staffRepository;
        this.userAccountRepository = userAccountRepository;
        this.currentUserService = currentUserService;
    }

    // Get staff accessible to the authenticated user
    public List<Staff> getAllStaff() {
        String organisationId =
                currentUserService.getCurrentOrganisationId();

        String role = getCurrentRole();
        String department = getCurrentDepartment();

        if (isAdmin(role)) {
            return staffRepository.findByOrganisationId(organisationId);
        }

        if (isHRDepartment(department)) {
            return staffRepository.findByOrganisationId(organisationId)
                    .stream()
                    .filter(this::isEmployee)
                    .toList();
        }

        if (isManager(role)) {
            return staffRepository.findByOrganisationId(organisationId)
                    .stream()
                    .filter(staff ->
                            isSameDepartment(staff.getDepartment(), department)
                                    && isEmployee(staff)
                    )
                    .toList();
        }

        throw new AccessDeniedException(
                "You do not have permission to access staff management"
        );
    }

    // Get staff by organisation type within the authenticated organisation
    public List<Staff> getStaffByOrganisationType(String organisationType) {
        return getAllStaff().stream()
                .filter(staff ->
                        staff.getOrganisationType() != null
                                && staff.getOrganisationType()
                                .equalsIgnoreCase(organisationType)
                )
                .toList();
    }

    // Organisation ownership is determined by the tenant database
    public List<Staff> getStaffByOrganisationId(String organisationId) {
        return getAllStaff();
    }

    // Get staff by ID, only if accessible to the authenticated user
    public Optional<Staff> getStaffById(String id) {
        return getAllStaff().stream()
                .filter(staff -> id.equals(staff.getId()))
                .findFirst();
    }

    // Add staff to the authenticated organisation
    public Staff addStaff(Staff staff) {
        String organisationId =
                currentUserService.getCurrentOrganisationId();

        String role = getCurrentRole();
        String department = getCurrentDepartment();

        if (!canManageStaff(role, department)) {
            throw new AccessDeniedException(
                    "You do not have permission to add staff"
            );
        }

        // Managers can only add staff to their own department
        if (isManager(role) && !isHRDepartment(department)) {
            if (department == null || department.isBlank()) {
                throw new AccessDeniedException(
                        "Your account has no assigned department"
                );
            }

            staff.setDepartment(department);
        }

        staff.setOrganisationId(organisationId);

        if (staff.getEmployeeCode() != null
                && !staff.getEmployeeCode().isBlank()) {

            boolean exists =
                    staffRepository.existsByOrganisationIdAndEmployeeCode(
                            organisationId,
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

    // Update staff accessible to the authenticated user
    public Optional<Staff> updateStaff(String id, Staff updatedStaff) {
        return getStaffById(id)
                .map(existingStaff -> {

                    String employeeCode = updatedStaff.getEmployeeCode();

                    if (employeeCode != null
                            && !employeeCode.isBlank()) {

                        boolean duplicate = staffRepository
                                .findByOrganisationId(
                                        currentUserService.getCurrentOrganisationId()
                                )
                                .stream()
                                .anyMatch(staff ->
                                        !staff.getId().equals(id)
                                                && employeeCode.equals(
                                                staff.getEmployeeCode()
                                        )
                                );

                        if (duplicate) {
                            throw new IllegalArgumentException(
                                    "Employee code already exists in this organisation"
                            );
                        }
                    }

                    // Managers cannot move staff outside their department
                    String role = getCurrentRole();
                    String department = getCurrentDepartment();

                    if (isManager(role)
                            && !isHRDepartment(department)
                            && !isSameDepartment(
                            updatedStaff.getDepartment(),
                            department
                    )) {
                        throw new AccessDeniedException(
                                "You cannot assign staff to another department"
                        );
                    }

                    existingStaff.setName(updatedStaff.getName());
                    existingStaff.setEmail(updatedStaff.getEmail());
                    existingStaff.setDepartment(updatedStaff.getDepartment());
                    existingStaff.setPosition(updatedStaff.getPosition());
                    existingStaff.setPhone(updatedStaff.getPhone());
                    existingStaff.setEmployeeCode(employeeCode);
                    existingStaff.setCustomFields(
                            updatedStaff.getCustomFields()
                    );

                    // Organisation ownership cannot be changed
                    return staffRepository.save(existingStaff);
                });
    }

    // Delete staff only if accessible to the authenticated user
    public void deleteStaff(String id) {
        getStaffById(id).ifPresent(staffRepository::delete);
    }

    // Check whether the current user can manage staff
    private boolean canManageStaff(String role, String department) {
        return isAdmin(role)
                || isHRDepartment(department)
                || isManager(role);
    }

    // Staff without a linked account are treated as ordinary employees
    private boolean isEmployee(Staff staff) {
        String organisationId =
                currentUserService.getCurrentOrganisationId();

        Optional<UserAccount> account =
                userAccountRepository.findByEmailAndOrganisationId(
                        staff.getEmail(),
                        organisationId
                );

        if (account.isEmpty()) {
            return true;
        }

        String staffRole = account.get().getRole();

        return staffRole == null
                || staffRole.equalsIgnoreCase("USER");
    }

    private String getCurrentRole() {
        return currentUserService.getCurrentUserRole();
    }

    private String getCurrentDepartment() {
        return currentUserService.getCurrentUserDepartment();
    }

    private boolean isAdmin(String role) {
        return role != null && role.equalsIgnoreCase("ADMIN");
    }

    private boolean isManager(String role) {
        return role != null && role.equalsIgnoreCase("MANAGER");
    }

    private boolean isHRDepartment(String department) {
        return department != null
                && department.equalsIgnoreCase("HR");
    }

    private boolean isSameDepartment(
            String firstDepartment,
            String secondDepartment
    ) {
        return firstDepartment != null
                && secondDepartment != null
                && firstDepartment.equalsIgnoreCase(secondDepartment);
    }
}