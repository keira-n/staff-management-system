import './style.css';

import type { Staff } from './types/Staff';
import type { Organisation } from './types/Organisation';

import {
    getAllStaff,
    addStaff,
    updateStaff,
    deleteStaff
} from './services/staffService';

import {
    getAllOrganisations
} from './services/organisationService';

import { renderOrganisationPage } from './organisation';

type ManagedStaff = Staff & {
    phone?: string;
    organisationName?: string;
    organisationType?: string;
    organisationId?: string;
    employeeCode?: string;
    customFields?: Record<string, unknown>;
};

const app = document.querySelector<HTMLDivElement>('#app')!;

let staffList: ManagedStaff[] = [];
let organisations: Organisation[] = [];

let editingId: string | null = null;
let selectedOrganisation = 'all';
let searchQuery = '';

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

async function loadData() {
    try {
        const [staff, organisationData] = await Promise.all([
            getAllStaff(),
            getAllOrganisations()
        ]);

        staffList = staff as ManagedStaff[];
        organisations = organisationData;

        render();
    } catch (error) {
        console.error(error);
        app.innerHTML = `
            <div class="container">
                <h2>Unable to load Staff Management</h2>
                <p>Check the backend connection.</p>
            </div>
        `;
    }
}

function getFilteredStaff(): ManagedStaff[] {
    return staffList.filter(staff => {
        const matchesOrganisation =
            selectedOrganisation === 'all' ||
            staff.organisationId === selectedOrganisation;

        const query = searchQuery.toLowerCase();

        const matchesSearch =
            staff.name.toLowerCase().includes(query) ||
            staff.email.toLowerCase().includes(query) ||
            (staff.employeeCode ?? '').toLowerCase().includes(query);

        return matchesOrganisation && matchesSearch;
    });
}

function render() {
    const filteredStaff = getFilteredStaff();

    app.innerHTML = `
        <div class="container">

            <header>
                <h1>Staff Management</h1>
                <p>Manage staff across different organisations</p>

                <div class="form-buttons">
                    <button type="button" id="showStaff">
                        Staff Members
                    </button>

                    <button type="button" id="showOrganisations">
                        Organisations
                    </button>
                </div>
            </header>

            <section class="form-section">
                <h2>${editingId ? 'Edit Staff' : 'Add New Staff'}</h2>

                <form id="staffForm">

                    <input
                        name="name"
                        placeholder="Full Name"
                        value="${escapeHTML(getEditingStaff()?.name ?? '')}"
                        required
                    >

                    <input
                        name="email"
                        type="email"
                        placeholder="Email"
                        value="${escapeHTML(getEditingStaff()?.email ?? '')}"
                        required
                    >

                    <input
                        name="phone"
                        placeholder="Phone Number"
                        value="${escapeHTML(getEditingStaff()?.phone ?? '')}"
                    >

                    <select name="organisationId" required>
                        <option value="">Select Organisation</option>

                        ${organisations.map(org => `
                            <option
                                value="${escapeHTML(org.id)}"
                                ${getEditingStaff()?.organisationId === org.id ? 'selected' : ''}
                            >
                                ${escapeHTML(org.name)} (${escapeHTML(org.type)})
                            </option>
                        `).join('')}
                    </select>

                    <input
                        name="employeeCode"
                        placeholder="Employee Code"
                        value="${escapeHTML(getEditingStaff()?.employeeCode ?? '')}"
                    >

                    <input
                        name="department"
                        placeholder="Department"
                        value="${escapeHTML(getEditingStaff()?.department ?? '')}"
                    >

                    <input
                        name="position"
                        placeholder="Position"
                        value="${escapeHTML(getEditingStaff()?.position ?? '')}"
                    >

                    <textarea
                        name="customFields"
                        placeholder='Custom Fields (JSON), e.g. {"shift":"Night","speciality":"Cardiology"}'
                        rows="4"
                    >${escapeHTML(
        JSON.stringify(
            getEditingStaff()?.customFields ?? {},
            null,
            2
        )
    )}</textarea>

                    <div class="form-buttons">
                        <button type="submit">
                            ${editingId ? 'Update Staff' : 'Add Staff'}
                        </button>

                        ${editingId ? `
                            <button type="button" id="cancelEdit">
                                Cancel
                            </button>
                        ` : ''}
                    </div>

                </form>
            </section>

            <section class="staff-section">

                <h2>Staff Members (${filteredStaff.length})</h2>

                <div class="staff-filters">

                    <input
                        id="searchStaff"
                        placeholder="Search staff..."
                        value="${escapeHTML(searchQuery)}"
                    >

                    <select id="filterOrganisation">
                        <option value="all">All Organisations</option>

                        ${organisations.map(org => `
                            <option
                                value="${escapeHTML(org.id)}"
                                ${selectedOrganisation === org.id ? 'selected' : ''}
                            >
                                ${escapeHTML(org.name)}
                            </option>
                        `).join('')}
                    </select>

                </div>

                <div id="staffList">

                    ${filteredStaff.length === 0
        ? '<p>No staff members found.</p>'
        : filteredStaff.map(staff => `
                            <div class="staff-card">

                                <div>
                                    <h3>${escapeHTML(staff.name)}</h3>

                                    <p>${escapeHTML(staff.email)}</p>

                                    <p>${escapeHTML(staff.phone ?? '')}</p>

                                    <p>
                                        ${escapeHTML(staff.position ?? '')}
                                        ${staff.department
            ? ` - ${escapeHTML(staff.department)}`
            : ''}
                                    </p>

                                    <p>
                                        Organisation:
                                        ${escapeHTML(
            staff.organisationName ??
            organisations.find(
                org => org.id === staff.organisationId
            )?.name ??
            'Not specified'
        )}
                                    </p>

                                    <p>
                                        Employee Code:
                                        ${escapeHTML(staff.employeeCode ?? 'N/A')}
                                    </p>

                                    ${Object.keys(staff.customFields ?? {}).length > 0
            ? `<p>Custom Fields: ${escapeHTML(
                JSON.stringify(staff.customFields)
            )}</p>`
            : ''}

                                </div>

                                <div class="actions">
                                    <button
                                        class="edit-btn"
                                        data-id="${escapeHTML(staff.id)}"
                                    >
                                        Edit
                                    </button>

                                    <button
                                        class="delete-btn"
                                        data-id="${escapeHTML(staff.id)}"
                                    >
                                        Delete
                                    </button>
                                </div>

                            </div>
                        `).join('')
    }

                </div>
            </section>

        </div>
    `;

    attachEventListeners();
}

function getEditingStaff(): ManagedStaff | undefined {
    return staffList.find(staff => staff.id === editingId);
}

function attachEventListeners() {
    document.querySelector('#showStaff')
        ?.addEventListener('click', () => {
            editingId = null;
            loadData();
        });

    document.querySelector('#showOrganisations')
        ?.addEventListener('click', () => {
            renderOrganisationPage(app, () => {
                editingId = null;
                loadData();
            });
        });

    const form = document.querySelector<HTMLFormElement>('#staffForm')!;

    form.addEventListener('submit', async event => {
        event.preventDefault();

        const formData = new FormData(form);

        const organisationId = String(
            formData.get('organisationId')
        );

        const organisation = organisations.find(
            org => org.id === organisationId
        );

        let customFields: Record<string, unknown> = {};

        try {
            const customFieldsInput = String(
                formData.get('customFields') ?? ''
            ).trim();

            if (customFieldsInput) {
                customFields = JSON.parse(customFieldsInput);

                if (
                    customFields === null ||
                    Array.isArray(customFields) ||
                    typeof customFields !== 'object'
                ) {
                    throw new Error('Custom fields must be a JSON object.');
                }
            }
        } catch {
            alert('Invalid custom fields JSON. Please check the format.');
            return;
        }

        const staff: ManagedStaff = {
            name: String(formData.get('name')).trim(),
            email: String(formData.get('email')).trim(),
            phone: String(formData.get('phone')).trim(),
            organisationId,
            organisationName: organisation?.name ?? '',
            organisationType: organisation?.type ?? '',
            employeeCode: String(formData.get('employeeCode')).trim(),
            department: String(formData.get('department')).trim(),
            position: String(formData.get('position')).trim(),
            customFields
        };

        try {
            if (editingId) {
                await updateStaff(editingId, staff);
            } else {
                await addStaff(staff);
            }

            editingId = null;
            await loadData();

        } catch (error) {
            console.error(error);
            alert('Unable to save staff member.');
        }
    });

    document.querySelector('#cancelEdit')
        ?.addEventListener('click', () => {
            editingId = null;
            render();
        });

    document.querySelector('#searchStaff')
        ?.addEventListener('input', event => {
            searchQuery = (event.target as HTMLInputElement).value;
            render();

            const searchInput = document.querySelector<HTMLInputElement>(
                '#searchStaff'
            );

            searchInput?.focus();
            searchInput?.setSelectionRange(
                searchQuery.length,
                searchQuery.length
            );
        });

    document.querySelector('#filterOrganisation')
        ?.addEventListener('change', event => {
            selectedOrganisation = (
                event.target as HTMLSelectElement
            ).value;

            render();
        });

    document.querySelectorAll<HTMLButtonElement>('.edit-btn')
        .forEach(button => {
            button.addEventListener('click', () => {
                editingId = button.dataset.id ?? null;
                render();

                document.querySelector('#staffForm')
                    ?.scrollIntoView({ behavior: 'smooth' });
            });
        });

    document.querySelectorAll<HTMLButtonElement>('.delete-btn')
        .forEach(button => {
            button.addEventListener('click', async () => {
                const id = button.dataset.id;

                if (!id || !confirm('Delete this staff member?')) {
                    return;
                }

                try {
                    await deleteStaff(id);
                    await loadData();
                } catch (error) {
                    console.error(error);
                    alert('Unable to delete staff member.');
                }
            });
        });
}

loadData();
