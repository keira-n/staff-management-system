const API_URL = 'http://localhost:8080/api/auth';

export interface AuthResponse {
    token: string;
    userId: string;
    email: string;
    role: string;
    department: string;
    organisationId: string;
    organisationName: string;
    expiresAt: string;
    mustChangePassword: boolean;
}

export interface RegisterRequest {
    organisationName: string;
    organisationType: string;
    description: string;
    email: string;
    password: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
}

async function getErrorMessage(
    response: Response,
    fallback: string
): Promise<string> {
    try {
        const result = await response.json();

        if (result.message) {
            return result.message;
        }
    } catch {
        // Response was not JSON
    }

    return fallback;
}

export async function registerOrganisation(
    data: RegisterRequest
): Promise<AuthResponse> {
    const response = await fetch(`${API_URL}/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    if (!response.ok) {
        throw new Error(
            await getErrorMessage(response, 'Registration failed.')
        );
    }

    const result: AuthResponse = await response.json();

    saveSession(result);

    return result;
}

export async function login(
    data: LoginRequest
): Promise<AuthResponse> {
    const response = await fetch(`${API_URL}/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    if (!response.ok) {
        throw new Error(
            await getErrorMessage(
                response,
                'Invalid email or password.'
            )
        );
    }

    const result: AuthResponse = await response.json();

    saveSession(result);

    return result;
}

export async function changePassword(
    data: ChangePasswordRequest
): Promise<AuthResponse> {
    const token = getToken();

    if (!token) {
        throw new Error('You must be logged in to change your password.');
    }

    const response = await fetch(`${API_URL}/change-password`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(data)
    });

    if (!response.ok) {
        throw new Error(
            await getErrorMessage(
                response,
                'Password change failed.'
            )
        );
    }

    const result: AuthResponse = await response.json();

    // Replace the temporary-password token with the new token
    saveSession(result);

    return result;
}

export function saveSession(auth: AuthResponse): void {
    localStorage.setItem('staffManagementToken', auth.token);

    localStorage.setItem('staffManagementUser', JSON.stringify({
        userId: auth.userId,
        email: auth.email,
        role: auth.role,
        department: auth.department,
        organisationId: auth.organisationId,
        organisationName: auth.organisationName,
        mustChangePassword: auth.mustChangePassword
    }));
}

export function getToken(): string | null {
    return localStorage.getItem('staffManagementToken');
}

export function getCurrentUser(): Omit<AuthResponse, 'token' | 'expiresAt'> | null {
    const user = localStorage.getItem('staffManagementUser');

    if (!user) {
        return null;
    }

    try {
        return JSON.parse(user);
    } catch {
        logout();
        return null;
    }
}

export function logout(): void {
    localStorage.removeItem('staffManagementToken');
    localStorage.removeItem('staffManagementUser');
}

export function isAuthenticated(): boolean {
    return getToken() !== null;
}

export function mustChangePassword(): boolean {
    return getCurrentUser()?.mustChangePassword === true;
}
