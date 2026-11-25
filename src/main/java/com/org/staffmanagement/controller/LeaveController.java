package com.org.staffmanagement.controller;

import com.org.staffmanagement.models.LeaveBalance;
import com.org.staffmanagement.models.LeaveRequest;
import com.org.staffmanagement.service.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // Submit a leave request
    @PostMapping
    public ResponseEntity<LeaveRequest> submitLeaveRequest(
            @RequestBody LeaveRequest request
    ) {
        LeaveRequest savedRequest =
                leaveService.submitLeaveRequest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedRequest);
    }

    // Get the authenticated employee's leave requests
    @GetMapping("/my-requests")
    public ResponseEntity<List<LeaveRequest>> getMyLeaveRequests() {

        return ResponseEntity.ok(
                leaveService.getMyLeaveRequests()
        );
    }

    // Get the authenticated employee's leave balance
    @GetMapping("/my-balance")
    public ResponseEntity<LeaveBalance> getMyLeaveBalance() {

        return ResponseEntity.ok(
                leaveService.getMyLeaveBalance()
        );
    }

    // Get leave requests the current user can review
    @GetMapping("/review")
    public ResponseEntity<List<LeaveRequest>> getReviewableLeaveRequests() {

        return ResponseEntity.ok(
                leaveService.getReviewableLeaveRequests()
        );
    }

    // Approve a leave request
    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeaveRequest(
            @PathVariable String id,
            @RequestBody(required = false) ReviewRequest request
    ) {
        String comment = request == null ? null : request.comment();

        return ResponseEntity.ok(
                leaveService.approveLeaveRequest(id, comment)
        );
    }

    // Reject a leave request
    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeaveRequest(
            @PathVariable String id,
            @RequestBody(required = false) ReviewRequest request
    ) {
        String comment = request == null ? null : request.comment();

        return ResponseEntity.ok(
                leaveService.rejectLeaveRequest(id, comment)
        );
    }

    public record ReviewRequest(String comment) {
    }
}
