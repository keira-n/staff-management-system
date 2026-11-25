package com.org.staffmanagement.service;

import com.org.staffmanagement.models.LeaveBalance;
import com.org.staffmanagement.models.LeaveRequest;
import com.org.staffmanagement.models.Staff;
import com.org.staffmanagement.repository.LeaveBalanceRepository;
import com.org.staffmanagement.repository.LeaveRequestRepository;
import com.org.staffmanagement.repository.StaffRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final StaffRepository staffRepository;
    private final CurrentUserService currentUserService;

    public LeaveService(
            LeaveRequestRepository leaveRequestRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            StaffRepository staffRepository,
            CurrentUserService currentUserService
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.staffRepository = staffRepository;
        this.currentUserService = currentUserService;
    }

    // Submit a leave request for the authenticated employee
    public LeaveRequest submitLeaveRequest(LeaveRequest request) {

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        String email =
                currentUserService.getCurrentUserEmail();

        Staff staff = staffRepository
                .findByEmailAndOrganisationId(email, organisationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No staff profile found for your account"
                        )
                );

        validateLeaveRequest(request);

        int totalDays = calculateLeaveDays(
                request.getStartDate(),
                request.getEndDate()
        );

        LeaveBalance balance = getOrCreateBalance(staff, organisationId);

        checkAvailableBalance(
                balance,
                request.getLeaveType(),
                totalDays
        );

        request.setId(null);
        request.setOrganisationId(organisationId);
        request.setStaffId(staff.getId());
        request.setEmployeeEmail(staff.getEmail());
        request.setEmployeeName(staff.getName());
        request.setDepartment(staff.getDepartment());
        request.setTotalDays(totalDays);
        request.setStatus("PENDING");
        request.setSubmittedAt(Instant.now());
        request.setReviewedBy(null);
        request.setReviewedAt(null);
        request.setReviewComment(null);

        return leaveRequestRepository.save(request);
    }

    // Get the authenticated employee's leave requests
    public List<LeaveRequest> getMyLeaveRequests() {

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        String email =
                currentUserService.getCurrentUserEmail();

        return leaveRequestRepository
                .findByOrganisationIdAndEmployeeEmailOrderBySubmittedAtDesc(
                        organisationId,
                        email
                );
    }

    // Get the authenticated employee's leave balance
    public LeaveBalance getMyLeaveBalance() {

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        String email =
                currentUserService.getCurrentUserEmail();

        Staff staff = staffRepository
                .findByEmailAndOrganisationId(email, organisationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No staff profile found for your account"
                        )
                );

        return getOrCreateBalance(staff, organisationId);
    }

    // Get leave requests the current user is authorised to review
    public List<LeaveRequest> getReviewableLeaveRequests() {

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        String role = currentUserService.getCurrentUserRole();
        String department = currentUserService.getCurrentUserDepartment();

        if (isAdmin(role) || isHR(department)) {
            return leaveRequestRepository
                    .findByOrganisationIdOrderBySubmittedAtDesc(
                            organisationId
                    );
        }

        if (isManager(role)) {
            return leaveRequestRepository
                    .findByOrganisationIdAndDepartmentOrderBySubmittedAtDesc(
                            organisationId,
                            department
                    );
        }

        throw new AccessDeniedException(
                "You do not have permission to review leave requests"
        );
    }

    // Approve a leave request
    public LeaveRequest approveLeaveRequest(
            String requestId,
            String reviewComment
    ) {
        return reviewLeaveRequest(
                requestId,
                "APPROVED",
                reviewComment
        );
    }

    // Reject a leave request
    public LeaveRequest rejectLeaveRequest(
            String requestId,
            String reviewComment
    ) {
        return reviewLeaveRequest(
                requestId,
                "REJECTED",
                reviewComment
        );
    }

    private LeaveRequest reviewLeaveRequest(
            String requestId,
            String newStatus,
            String reviewComment
    ) {

        String organisationId =
                currentUserService.getCurrentOrganisationId();

        String role = currentUserService.getCurrentUserRole();
        String department = currentUserService.getCurrentUserDepartment();

        if (!isAdmin(role) && !isHR(department) && !isManager(role)) {
            throw new AccessDeniedException(
                    "You do not have permission to review leave requests"
            );
        }

        LeaveRequest request = leaveRequestRepository
                .findByIdAndOrganisationId(requestId, organisationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Leave request not found"
                        )
                );

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException(
                    "This leave request has already been reviewed"
            );
        }

        if (isManager(role)
                && !isHR(department)
                && !sameDepartment(department, request.getDepartment())) {
            throw new AccessDeniedException(
                    "You can only review requests from your department"
            );
        }

        if ("APPROVED".equals(newStatus)) {
            Staff staff = staffRepository
                    .findById(request.getStaffId())
                    .filter(existingStaff ->
                            organisationId.equals(
                                    existingStaff.getOrganisationId()
                            )
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Employee record not found"
                            )
                    );

            LeaveBalance balance =
                    getOrCreateBalance(staff, organisationId);

            checkAvailableBalance(
                    balance,
                    request.getLeaveType(),
                    request.getTotalDays()
            );

            deductLeave(
                    balance,
                    request.getLeaveType(),
                    request.getTotalDays()
            );

            leaveBalanceRepository.save(balance);
        }

        request.setStatus(newStatus);
        request.setReviewedBy(currentUserService.getCurrentUserId());
        request.setReviewedAt(Instant.now());
        request.setReviewComment(reviewComment);

        return leaveRequestRepository.save(request);
    }

    // Find an existing balance or create one with default allowances
    private LeaveBalance getOrCreateBalance(
            Staff staff,
            String organisationId
    ) {

        return leaveBalanceRepository
                .findByStaffIdAndOrganisationId(
                        staff.getId(),
                        organisationId
                )
                .orElseGet(() -> {

                    LeaveBalance balance = new LeaveBalance();

                    balance.setOrganisationId(organisationId);
                    balance.setStaffId(staff.getId());
                    balance.setEmployeeEmail(staff.getEmail());

                    return leaveBalanceRepository.save(balance);
                });
    }

    // Validate request dates and leave type
    private void validateLeaveRequest(LeaveRequest request) {

        if (request.getLeaveType() == null
                || (!request.getLeaveType().equalsIgnoreCase("SICK_LEAVE")
                && !request.getLeaveType().equalsIgnoreCase("PAID_VACATION"))) {
            throw new IllegalArgumentException(
                    "Leave type must be SICK_LEAVE or PAID_VACATION"
            );
        }

        if (request.getStartDate() == null
                || request.getEndDate() == null) {
            throw new IllegalArgumentException(
                    "Leave start date and end date are required"
            );
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Leave start date cannot be in the past"
            );
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException(
                    "Leave end date cannot be before start date"
            );
        }

        if (request.getReason() == null
                || request.getReason().isBlank()) {
            throw new IllegalArgumentException(
                    "Leave reason is required"
            );
        }
    }

    // Calculate leave days, excluding weekends
    private int calculateLeaveDays(
            LocalDate startDate,
            LocalDate endDate
    ) {

        int totalDays = 0;

        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {

            if (date.getDayOfWeek().getValue() <= 5) {
                totalDays++;
            }

            date = date.plusDays(1);
        }

        if (totalDays == 0) {
            throw new IllegalArgumentException(
                    "Leave request must include at least one working day"
            );
        }

        return totalDays;
    }

    // Check whether the employee has enough remaining leave
    private void checkAvailableBalance(
            LeaveBalance balance,
            String leaveType,
            int requestedDays
    ) {

        if ("SICK_LEAVE".equalsIgnoreCase(leaveType)
                && balance.getRemainingSickLeave() < requestedDays) {
            throw new IllegalArgumentException(
                    "Insufficient sick leave balance"
            );
        }

        if ("PAID_VACATION".equalsIgnoreCase(leaveType)
                && balance.getRemainingPaidVacation() < requestedDays) {
            throw new IllegalArgumentException(
                    "Insufficient paid vacation balance"
            );
        }
    }

    // Deduct approved leave
    private void deductLeave(
            LeaveBalance balance,
            String leaveType,
            int requestedDays
    ) {

        if ("SICK_LEAVE".equalsIgnoreCase(leaveType)) {
            balance.setUsedSickLeave(
                    balance.getUsedSickLeave() + requestedDays
            );
        }

        if ("PAID_VACATION".equalsIgnoreCase(leaveType)) {
            balance.setUsedPaidVacation(
                    balance.getUsedPaidVacation() + requestedDays
            );
        }
    }

    private boolean isAdmin(String role) {
        return role != null && role.equalsIgnoreCase("ADMIN");
    }

    private boolean isManager(String role) {
        return role != null && role.equalsIgnoreCase("MANAGER");
    }

    private boolean isHR(String department) {
        return department != null
                && department.equalsIgnoreCase("HR");
    }

    private boolean sameDepartment(
            String first,
            String second
    ) {
        return first != null
                && second != null
                && first.equalsIgnoreCase(second);
    }
}
