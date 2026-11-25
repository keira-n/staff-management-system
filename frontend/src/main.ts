import './styles/global.css';
import './styles/auth.css';
import './styles/header.css';
import './styles/staff.css';
import './styles/organisations.css';
import './styles/leave.css';

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
