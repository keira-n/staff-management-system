package com.org.staffmanagement.controller;

import com.org.staffmanagement.service.AuthenticationService;
import com.org.staffmanagement.service.AuthenticationService.AuthResponse;
import com.org.staffmanagement.service.AuthenticationService.ChangePasswordRequest;
import com.org.staffmanagement.service.AuthenticationService.LoginRequest;
import com.org.staffmanagement.service.AuthenticationService.RegisterRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(
            AuthenticationService authenticationService
    ) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ) {
        try {
            AuthResponse response =
                    authenticationService.registerOrganisation(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(exception.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {
        try {
            AuthResponse response =
                    authenticationService.login(request);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid login credentials"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request
    ) {
        try {
            AuthResponse response =
                    authenticationService.changePassword(request);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(exception.getMessage()));
        }
    }

    public record ErrorResponse(String message) {
    }
}
