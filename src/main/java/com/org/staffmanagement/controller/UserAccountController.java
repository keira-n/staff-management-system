package com.org.staffmanagement.controller;

import com.org.staffmanagement.models.UserAccount;
import com.org.staffmanagement.repository.UserAccountRepository;
import com.org.staffmanagement.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/users")
public class UserAccountController {

    private final UserAccountRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public UserAccountController(
            UserAccountRepository userRepository,
            CurrentUserService currentUserService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    // Get users belonging to the authenticated organisation
    @GetMapping
    public List<UserResponse> getUsers() {
        requireAdmin();

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        return userRepository.findByOrganisationId(organisationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Create a user or manager account
    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody CreateUserRequest request
    ) {
        requireAdmin();

        if (request.email() == null || request.email().isBlank()
                || request.password() == null
                || request.password().length() < 8) {
            return ResponseEntity.badRequest()
                    .body("A valid email and password of at least 8 characters are required.");
        }

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            return ResponseEntity.badRequest()
                    .body("Please provide a valid email address.");
        }

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        if (userRepository.existsByEmailAndOrganisationId(
                email,
                organisationId
        )) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("An account with this email already exists.");
        }

        String role = request.role() == null
                || request.role().isBlank()
                ? "USER"
                : request.role().trim().toUpperCase(Locale.ROOT);

        if (!role.equals("USER") && !role.equals("MANAGER")) {
            return ResponseEntity.badRequest()
                    .body("Role must be USER or MANAGER.");
        }

        String department = request.department() == null
                ? ""
                : request.department().trim();

        if (role.equals("MANAGER") && department.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("A department is required for manager accounts.");
        }

        UserAccount user = new UserAccount(
                email,
                passwordEncoder.encode(request.password()),
                organisationId,
                role
        );

        user.setDepartment(department);

        UserAccount savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(savedUser));
    }

    // Only organisation administrators can manage user accounts
    private void requireAdmin() {
        String role = currentUserService.getCurrentUserRole();

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            throw new AccessDeniedException(
                    "Only organisation administrators can manage users."
            );
        }
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getDepartment(),
                user.isEnabled()
        );
    }

    public record CreateUserRequest(
            String email,
            String password,
            String role,
            String department
    ) {
    }

    public record UserResponse(
            String id,
            String email,
            String role,
            String department,
            boolean enabled
    ) {
    }
}