import type { Staff } from '../../types/Staff';

type StaffFormOptions = {
    staff?: Staff;
    onSubmit: (staff: Staff) => void;
    onCancel: () => void;
};

export function renderStaffForm(
    container: HTMLElement,
    options: StaffFormOptions
): void {
    const staff = options.staff;

    container.innerHTML = `
        <section class="form-section">
            <h2>${staff ? 'Edit Staff' : 'Add New Staff'}</h2>

            <form id="staffForm">

                <input
                    name="name"
                    placeholder="Full Name"
                    value="${escapeHTML(staff?.name ?? '')}"
                    required
                >

                <input
                    name="email"
                    type="email"
                    placeholder="Email"
                    value="${escapeHTML(staff?.email ?? '')}"
                    required
                >

                <input
                    name="phone"
                    placeholder="Phone Number"
                    value="${escapeHTML(staff?.phone ?? '')}"
                >

                <input
                    name="employeeCode"
                    placeholder="Employee Code"
                    value="${escapeHTML(staff?.employeeCode ?? '')}"
                >

                <input
                    name="department"
                    placeholder="Department"
                    value="${escapeHTML(staff?.department ?? '')}"
                >

                <input
                    name="position"
                    placeholder="Position"
                    value="${escapeHTML(staff?.position ?? '')}"
                >

                <textarea
                    name="customFields"
                    placeholder='Custom Fields (JSON), e.g. {"shift":"Night"}'
                    rows="4"
                >${escapeHTML(
        JSON.stringify(staff?.customFields ?? {}, null, 2)
    )}</textarea>

                <p id="staffFormError" style="color: #f87171;"></p>

                <div class="form-buttons">
                    <button type="submit">
                        ${staff ? 'Update Staff' : 'Add Staff'}
                    </button>

                    <button type="button" id="cancelStaffForm">
                        Cancel
                    </button>
                </div>

            </form>
        </section>
    `;

    const form = container.querySelector<HTMLFormElement>('#staffForm')!;

    form.addEventListener('submit', event => {
        event.preventDefault();

        const formData = new FormData(form);
        const errorElement =
            container.querySelector<HTMLElement>('#staffFormError');

        let customFields: Record<string, unknown> = {};

        try {
            const customFieldsInput = String(
                formData.get('customFields') ?? ''
            ).trim();

            if (customFieldsInput) {
                const parsed: unknown = JSON.parse(customFieldsInput);

                if (
                    parsed === null ||
                    Array.isArray(parsed) ||
                    typeof parsed !== 'object'
                ) {
                    throw new Error('Custom fields must be a JSON object.');
                }

                customFields = parsed as Record<string, unknown>;
            }
        } catch (error) {
            if (errorElement) {
                errorElement.textContent =
                    error instanceof Error
                        ? error.message
                        : 'Invalid custom fields JSON.';
            }
            return;
        }

        const submittedStaff: Staff = {
            ...(staff ?? {}),
            name: String(formData.get('name')).trim(),
            email: String(formData.get('email')).trim(),
            phone: String(formData.get('phone') ?? '').trim(),
            employeeCode: String(
                formData.get('employeeCode') ?? ''
            ).trim(),
            department: String(
                formData.get('department') ?? ''
            ).trim(),
            position: String(
                formData.get('position') ?? ''
            ).trim(),
            customFields
        };

        options.onSubmit(submittedStaff);
    });

    container.querySelector('#cancelStaffForm')
        ?.addEventListener('click', options.onCancel);
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