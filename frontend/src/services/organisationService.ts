import type { Organisation } from '../types/Organisation';

const API_URL = 'http://localhost:8080/api/organisations';

export async function getAllOrganisations(): Promise<Organisation[]> {
    const response = await fetch(API_URL);

    if (!response.ok) {
        throw new Error('Failed to fetch organisations');
    }
    return response.json();
}

export async function addOrganisation(
    organisation: Organisation
): Promise<Organisation> {
    const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(organisation)
    });

    if (!response.ok) {
        throw new Error('Failed to add organisation');
    }

    return response.json();
}

export async function updateOrganisation(
    id: string,
    organisation: Organisation
): Promise<Organisation> {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(organisation)
    });

    if (!response.ok) {
        throw new Error('Failed to update organisation');
    }

    return response.json();
}

export async function deleteOrganisation(id: string): Promise<void> {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'DELETE'
    });

    if (!response.ok) {
        throw new Error('Failed to delete organisation');
    }
}