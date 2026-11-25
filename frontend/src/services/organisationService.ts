import type { Organisation } from '../types/Organisation';
import { getToken } from './authService';

const API_URL = 'http://localhost:8080/api/organisations';

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

export async function getAllOrganisations(): Promise<Organisation[]> {
    const response = await fetch(API_URL, {
        headers: getHeaders()
    });

    if (!response.ok) {
        await handleError(response, 'Failed to fetch organisations.');
    }

    return response.json();
}

export async function addOrganisation(
    _organisation: Organisation
): Promise<Organisation> {
    throw new Error(
        'Organisation registration must use the authentication service.'
    );
}

export async function updateOrganisation(
    id: string,
    organisation: Organisation
): Promise<Organisation> {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'PUT',
        headers: getHeaders(true),
        body: JSON.stringify(organisation)
    });

    if (!response.ok) {
        await handleError(response, 'Failed to update organisation.');
    }

    return response.json();
}

export async function deleteOrganisation(id: string): Promise<void> {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'DELETE',
        headers: getHeaders()
    });

    if (!response.ok) {
        await handleError(response, 'Failed to delete organisation.');
    }
}
