import type {
    LeaveRequest,
    LeaveBalance,
    CreateLeaveRequest
} from '../types/Leave';

import { getToken } from './authService';

const API_URL = 'http://localhost:8080/api/leaves';

function getHeaders(json = false): HeadersInit {
    const token = getToken();

    return {
        ...(json ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };
}

async function handleError(
    response: Response,
    fallback: string
): Promise<never> {
    const message = await response.text();
    throw new Error(message || fallback);
}

export async function submitLeaveRequest(
    request: CreateLeaveRequest
): Promise<LeaveRequest> {
    const response = await fetch(API_URL, {
        method: 'POST',
        headers: getHeaders(true),
        body: JSON.stringify(request)
    });

    if (!response.ok) {
        await handleError(response, 'Failed to submit leave request.');
    }

    return response.json();
}

export async function getMyLeaveRequests(): Promise<LeaveRequest[]> {
    const response = await fetch(`${API_URL}/my-requests`, {
        headers: getHeaders()
    });

    if (!response.ok) {
        await handleError(response, 'Failed to fetch leave requests.');
    }

    return response.json();
}

export async function getMyLeaveBalance(): Promise<LeaveBalance> {
    const response = await fetch(`${API_URL}/my-balance`, {
        headers: getHeaders()
    });

    if (!response.ok) {
        await handleError(response, 'Failed to fetch leave balance.');
    }

    return response.json();
}

export async function getReviewableLeaveRequests(): Promise<LeaveRequest[]> {
    const response = await fetch(`${API_URL}/review`, {
        headers: getHeaders()
    });

    if (!response.ok) {
        await handleError(response, 'Failed to fetch requests for review.');
    }

    return response.json();
}

export async function approveLeaveRequest(
    id: string,
    comment?: string
): Promise<LeaveRequest> {
    const response = await fetch(`${API_URL}/${encodeURIComponent(id)}/approve`, {
        method: 'PUT',
        headers: getHeaders(true),
        body: JSON.stringify({ comment: comment ?? '' })
    });

    if (!response.ok) {
        await handleError(response, 'Failed to approve leave request.');
    }

    return response.json();
}

export async function rejectLeaveRequest(
    id: string,
    comment?: string
): Promise<LeaveRequest> {
    const response = await fetch(`${API_URL}/${encodeURIComponent(id)}/reject`, {
        method: 'PUT',
        headers: getHeaders(true),
        body: JSON.stringify({ comment: comment ?? '' })
    });

    if (!response.ok) {
        await handleError(response, 'Failed to reject leave request.');
    }

    return response.json();
}
