import type { Organisation } from '../../types/Organisation';

import {
    getAllOrganisations,
    updateOrganisation
} from '../../services/organisationService';

export async function renderOrganisationSettings(
    container: HTMLElement
): Promise<void> {
    let organisation: Organisation | null = null;

    async function loadOrganisation(): Promise<void> {
        container.innerHTML = `
            <div class="container">
                <p>Loading organisation settings...</p>
            </div>
        `;

        try {
            const organisations = await getAllOrganisations();

            organisation = organisations.length > 0
                ? organisations[0]
                : null;

            render();
        } catch (error) {
            console.error(error);

            container.innerHTML = `
                <div class="container">
                    <h2>Unable to load organisation settings.</h2>
                </div>
            `;
        }
    }

    function render(): void {
        if (!organisation) {
            container.innerHTML = `
                <div class="container">
                    <h2>Organisation not found.</h2>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="container">

                <header>
                    <h1>Organisation Settings</h1>
                    <p>Manage your organisation's information.</p>
                </header>

                <section class="form-section">

                    <h2>Organisation Information</h2>

                    <form id="organisationForm">

                        <label for="organisationName">
                            Organisation Name
                        </label>

                        <input
                            id="organisationName"
                            name="name"
                            type="text"
                            value="${escapeHTML(organisation.name)}"
                            placeholder="Organisation Name"
                            required
                        >

                        <label for="organisationType">
                            Organisation Type
                        </label>

                        <input
                            id="organisationType"
                            name="type"
                            type="text"
                            value="${escapeHTML(organisation.type)}"
                            placeholder="Organisation Type"
                            required
                        >

                        <label for="organisationDescription">
                            Description
                        </label>

                        <textarea
                            id="organisationDescription"
                            name="description"
                            rows="4"
                            placeholder="Organisation Description"
                        >${escapeHTML(organisation.description ?? '')}</textarea>

                        <div class="form-buttons">
                            <button type="submit">
                                Save Changes
                            </button>
                        </div>

                    </form>

                </section>

            </div>
        `;

        const form = container.querySelector<HTMLFormElement>(
            '#organisationForm'
        );

        form?.addEventListener('submit', async event => {
            event.preventDefault();

            if (!organisation?.id) {
                alert('Organisation ID is missing.');
                return;
            }

            const formData = new FormData(form);

            const updatedOrganisation: Organisation = {
                id: organisation.id,
                name: String(formData.get('name')).trim(),
                type: String(formData.get('type')).trim(),
                description: String(
                    formData.get('description') ?? ''
                ).trim()
            };

            if (!updatedOrganisation.name || !updatedOrganisation.type) {
                alert('Organisation name and type are required.');
                return;
            }

            const submitButton = form.querySelector<HTMLButtonElement>(
                'button[type="submit"]'
            );

            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = 'Saving...';
            }

            try {
                await updateOrganisation(
                    organisation.id,
                    updatedOrganisation
                );

                organisation = updatedOrganisation;

                alert('Organisation settings updated successfully.');

                render();
            } catch (error) {
                console.error(error);
                alert('Unable to update organisation settings.');

                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.textContent = 'Save Changes';
                }
            }
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

    await loadOrganisation();
}
