package com.org.staffmanagement.controller;

import com.org.staffmanagement.models.StaffContext;
import com.org.staffmanagement.repository.StaffContextRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contexts")
@CrossOrigin(origins = "*")
public class StaffContextController {

    private final StaffContextRepository repository;

    public StaffContextController(StaffContextRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<StaffContext> getAllContexts() {
        return repository.findAll();
    }

    @GetMapping("/{key}")
    public ResponseEntity<StaffContext> getContext(@PathVariable String key) {
        return repository.findByKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public StaffContext createContext(@RequestBody StaffContext context) {
        return repository.save(context);
    }

    @PutMapping("/{key}")
    public ResponseEntity<StaffContext> updateContext(
            @PathVariable String key,
            @RequestBody StaffContext updatedContext) {

        return repository.findByKey(key).map(existing -> {
            existing.setName(updatedContext.getName());
            existing.setDescription(updatedContext.getDescription());
            existing.setFields(updatedContext.getFields());

            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteContext(@PathVariable String key) {
        return repository.findByKey(key).map(context -> {
            repository.delete(context);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
