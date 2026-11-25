package com.org.staffmanagement.service;

import com.mongodb.client.MongoClient;
import com.org.staffmanagement.models.CentralUserAccount;
import com.org.staffmanagement.models.Organisation;
import com.org.staffmanagement.models.Staff;
import com.org.staffmanagement.models.UserAccount;
import com.org.staffmanagement.repository.StaffRepository;
import com.org.staffmanagement.repository.UserAccountRepository;
import com.org.staffmanagement.repository.master.CentralUserAccountRepository;
import com.org.staffmanagement.repository.master.OrganisationRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class StaffService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String PASSWORD_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";

    private final StaffRepository staffRepository;
    private final UserAccountRepository userAccountRepository;
    private final CentralUserAccountRepository centralUserAccountRepository;
    private final OrganisationRepository organisationRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public StaffService(
            StaffRepository staffRepository,
            UserAccountRepository userAccountRepository,
            CentralUserAccountRepository centralUserAccountRepository,
            OrganisationRepository organisationRepository,
            CurrentUserService currentUserService,
            PasswordEncoder passwordEncoder,
            MongoClient mongoClient
    ) {
        this.staffRepository = staffRepository;
        this.userAccountRepository = userAccountRepository;
        this.centralUserAccountRepository = centralUserAccountRepository;
        this.organisationRepository = organisationRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

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

    public List<Staff> getStaffByOrganisationType(String organisationType) {
        return getAllStaff().stream()
                .filter(staff ->
                        staff.getOrganisationType() != null
                                && staff.getOrganisationType()
                                .equalsIgnoreCase(organisationType)
                )
                .toList();
    }

    public List<Staff> getStaffByOrganisationId(String organisationId) {
        return getAllStaff();
    }

    public Optional<Staff> getStaffById(String id) {
        return getAllStaff().stream()
                .filter(staff -> id.equals(staff.getId()))
                .findFirst();
    }

    // Create a staff record and its login account.
    public StaffCreationResponse addStaff(Staff staff) {
        String organisationId =
                currentUserService.getCurrentOrganisationId();

        String role = getCurrentRole();
        String department = getCurrentDepartment();

        if (!canManageStaff(role, department)) {
            throw new AccessDeniedException(
                    "You do not have permission to add staff"
            );
        }

        if (staff == null) {
            staff.setId(null);
            staff.setUserAccountId(null);
            throw new IllegalArgumentException(
                    "Employee details are required"
            );
        }

        if (staff.getEmail() == null || staff.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "Employee email is required to create a login account"
            );
        }

        String email = normaliseEmail(staff.getEmail());

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            throw new IllegalArgumentException(
                    "Please provide a valid employee email address"
            );
        }

        // Managers can only add staff to their own department.
        if (isManager(role) && !isHRDepartment(department)) {
            if (department == null || department.isBlank()) {
                throw new AccessDeniedException(
                        "Your account has no assigned department"
                );
            }

            staff.setDepartment(department);
        }

        staff.setEmail(email);
        staff.setOrganisationId(organisationId);

        if (staff.getEmployeeCode() != null
                && !staff.getEmployeeCode().isBlank()
                && staffRepository.existsByOrganisationIdAndEmployeeCode(
                organisationId,
                staff.getEmployeeCode()
        )) {
            throw new IllegalArgumentException(
                    "Employee code already exists in this organisation"
            );
        }

        // Check the central directory before creating any tenant records.
        if (centralUserAccountRepository.existsByNormalisedEmail(email)) {
            throw new IllegalArgumentException(
                    "This email address is already registered in another account"
            );
        }

        if (userAccountRepository.existsByEmailAndOrganisationId(
                email,
                organisationId
        )) {
            throw new IllegalArgumentException(
                    "An account with this email already exists"
            );
        }

        Organisation organisation = organisationRepository
                .findById(organisationId)
                .orElseThrow(() -> new IllegalStateException(
                        "Organisation could not be found"
                ));

        String databaseName = organisation.getDatabaseName();

        if (databaseName == null || databaseName.isBlank()
                || !"ACTIVE".equalsIgnoreCase(
                organisation.getDatabaseStatus()
        )) {
            throw new IllegalStateException(
                    "Organisation database is not available"
            );
        }

        String temporaryPassword = generateTemporaryPassword();

        Staff savedStaff = null;
        UserAccount savedUser = null;

        try {
            // Save the staff profile in the tenant database.
            savedStaff = staffRepository.save(staff);

            // Create the employee login account in the same tenant database.
            UserAccount employeeAccount = new UserAccount(
                    email,
                    passwordEncoder.encode(temporaryPassword),
                    organisationId,
                    staff.getDepartment(),
                    "USER"
            );

            employeeAccount.setStaffId(savedStaff.getId());
            employeeAccount.setMustChangePassword(true);

            savedUser = userAccountRepository.save(employeeAccount);

            // Link the staff profile to its login account.
            savedStaff.setUserAccountId(savedUser.getId());
            savedStaff = staffRepository.save(savedStaff);

            // Register the account in the central master database.
            CentralUserAccount centralAccount = new CentralUserAccount(
                    email,
                    organisationId,
                    databaseName,
                    savedUser.getId()
            );

            centralUserAccountRepository.save(centralAccount);

            // Return the plaintext password only in this response.
            return new StaffCreationResponse(
                    savedStaff,
                    temporaryPassword
            );

        } catch (DuplicateKeyException exception) {
            cleanupCreatedEmployee(savedStaff, savedUser);

            throw new IllegalArgumentException(
                    "This email address or employee code is already registered",
                    exception
            );

        } catch (RuntimeException exception) {
            cleanupCreatedEmployee(savedStaff, savedUser);

            throw new IllegalStateException(
                    "Employee account could not be created",
                    exception
            );
        }
    }

    // Update staff accessible to the authenticated user.
    public Optional<Staff> updateStaff(String id, Staff updatedStaff) {
        return getStaffById(id)
                .map(existingStaff -> {

                    String employeeCode = updatedStaff.getEmployeeCode();

                    if (employeeCode != null && !employeeCode.isBlank()) {
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

                    // Do not allow an account-linked employee's login email
                    // to become inconsistent with the central directory.
                    if (existingStaff.getUserAccountId() != null
                            && !normaliseEmail(existingStaff.getEmail())
                            .equals(normaliseEmail(updatedStaff.getEmail()))) {
                        throw new IllegalArgumentException(
                                "An employee's login email cannot be changed here"
                        );
                    }

                    existingStaff.setName(updatedStaff.getName());
                    existingStaff.setDepartment(updatedStaff.getDepartment());
                    existingStaff.setPosition(updatedStaff.getPosition());
                    existingStaff.setPhone(updatedStaff.getPhone());
                    existingStaff.setEmployeeCode(employeeCode);
                    existingStaff.setCustomFields(
                            updatedStaff.getCustomFields()
                    );

                    if (existingStaff.getUserAccountId() == null) {
                        existingStaff.setEmail(updatedStaff.getEmail());
                    }

                    return staffRepository.save(existingStaff);
                });
    }

    // Disable the employee's login account when deleting their staff record.
    public void deleteStaff(String id) {
        getStaffById(id).ifPresent(staff -> {
            String organisationId =
                    currentUserService.getCurrentOrganisationId();

            Optional<UserAccount> account =
                    staff.getUserAccountId() == null
                            ? userAccountRepository.findByEmailAndOrganisationId(
                            staff.getEmail(),
                            organisationId
                    )
                            : userAccountRepository.findByIdAndOrganisationId(
                            staff.getUserAccountId(),
                            organisationId
                    );

            account.ifPresent(user -> {
                user.setEnabled(false);
                userAccountRepository.save(user);

                centralUserAccountRepository
                        .findByNormalisedEmail(normaliseEmail(user.getEmail()))
                        .ifPresent(centralAccount -> {
                            centralAccount.setEnabled(false);
                            centralUserAccountRepository.save(centralAccount);
                        });
            });

            staffRepository.delete(staff);
        });
    }

    private void cleanupCreatedEmployee(
            Staff staff,
            UserAccount user
    ) {
        if (user != null && user.getId() != null) {
            userAccountRepository.deleteById(user.getId());
        }

        if (staff != null && staff.getId() != null) {
            staffRepository.deleteById(staff.getId());
        }
    }

    private String generateTemporaryPassword() {
        int length = 20;
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(
                    PASSWORD_CHARACTERS.length()
            );

            password.append(PASSWORD_CHARACTERS.charAt(index));
        }

        return password.toString();
    }

    private String normaliseEmail(String email) {
        return email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean canManageStaff(String role, String department) {
        return isAdmin(role)
                || isHRDepartment(department)
                || isManager(role);
    }

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

    public record StaffCreationResponse(
            Staff staff,
            String temporaryPassword
    ) {
    }
}
