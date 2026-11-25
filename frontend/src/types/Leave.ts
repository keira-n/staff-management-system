export type LeaveType = 'SICK_LEAVE' | 'PAID_VACATION';

export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface LeaveRequest {
    id?: string;
    organisationId?: string;

    staffId: string;
    employeeEmail: string;
    employeeName: string;
    department?: string;

    leaveType: LeaveType;
    startDate: string;
    endDate: string;
    totalDays: number;
    reason: string;

    status: LeaveStatus;
    submittedAt?: string;

    reviewedBy?: string;
    reviewedAt?: string;
    reviewComment?: string;
}

export interface LeaveBalance {
    id?: string;
    organisationId?: string;

    staffId: string;
    employeeEmail: string;

    annualSickLeave: number;
    usedSickLeave: number;
    remainingSickLeave: number;

    annualPaidVacation: number;
    usedPaidVacation: number;
    remainingPaidVacation: number;
}

export interface CreateLeaveRequest {
    leaveType: LeaveType;
    startDate: string;
    endDate: string;
    reason: string;
}

export interface ReviewLeaveRequest {
    status: 'APPROVED' | 'REJECTED';
    reviewComment?: string;
}
