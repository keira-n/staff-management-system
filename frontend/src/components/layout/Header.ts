import { getCurrentUser } from '../../services/authService';

type HeaderPage = 'staff' | 'organisations' | 'leave';

type HeaderOptions = {
    onNavigate: (page: HeaderPage) => void;
    onLogout: () => void;
};

export function renderHeader(
    container: HTMLElement,
    options: HeaderOptions
): void {
    const user = getCurrentUser();

    if (!user) {
        return;
    }

    const isAdmin = user.role === 'ADMIN';

    container.innerHTML = `
        <header>
            <h1>Staff Management</h1>

            <p>${escapeHTML(user.organisationName)}</p>

            <p>
                ${escapeHTML(user.email)}
                — ${escapeHTML(user.role)}
            </p>

            <div class="form-buttons">
                <button type="button" id="navigateStaff">
                    Staff Members
                </button>

                <button type="button" id="navigateLeave">
                    Leave Management
                </button>

                ${isAdmin ? `
                    <button type="button" id="navigateOrganisations">
                        Organisation Settings
                    </button>
                ` : ''}

                <button type="button" id="headerLogout">
                    Logout
                </button>
            </div>
        </header>
    `;

    container.querySelector('#navigateStaff')
        ?.addEventListener('click', () => {
            options.onNavigate('staff');
        });

    container.querySelector('#navigateLeave')
        ?.addEventListener('click', () => {
            options.onNavigate('leave');
        });

    container.querySelector('#navigateOrganisations')
        ?.addEventListener('click', () => {
            options.onNavigate('organisations');
        });

    container.querySelector('#headerLogout')
        ?.addEventListener('click', () => {
            options.onLogout();
        });
}

function escapeHTML(value: unknown): string {
    return String(value ?? '').replace(/[&<>"']/g, character => {
        const entities: Record<string, string> = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        };

        return entities[character];
    });
}
