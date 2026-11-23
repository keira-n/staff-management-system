import type { Organisation } from './types/Organisation';

import {
    getAllOrganisations,
    addOrganisation,
    updateOrganisation,
    deleteOrganisation
} from './services/organisationService';

export async function renderOrganisationPage(
    app: HTMLDivElement,
    onBack: () => void
) {
    let organisations: Organisation[] = [];
    let editingId: string | null = null;

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

    async function loadOrganisations() {
        try {
            organisations = await getAllOrganisations();
            render();
        } catch (error) {
            console.error(error);
            app.innerHTML = `
                <div class="container">
                    <h2>Unable to load organisations.</h2>
                </div>
            `;
        }
    }

    function render() {
        app.innerHTML = `
            <div class="container">

                <header>
                    <button type="button" id="backToStaff">
                        ← Back to Staff
                    </button>

                    <h1>Organisation Management</h1>
                    <p>Manage organisations using Staff Management</p>
                </header>

                <section class="form-section">

                    <h2>
                        ${editingId ? 'Edit Organisation' : 'Add Organisation'}
                    </h2>

                    <form id="organisationForm">

                        <input
                            name="name"
                            placeholder="Organisation Name"
                            required
                        >

                        <input
                            name="type"
                            placeholder="Organisation Type"
                            required
                        >

                        <textarea
                            name="description"
                            placeholder="Description"
                            rows="4"
                        ></textarea>

                        <div class="form-buttons">

                            <button type="submit">
                                ${editingId ? 'Update Organisation' : 'Add Organisation'}
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

                    <h2>Organisations (${organisations.length})</h2>

                    ${
            organisations.length === 0
                ? '<p>No organisations added yet.</p>'
                : organisations.map(org => `
                                <div class="staff-card">

                                    <div>
                                        <h3>${escapeHTML(org.name)}</h3>
                                        <p>Type: ${escapeHTML(org.type)}</p>
                                        <p>${escapeHTML(org.description ?? '')}</p>
                                    </div>

                                    <div class="actions">

                                        <button
                                            class="edit-btn"
                                            data-id="${escapeHTML(org.id)}"
                                        >
                                            Edit
                                        </button>

                                        <button
                                            class="delete-btn"
                                            data-id="${escapeHTML(org.id)}"
                                        >
                                            Delete
                                        </button>

                                    </div>

                                </div>
                            `).join('')
        }

                </section>

            </div>
        `;

        document.querySelector('#backToStaff')
            ?.addEventListener('click', onBack);

        const form = document.querySelector<HTMLFormElement>(
            '#organisationForm'
        )!;

        if (editingId) {
            const organisation = organisations.find(
                org => org.id === editingId
            );

            if (organisation) {
                (form.elements.namedItem('name') as HTMLInputElement).value =
                    organisation.name;

                (form.elements.namedItem('type') as HTMLInputElement).value =
                    organisation.type;

                (form.elements.namedItem('description') as HTMLTextAreaElement).value =
                    organisation.description ?? '';
            }
        }

        form.addEventListener('submit', async event => {
            event.preventDefault();

            const formData = new FormData(form);

            const organisation: Organisation = {
                name: String(formData.get('name')).trim(),
                type: String(formData.get('type')).trim(),
                description: String(formData.get('description')).trim()
            };

            try {
                if (editingId) {
                    await updateOrganisation(editingId, organisation);
                } else {
                    await addOrganisation(organisation);
                }

                editingId = null;
                await loadOrganisations();

            } catch (error) {
                console.error(error);
                alert('Unable to save organisation.');
            }
        });

        document.querySelector('#cancelEdit')
            ?.addEventListener('click', () => {
                editingId = null;
                render();
            });

        document.querySelectorAll<HTMLButtonElement>('.edit-btn')
            .forEach(button => {
                button.addEventListener('click', () => {
                    editingId = button.dataset.id ?? null;
                    render();

                    document.querySelector('#organisationForm')
                        ?.scrollIntoView({ behavior: 'smooth' });
                });
            });

        document.querySelectorAll<HTMLButtonElement>('.delete-btn')
            .forEach(button => {
                button.addEventListener('click', async () => {
                    const id = button.dataset.id;

                    if (!id || !confirm('Delete this organisation?')) {
                        return;
                    }

                    try {
                        await deleteOrganisation(id);
                        await loadOrganisations();
                    } catch (error) {
                        console.error(error);
                        alert('Unable to delete organisation.');
                    }
                });
            });
    }

    await loadOrganisations();
}
