import type { Staff } from '../types/Staff';

const API_URL = 'http://localhost:8080/api/staff';

export async function getAllStaff(
    organisationType?: string,
    organisationId?: string
): Promise<Staff[]> {
    const params = new URLSearchParams();

    if (organisationType) {
        params.append('organizationType', organisationType);
    }

    if (organisationId) {
        params.append('organisationId', organisationId);
    }

    const query = params.toString();
    const url = query ? `${API_URL}?${query}` : API_URL;

    const response = await fetch(url);

    if (!response.ok) {
        throw new Error('Failed to fetch staff');
    }

    return response.json();
}

export async function addStaff(staff: Staff): Promise<Staff> {
    const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(staff)
    });

    if (!response.ok) {
        throw new Error('Failed to add staff');
    }

    return response.json();
}

export async function updateStaff(
    id: string,
    staff: Staff
): Promise<Staff> {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(staff)
    });

    if (!response.ok) {
        throw new Error('Failed to update staff');
    }

    return response.json();
}

export async function deleteStaff(id: string): Promise<void> {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'DELETE'
    });

    if (!response.ok) {
        throw new Error('Failed to delete staff');
    }
}