import './styles/global.css';

import './components/auth/auth.css';
import './components/layout/header.css';
import './components/staff/staff.css';
import './components/organisations/organisations.css';
import './components/leave/leave.css';

import { isAuthenticated } from './services/authService';

import { renderAuthPage } from './components/auth/AuthPage';
import { renderDashboardPage } from './pages/Dashboard';

const app = document.querySelector<HTMLDivElement>('#app');

if (!app) {
    throw new Error('Application root element #app was not found.');
}

async function renderApp(): Promise<void> {
    if (isAuthenticated()) {
        await renderDashboardPage(app);
        return;
    }

    renderAuthPage(app, {
        onAuthenticated: () => {
            void renderApp();
        }
    });
}

void renderApp();
