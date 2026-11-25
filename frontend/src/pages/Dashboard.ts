import type { Staff } from '../types/Staff';

import {
    getAllStaff,
    addStaff,
    updateStaff,
    deleteStaff
} from '../services/staffService';

import {
    logout,
    getCurrentUser
} from '../services/authService';

import { renderHeader } from '../components/layout/Header';
import { renderStaffForm } from '../components/staff/StaffForm';
import { renderStaffList } from '../components/staff/StaffList';
import { renderOrganisationSettings } from '../components/organisations/OrganisationSettings';

import { renderLeaveRequestForm } from '../components/leave/LeaveRequestForm';
import { renderLeaveRequestList } from '../components/leave/LeaveRequestList';
import { renderLeaveBalanceCard } from '../components/leave/LeaveBalanceCard';

type ManagedStaff = Staff & {
    phone?: string;
    employeeCode?: string;
    customFields?: Record<string, unknown>;
};

type DashboardPageType = 'staff' | 'organisations' | 'leave';

const PAGE_ROUTES: Record<DashboardPageType, string> = {
    staff: '/staff',
    organisations: '/organisations',
    leave: '/leave'
};

function getPageFromPath(): DashboardPageType {
    switch (window.location.pathname) {
        case '/organisations':
            return 'organisations';

        case '/leave':
            return 'leave';

        case '/staff':
        default:
            return 'staff';
    }
}

export async function renderDashboardPage(
    app: HTMLElement
): Promise<void> {
    let staffList: ManagedStaff[] = [];
    let editingStaff: ManagedStaff | null = null;
    let currentPage: DashboardPageType = getPageFromPath();

    const currentUser = getCurrentUser();
    const userRole = currentUser?.role?.toUpperCase() ?? 'USER';

    const canReviewLeave = ['MANAGER', 'HR', 'ADMIN'].includes(userRole);

    app.innerHTML = `
        <div class="dashboard-layout">
            <div id="dashboardHeader"></div>

            <main class="dashboard-main">
                <div class="container">
                    <div id="dashboardContent"></div>
                </div>
            </main>
        </div>
    `;

    const headerContainer = app.querySelector<HTMLElement>(
        '#dashboardHeader'
    )!;

    const contentContainer = app.querySelector<HTMLElement>(
        '#dashboardContent'
    )!;

    renderHeader(headerContainer, {
        onNavigate: page => {
            if (
                page !== 'staff' &&
                page !== 'organisations' &&
                page !== 'leave'
            ) {
                return;
            }

            navigateToPage(page);
        },
        onLogout: () => {
            logout();
            window.location.assign('/login');
        }
    });

    function navigateToPage(page: DashboardPageType): void {
        currentPage = page;

        const route = PAGE_ROUTES[page];

        if (window.location.pathname !== route) {
            window.history.pushState({}, '', route);
        }

        void renderPage();
    }

    async function loadStaff(): Promise<void> {
        contentContainer.innerHTML = `
            <p>Loading staff...</p>
        `;

        try {
            staffList = await getAllStaff();
            renderStaffPage();
        } catch (error) {
            console.error(error);

            contentContainer.innerHTML = `
                <section class="form-section">
                    <h2>Unable to load staff.</h2>
                    <p>Please try again.</p>
                    <button type="button" id="retryStaff">
                        Retry
                    </button>
                </section>
            `;

            contentContainer
                .querySelector('#retryStaff')
                ?.addEventListener('click', loadStaff);
        }
    }

    function showTemporaryPassword(password: string): void {
        const formContainer = contentContainer.querySelector<HTMLElement>(
            '#staffFormContainer'
        );

        if (!formContainer) {
            return;
        }

        const section = document.createElement('section');
        section.className = 'form-section';

        const heading = document.createElement('h2');
        heading.textContent = 'Staff account created successfully';

        const explanation = document.createElement('p');
        explanation.textContent =
            'Give this temporary password to the employee. They must change it when they first sign in.';

        const passwordContainer = document.createElement('div');
        passwordContainer.className = 'temporary-password';

        const passwordLabel = document.createElement('p');
        passwordLabel.textContent = 'Temporary password';

        const passwordValue = document.createElement('strong');
        passwordValue.textContent = password;

        const copyButton = document.createElement('button');
        copyButton.type = 'button';
        copyButton.textContent = 'Copy password';

        copyButton.addEventListener('click', async () => {
            try {
                await navigator.clipboard.writeText(password);
                copyButton.textContent = 'Copied';
            } catch (error) {
                console.error(error);
                alert('Unable to copy the password. Please copy it manually.');
            }
        });

        passwordContainer.appendChild(passwordLabel);
        passwordContainer.appendChild(passwordValue);
        passwordContainer.appendChild(copyButton);

        const warning = document.createElement('p');
        warning.textContent =
            'This password will not be shown again. Make sure the employee receives it before closing this page.';

        section.appendChild(heading);
        section.appendChild(explanation);
        section.appendChild(passwordContainer);
        section.appendChild(warning);

        formContainer.appendChild(section);

        section.scrollIntoView({
            behavior: 'smooth',
            block: 'center'
        });
    }

    function renderStaffPage(): void {
        contentContainer.innerHTML = `
            <section class="staff-section">

                <div class="staff-list-heading">
                    <div>
                        <h1>Staff Management</h1>
                        <p>Manage your organisation's staff members.</p>
                    </div>

                    <button type="button" id="addStaffButton">
                        + Add Staff
                    </button>
                </div>

                <div id="staffFormContainer"></div>

                <div id="staffListContainer"></div>

            </section>
        `;

        const formContainer = contentContainer.querySelector<HTMLDivElement>(
            '#staffFormContainer'
        )!;

        const listContainer = contentContainer.querySelector<HTMLDivElement>(
            '#staffListContainer'
        )!;

        renderStaffList(listContainer, staffList, {
            onEdit: staff => {
                editingStaff = staff;
                showStaffForm();
            },
            onDelete: deleteStaffMember
        });

        contentContainer
            .querySelector('#addStaffButton')
            ?.addEventListener('click', () => {
                editingStaff = null;
                showStaffForm();
            });

        function showStaffForm(): void {
            renderStaffForm(formContainer, {
                staff: editingStaff ?? undefined,

                onSubmit: async staff => {
                    try {
                        if (editingStaff?.id) {
                            await updateStaff(editingStaff.id, staff);

                            editingStaff = null;
                            await loadStaff();
                        } else {
                            const result = await addStaff(staff);

                            editingStaff = null;
                            await loadStaff();

                            showTemporaryPassword(result.temporaryPassword);
                        }
                    } catch (error) {
                        console.error(error);
                        alert('Unable to save staff member.');
                    }
                },

                onCancel: () => {
                    editingStaff = null;
                    formContainer.innerHTML = '';
                }
            });

            formContainer.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    }

    async function deleteStaffMember(
        staff: ManagedStaff
    ): Promise<void> {
        if (!staff.id) {
            return;
        }

        if (!confirm(`Delete ${staff.name}?`)) {
            return;
        }

        try {
            await deleteStaff(staff.id);
            await loadStaff();
        } catch (error) {
            console.error(error);
            alert('Unable to delete staff member.');
        }
    }

    async function renderLeavePage(): Promise<void> {
        contentContainer.innerHTML = `
            <section class="leave-section">
                <div class="staff-list-heading">
                    <div>
                        <h1>Leave Management</h1>
                        <p>Submit and manage employee leave requests.</p>
                    </div>
                </div>

                <nav class="leave-navigation">
                    <button type="button" data-leave-tab="request">
                        Request Leave
                    </button>

                    <button type="button" data-leave-tab="history">
                        ${canReviewLeave ? 'All Requests' : 'My Requests'}
                    </button>

                    <button type="button" data-leave-tab="balance">
                        My Leave Balance
                    </button>

                    ${
            canReviewLeave
                ? `
                                <button type="button" data-leave-tab="review">
                                    Review Requests
                                </button>
                            `
                : ''
        }
                </nav>

                <div id="leaveContent"></div>
            </section>
        `;

        const leaveContent = contentContainer.querySelector<HTMLDivElement>(
            '#leaveContent'
        )!;

        async function showLeaveTab(tab: string): Promise<void> {
            leaveContent.innerHTML = '';

            switch (tab) {
                case 'request': {
                    const formContainer = document.createElement('div');
                    leaveContent.appendChild(formContainer);

                    renderLeaveRequestForm(formContainer, {
                        onSubmitted: async () => {
                            alert('Leave request submitted successfully.');
                            await showLeaveTab('history');
                        }
                    });
                    break;
                }

                case 'history': {
                    const listContainer = document.createElement('div');
                    leaveContent.appendChild(listContainer);

                    await renderLeaveRequestList(listContainer, {
                        canReview: false
                    });
                    break;
                }

                case 'balance': {
                    const balanceContainer = document.createElement('div');
                    leaveContent.appendChild(balanceContainer);

                    await renderLeaveBalanceCard(balanceContainer);
                    break;
                }

                case 'review': {
                    if (!canReviewLeave) {
                        return;
                    }

                    const reviewContainer = document.createElement('div');
                    leaveContent.appendChild(reviewContainer);

                    await renderLeaveRequestList(reviewContainer, {
                        canReview: true
                    });
                    break;
                }
            }
        }

        contentContainer
            .querySelectorAll<HTMLButtonElement>('[data-leave-tab]')
            .forEach(button => {
                button.addEventListener('click', async () => {
                    const tab = button.dataset.leaveTab;

                    if (tab) {
                        await showLeaveTab(tab);
                    }
                });
            });

        await showLeaveTab('request');
    }

    async function renderPage(): Promise<void> {
        if (currentPage === 'staff') {
            await loadStaff();
            return;
        }

        if (currentPage === 'leave') {
            await renderLeavePage();
            return;
        }

        await renderOrganisationSettings(contentContainer);
    }

    await renderPage();
}