package com.org.staffmanagement.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class FirstLoginPasswordChangeFilter extends OncePerRequestFilter {

    private static final String CHANGE_PASSWORD_PATH =
            "/api/auth/change-password";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {

            Boolean mustChangePassword =
                    jwtAuthentication.getToken().getClaim(
                            "mustChangePassword"
                    );

            boolean isPasswordChangeRequest =
                    "POST".equalsIgnoreCase(request.getMethod()) &&
                            CHANGE_PASSWORD_PATH.equals(request.getServletPath());

            if (Boolean.TRUE.equals(mustChangePassword) &&
                    !isPasswordChangeRequest) {

                response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN
                );

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(
                        "{\"message\":\"You must change your temporary password before continuing.\"}"
                );

                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
