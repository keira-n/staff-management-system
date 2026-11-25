import type { Staff } from '../../types/Staff';

type StaffCardOptions = {
    onEdit: () => void;
    onDelete: () => void;
};

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

export function renderStaffCard(
    staff: Staff,
    options: StaffCardOptions
): HTMLElement {
    const card = document.createElement('div');
    card.className = 'staff-card';

    const customFields = staff.customFields ?? {};

    card.innerHTML = `
        <div>
            <h3>${escapeHTML(staff.name)}</h3>

            <p>${escapeHTML(staff.email)}</p>

            ${staff.phone
        ? `<p>${escapeHTML(staff.phone)}</p>`
        : ''}

            <p>
                ${escapeHTML(staff.position ?? '')}
                ${staff.department
        ? ` - ${escapeHTML(staff.department)}`
        : ''}
            </p>

            <p>
                Employee Code:
                ${escapeHTML(staff.employeeCode ?? 'N/A')}
            </p>

            ${Object.keys(customFields).length > 0
        ? `<p>Custom Fields: ${escapeHTML(
            JSON.stringify(customFields)
        )}</p>`
        : ''}
        </div>

        <div class="actions">
            <button type="button" class="edit-btn">Edit</button>
            <button type="button" class="delete-btn">Delete</button>
        </div>
    `;

    card.querySelector('.edit-btn')
        ?.addEventListener('click', options.onEdit);

    card.querySelector('.delete-btn')
        ?.addEventListener('click', options.onDelete);

    return card;
}