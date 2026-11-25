package com.org.staffmanagement.service;

import com.mongodb.client.MongoClient;
import com.org.staffmanagement.models.CentralUserAccount;
import com.org.staffmanagement.models.Organisation;
import com.org.staffmanagement.models.UserAccount;
import com.org.staffmanagement.repository.master.CentralUserAccountRepository;
import com.org.staffmanagement.repository.master.OrganisationRepository;

import org.springframework.dao.DuplicateKeyException;
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
    private final CentralUserAccountRepository centralUserAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthenticationService(
            MongoClient mongoClient,
            OrganisationRepository organisationRepository,
            CentralUserAccountRepository centralUserAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder
    ) {
        this.mongoClient = mongoClient;
        this.organisationRepository = organisationRepository;
        this.centralUserAccountRepository = centralUserAccountRepository;
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

        String email = normaliseEmail(request.email());

        // Check whether the email is already registered globally
        if (centralUserAccountRepository.existsByNormalisedEmail(email)) {
            throw new IllegalArgumentException(
                    "This email address is already registered"
            );
        }

        Organisation organisation = new Organisation(
                request.organisationName().trim(),
                request.organisationType().trim(),
                request.description()
        );

        Organisation savedOrganisation =
                organisationRepository.save(organisation);

        String databaseName =
                "staff_org_" + savedOrganisation.getId();

        savedOrganisation.setDatabaseName(databaseName);
        savedOrganisation.setDatabaseStatus("PROVISIONING");

        organisationRepository.save(savedOrganisation);

        MongoTemplate tenantTemplate =
                new MongoTemplate(mongoClient, databaseName);

        UserAccount savedUser = null;

        try {

            // Create the initial administrator in the tenant database
            UserAccount admin = new UserAccount(
                    email,
                    passwordEncoder.encode(request.password()),
                    savedOrganisation.getId(),
                    "ADMIN"
            );

            savedUser = tenantTemplate.save(admin);

            // Register the account in the central master database
            CentralUserAccount centralAccount =
                    new CentralUserAccount(
                            email,
                            savedOrganisation.getId(),
                            databaseName,
                            savedUser.getId()
                    );

            centralUserAccountRepository.save(centralAccount);

            // Activate the organisation after successful provisioning
            savedOrganisation.setDatabaseStatus("ACTIVE");
            organisationRepository.save(savedOrganisation);

        } catch (DuplicateKeyException exception) {

            // Remove the tenant account if central registration failed
            if (savedUser != null) {
                tenantTemplate.remove(
                        Query.query(
                                Criteria.where("_id").is(savedUser.getId())
                        ),
                        UserAccount.class
                );
            }

            savedOrganisation.setDatabaseStatus("FAILED");
            organisationRepository.save(savedOrganisation);

            throw new IllegalArgumentException(
                    "This email address is already registered"
            );

        } catch (RuntimeException exception) {

            savedOrganisation.setDatabaseStatus("FAILED");
            organisationRepository.save(savedOrganisation);

            throw new IllegalStateException(
                    "Organisation database provisioning failed.",
                    exception
            );
        }

        return createAuthResponse(savedUser, savedOrganisation);
    }

    // Authenticate using the central account directory
    public AuthResponse login(LoginRequest request) {

        if (request.email() == null ||
                request.email().isBlank() ||
                request.password() == null ||
                request.password().isBlank()) {
            throw new IllegalArgumentException(
                    "Email and password are required"
            );
        }

        String email = normaliseEmail(request.email());

        // Find the account in the central master database
        CentralUserAccount centralAccount =
                centralUserAccountRepository
                        .findByNormalisedEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid login credentials"
                                )
                        );

        if (!centralAccount.isEnabled()) {
            throw new IllegalArgumentException(
                    "Invalid login credentials"
            );
        }

        // Identify the employee's organisation
        Organisation organisation =
                organisationRepository.findById(
                        centralAccount.getOrganisationId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid login credentials"
                        )
                );

        // Ensure the organisation is active
        if (!"ACTIVE".equalsIgnoreCase(
                organisation.getDatabaseStatus()
        )) {
            throw new IllegalArgumentException(
                    "Invalid login credentials"
            );
        }

        // Verify that the directory points to the correct database
        if (organisation.getDatabaseName() == null ||
                !organisation.getDatabaseName().equals(
                        centralAccount.getDatabaseName()
                )) {
            throw new IllegalArgumentException(
                    "Invalid login credentials"
            );
        }

        // Connect only to the employee's organisation database
        MongoTemplate tenantTemplate = new MongoTemplate(
                mongoClient,
                centralAccount.getDatabaseName()
        );

        // Retrieve the exact account identified by the directory
        UserAccount user = tenantTemplate.findById(
                centralAccount.getUserId(),
                UserAccount.class
        );

        if (user == null ||
                !email.equals(normaliseEmail(user.getEmail())) ||
                !organisation.getId().equals(user.getOrganisationId()) ||
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

    // Normalise email addresses consistently
    private String normaliseEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
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