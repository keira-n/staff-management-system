import type { Staff } from '../types/Staff';
import { getToken } from './authService';

const API_URL = 'http://localhost:8080/api/staff';

export interface StaffCreationResponse {
    staff: Staff;
    temporaryPassword: string;
}

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

export async function getAllStaff(
    organisationType?: string,
    _organisationId?: string
): Promise<Staff[]> {
    const params = new URLSearchParams();

    if (organisationType) {
        params.append('organisationType', organisationType);
    }

    const query = params.toString();
    const url = query ? `${API_URL}?${query}` : API_URL;

    const response = await fetch(url, {
        headers: getHeaders()
    });

    if (!response.ok) {
        await handleError(response, 'Failed to fetch staff.');
    }

    return response.json();
}

export async function addStaff(
    staff: Staff
): Promise<StaffCreationResponse> {
    const response = await fetch(API_URL, {
        method: 'POST',
        headers: getHeaders(true),
        body: JSON.stringify(staff)
    });

    if (!response.ok) {
        await handleError(response, 'Failed to add staff.');
    }

    return response.json();
}

export async function updateStaff(
    id: string,
    staff: Staff
): Promise<Staff> {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'PUT',
        headers: getHeaders(true),
        body: JSON.stringify(staff)
    });

    if (!response.ok) {
        await handleError(response, 'Failed to update staff.');
    }

    return response.json();
}

export async function deleteStaff(id: string): Promise<void> {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'DELETE',
        headers: getHeaders()
    });

    if (!response.ok) {
        await handleError(response, 'Failed to delete staff.');
    }
}
