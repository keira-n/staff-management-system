package com.org.staffmanagement.service;

import com.mongodb.client.MongoClient;
import com.org.staffmanagement.models.Organisation;
import com.org.staffmanagement.models.UserAccount;
import com.org.staffmanagement.repository.master.OrganisationRepository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
public class AuthenticationService {

    private final MongoClient mongoClient;
    private final OrganisationRepository organisationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthenticationService(
            MongoClient mongoClient,
            OrganisationRepository organisationRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder
    ) {
        this.mongoClient = mongoClient;
        this.organisationRepository = organisationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    // Register an organisation and provision its dedicated database
    public AuthResponse registerOrganisation(RegisterRequest request) {

        if (request.organisationName() == null ||
                request.organisationName().isBlank()) {
            throw new IllegalArgumentException(
                    "Organisation name is required"
            );
        }

        if (request.organisationType() == null ||
                request.organisationType().isBlank()) {
            throw new IllegalArgumentException(
                    "Organisation type is required"
            );
        }

        validateCredentials(request.email(), request.password());

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        Organisation organisation = new Organisation(
                request.organisationName().trim(),
                request.organisationType().trim(),
                request.description()
        );

        // Save organisation in the master database
        Organisation savedOrganisation =
                organisationRepository.save(organisation);

        // Assign a unique database name
        String databaseName =
                "staff_org_" + savedOrganisation.getId();

        savedOrganisation.setDatabaseName(databaseName);
        savedOrganisation.setDatabaseStatus("PROVISIONING");

        organisationRepository.save(savedOrganisation);

        UserAccount savedUser;

        try {

            // Connect directly to the new organisation database
            MongoTemplate tenantTemplate =
                    new MongoTemplate(mongoClient, databaseName);

            // Create the initial administrator account
            UserAccount admin = new UserAccount(
                    email,
                    passwordEncoder.encode(request.password()),
                    savedOrganisation.getId(),
                    "ADMIN"
            );

            // Saving the account creates the tenant database
            savedUser = tenantTemplate.save(admin);

            // Activate the database after successful provisioning
            savedOrganisation.setDatabaseStatus("ACTIVE");

            organisationRepository.save(savedOrganisation);

        } catch (RuntimeException exception) {

            savedOrganisation.setDatabaseStatus("FAILED");
            organisationRepository.save(savedOrganisation);

            throw new IllegalStateException(
                    "Organisation database provisioning failed.",
                    exception
            );
        }

        // Generate JWT only after successful provisioning
        return createAuthResponse(savedUser, savedOrganisation);
    }

    // Authenticate a user against their organisation database
    public AuthResponse login(LoginRequest request) {

        if (request.organisationId() == null ||
                request.organisationId().isBlank()) {
            throw new IllegalArgumentException(
                    "Organisation ID is required"
            );
        }

        if (request.email() == null ||
                request.email().isBlank() ||
                request.password() == null) {
            throw new IllegalArgumentException(
                    "Email and password are required"
            );
        }

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Find organisation in the master database
        Organisation organisation = organisationRepository
                .findById(request.organisationId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid login credentials"
                        )
                );

        if (!"ACTIVE".equalsIgnoreCase(
                organisation.getDatabaseStatus()
        )) {
            throw new IllegalArgumentException(
                    "Organisation database is not active"
            );
        }

        if (organisation.getDatabaseName() == null ||
                organisation.getDatabaseName().isBlank()) {
            throw new IllegalStateException(
                    "Organisation database is not configured"
            );
        }

        // Connect to the organisation's dedicated database
        MongoTemplate tenantTemplate = new MongoTemplate(
                mongoClient,
                organisation.getDatabaseName()
        );

        // Find the user inside that organisation's database
        Query query = Query.query(
                Criteria.where("email").is(email)
                        .and("organisationId").is(organisation.getId())
        );

        UserAccount user = tenantTemplate.findOne(
                query,
                UserAccount.class
        );

        if (user == null ||
                !user.isEnabled() ||
                !passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash()
                )) {
            throw new IllegalArgumentException(
                    "Invalid login credentials"
            );
        }

        return createAuthResponse(user, organisation);
    }

    // Generate authentication response and JWT
    private AuthResponse createAuthResponse(
            UserAccount user,
            Organisation organisation
    ) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(8, ChronoUnit.HOURS);

        String department = user.getDepartment() == null
                ? ""
                : user.getDepartment();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("staff-management")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("organisationId", user.getOrganisationId())
                .claim("department", department)
                .claim("role", user.getRole())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                department,
                organisation.getId(),
                organisation.getName(),
                expiresAt
        );
    }

    // Validate registration credentials
    private void validateCredentials(String email, String password) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            throw new IllegalArgumentException(
                    "Please provide a valid email address"
            );
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters"
            );
        }
    }

    public record RegisterRequest(
            String organisationName,
            String organisationType,
            String description,
            String email,
            String password
    ) {
    }

    public record LoginRequest(
            String organisationId,
            String email,
            String password
    ) {
    }

    public record AuthResponse(
            String token,
            String userId,
            String email,
            String role,
            String department,
            String organisationId,
            String organisationName,
            Instant expiresAt
    ) {
    }
}
