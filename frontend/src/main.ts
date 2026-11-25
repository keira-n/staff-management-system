import './styles/global.css';
import './styles/auth.css';
import './styles/header.css';
import './styles/staff.css';
import './styles/organisations.css';
import './styles/leave.css';

import {
    isAuthenticated,
    mustChangePassword
} from './services/authService';

import { renderAuthPage } from './components/auth/AuthPage';
import { renderChangePasswordPage } from './components/auth/ChangePasswordPage';
import { renderDashboardPage } from './pages/Dashboard';

const app = document.querySelector<HTMLDivElement>('#app');

if (!app) {
    throw new Error('Application root element #app was not found.');
}

type AppRoute =
    | '/login'
    | '/change-password'
    | '/staff'
    | '/organisations'
    | '/leave';

function navigate(path: AppRoute, replace = false): void {
    if (window.location.pathname !== path) {
        if (replace) {
            window.history.replaceState({}, '', path);
        } else {
            window.history.pushState({}, '', path);
        }
    }

    void renderApp();
}

async function renderApp(): Promise<void> {
    const path = window.location.pathname as AppRoute;
    const authenticated = isAuthenticated();

    // Unauthenticated users can only access the login page
    if (!authenticated) {
        if (path !== '/login') {
            window.history.replaceState({}, '', '/login');
        }

        renderAuthPage(app, {
            onAuthenticated: () => {
                navigate('/staff', true);
            }
        });

        return;
    }

    // Users with temporary passwords must change them first
    if (mustChangePassword()) {
        if (path !== '/change-password') {
            window.history.replaceState({}, '', '/change-password');
        }

        renderChangePasswordPage(app, {
            onPasswordChanged: () => {
                navigate('/staff', true);
            }
        });

        return;
    }

    // Authenticated users should not return to login or
    // the password-change page after completing authentication
    if (path === '/login' || path === '/change-password' || ![
        '/staff',
        '/organisations',
        '/leave'
    ].includes(path)) {
        window.history.replaceState({}, '', '/staff');
    }

    await renderDashboardPage(app);
}

// Handle browser back and forward navigation.
window.addEventListener('popstate', () => {
    void renderApp();
});

// Start the application.
void renderApp();
