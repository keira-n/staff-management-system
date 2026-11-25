package com.org.staffmanagement.controller;

import com.org.staffmanagement.models.LeaveBalance;
import com.org.staffmanagement.models.Staff;
import com.org.staffmanagement.repository.LeaveBalanceRepository;
import com.org.staffmanagement.repository.StaffRepository;
import com.org.staffmanagement.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves/balances")
public class LeaveBalanceController {

    private final LeaveBalanceRepository balanceRepository;
    private final StaffRepository staffRepository;
    private final CurrentUserService currentUserService;

    public LeaveBalanceController(
            LeaveBalanceRepository balanceRepository,
            StaffRepository staffRepository,
            CurrentUserService currentUserService
    ) {
        this.balanceRepository = balanceRepository;
        this.staffRepository = staffRepository;
        this.currentUserService = currentUserService;
    }

    // Get all employee leave balances in the organisation
    @GetMapping
    public ResponseEntity<List<LeaveBalance>> getAllBalances() {
        requireAdminOrHR();

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        List<LeaveBalance> balances =
                balanceRepository.findByOrganisationId(organisationId);

        return ResponseEntity.ok(balances);
    }

    // Update an employee's annual leave allowances
    @PutMapping("/{staffId}")
    public ResponseEntity<LeaveBalance> updateBalance(
            @PathVariable String staffId,
            @RequestBody UpdateBalanceRequest request
    ) {
        requireAdminOrHR();

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        if (request.annualSickLeave() < 0
                || request.annualPaidVacation() < 0) {
            throw new IllegalArgumentException(
                    "Leave allowances cannot be negative"
            );
        }

        Staff staff = staffRepository
                .findById(staffId)
                .filter(existingStaff ->
                        organisationId.equals(
                                existingStaff.getOrganisationId()
                        )
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found in this organisation"
                        )
                );

        LeaveBalance balance = balanceRepository
                .findByStaffIdAndOrganisationId(
                        staffId,
                        organisationId
                )
                .orElseGet(() -> {
                    LeaveBalance newBalance = new LeaveBalance();
                    newBalance.setOrganisationId(organisationId);
                    newBalance.setStaffId(staff.getId());
                    newBalance.setEmployeeEmail(staff.getEmail());
                    return newBalance;
                });

        if (request.annualSickLeave() < balance.getUsedSickLeave()) {
            throw new IllegalArgumentException(
                    "Sick leave allowance cannot be less than leave already used"
            );
        }

        if (request.annualPaidVacation()
                < balance.getUsedPaidVacation()) {
            throw new IllegalArgumentException(
                    "Paid vacation allowance cannot be less than leave already used"
            );
        }

        balance.setAnnualSickLeave(request.annualSickLeave());
        balance.setAnnualPaidVacation(request.annualPaidVacation());

        return ResponseEntity.ok(
                balanceRepository.save(balance)
        );
    }

    // Only ADMIN and HR department users can manage leave allowances
    private void requireAdminOrHR() {
        String role = currentUserService.getCurrentUserRole();
        String department = currentUserService.getCurrentUserDepartment();

        boolean isAdmin = role != null
                && role.equalsIgnoreCase("ADMIN");

        boolean isHR = department != null
                && department.equalsIgnoreCase("HR");

        if (!isAdmin && !isHR) {
            throw new AccessDeniedException(
                    "Only administrators and HR can manage leave balances"
            );
        }
    }

    public record UpdateBalanceRequest(
            int annualSickLeave,
            int annualPaidVacation
    ) {
    }
}
