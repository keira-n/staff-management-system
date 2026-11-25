import { getToken } from './services/authService';

const API_URL = 'http://localhost:8080/api/staff';

async function authenticatedFetch(
    url: string,
    options: RequestInit = {}
): Promise<Response> {
    const token = getToken();

    if (!token) {
        throw new Error('You must be logged in to perform this action.');
    }

    const response = await fetch(url, {
        ...options,
        headers: {
            ...options.headers,
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (response.status === 401) {
        throw new Error('Your session has expired. Please log in again.');
    }

    if (response.status === 403) {
        throw new Error('You do not have permission to perform this action.');
    }

    return response;
}

export async function getStaff() {
    const response = await authenticatedFetch(API_URL, {
        method: 'GET'
    });

    if (!response.ok) {
        throw new Error('Failed to fetch staff.');
    }

    return response.json();
}

export async function addStaff(staff: any) {
    const response = await authenticatedFetch(API_URL, {
        method: 'POST',
        body: JSON.stringify(staff)
    });

    if (!response.ok) {
        throw new Error('Failed to add staff.');
    }

    return response.json();
}

export async function updateStaff(id: string, staff: any) {
    const response = await authenticatedFetch(`${API_URL}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(staff)
    });

    if (!response.ok) {
        throw new Error('Failed to update staff.');
    }

    return response.json();
}

export async function deleteStaff(id: string) {
    const response = await authenticatedFetch(`${API_URL}/${id}`, {
        method: 'DELETE'
    });

    if (!response.ok) {
        throw new Error('Failed to delete staff.');
    }
}