package com.org.staffmanagement.controller;

import com.org.staffmanagement.models.StaffContext;
import com.org.staffmanagement.repository.StaffContextRepository;
import com.org.staffmanagement.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contexts")
public class StaffContextController {

    private final StaffContextRepository repository;
    private final CurrentUserService currentUserService;

    public StaffContextController(
            StaffContextRepository repository,
            CurrentUserService currentUserService
    ) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<StaffContext> getAllContexts() {
        String organisationId =
                currentUserService.getCurrentOrganisationId();

        return repository.findByOrganisationId(organisationId);
    }

    @GetMapping("/{key}")
    public ResponseEntity<StaffContext> getContext(
            @PathVariable String key
    ) {
        String organisationId =
                currentUserService.getCurrentOrganisationId();

        return repository.findByKeyAndOrganisationId(
                        key,
                        organisationId
                ).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StaffContext> createContext(
            @RequestBody StaffContext context
    ) {
        requireAdmin();

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        if (repository.existsByKeyAndOrganisationId(
                context.getKey(),
                organisationId
        )) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        context.setId(null);
        context.setOrganisationId(organisationId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repository.save(context));
    }

    @PutMapping("/{key}")
    public ResponseEntity<StaffContext> updateContext(
            @PathVariable String key,
            @RequestBody StaffContext updatedContext
    ) {
        requireAdmin();

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        return repository.findByKeyAndOrganisationId(
                key,
                organisationId
        ).map(existing -> {
            existing.setName(updatedContext.getName());
            existing.setDescription(updatedContext.getDescription());
            existing.setFields(updatedContext.getFields());

            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteContext(
            @PathVariable String key
    ) {
        requireAdmin();

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        return repository.findByKeyAndOrganisationId(
                key,
                organisationId
        ).map(context -> {
            repository.delete(context);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private void requireAdmin() {
        if (!"ADMIN".equals(
                currentUserService.getCurrentUserRole()
        )) {
            throw new AccessDeniedException(
                    "Only organisation administrators can manage contexts."
            );
        }
    }
}
