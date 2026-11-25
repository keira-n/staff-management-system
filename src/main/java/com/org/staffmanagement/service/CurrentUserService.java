package com.org.staffmanagement.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private JwtAuthenticationToken getJwtAuthentication() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new AccessDeniedException("Authentication required");
        }

        return jwtAuthentication;
    }

    public String getCurrentOrganisationId() {

        String organisationId = getJwtAuthentication()
                .getToken()
                .getClaimAsString("organisationId");

        if (organisationId == null || organisationId.isBlank()) {
            throw new AccessDeniedException(
                    "No organisation is associated with this account"
            );
        }

        return organisationId;
    }

    public String getCurrentUserId() {

        String userId = getJwtAuthentication()
                .getToken()
                .getClaimAsString("userId");

        if (userId == null || userId.isBlank()) {
            throw new AccessDeniedException(
                    "User identity could not be verified"
            );
        }

        return userId;
    }

    public String getCurrentUserEmail() {

        String email = getJwtAuthentication()
                .getToken()
                .getSubject();

        if (email == null || email.isBlank()) {
            throw new AccessDeniedException(
                    "User email could not be verified"
            );
        }

        return email;
    }

    public String getCurrentUserRole() {

        return getJwtAuthentication()
                .getToken()
                .getClaimAsString("role");
    }

    public String getCurrentUserDepartment() {

        return getJwtAuthentication()
                .getToken()
                .getClaimAsString("department");
    }
}
