import { submitLeaveRequest } from '../../services/leaveService';
import type { CreateLeaveRequest, LeaveType } from '../../types/Leave';

type LeaveRequestFormOptions = {
    onSubmitted: () => void;
};

export function renderLeaveRequestForm(
    app: HTMLDivElement,
    options: LeaveRequestFormOptions
): void {
    app.innerHTML = `
        <div class="form-container">
            <h2>Request Leave</h2>

            <form id="leave-request-form">
                <div class="form-group">
                    <label for="leaveType">Leave Type</label>
                    <select id="leaveType" name="leaveType" required>
                        <option value="PAID_VACATION">Paid Vacation</option>
                        <option value="SICK_LEAVE">Sick Leave</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="startDate">Start Date</label>
                    <input
                        type="date"
                        id="startDate"
                        name="startDate"
                        required
                    >
                </div>

                <div class="form-group">
                    <label for="endDate">End Date</label>
                    <input
                        type="date"
                        id="endDate"
                        name="endDate"
                        required
                    >
                </div>

                <div class="form-group">
                    <label for="reason">Reason</label>
                    <textarea
                        id="reason"
                        name="reason"
                        rows="4"
                        placeholder="Enter the reason for your leave..."
                        required
                    ></textarea>
                </div>

                <p id="leave-form-error" class="error-message"></p>

                <button type="submit" id="submit-leave-button">
                    Submit Request
                </button>
            </form>
        </div>
    `;

    const form = app.querySelector<HTMLFormElement>('#leave-request-form');
    const startDateInput = app.querySelector<HTMLInputElement>('#startDate');
    const endDateInput = app.querySelector<HTMLInputElement>('#endDate');
    const errorElement = app.querySelector<HTMLParagraphElement>(
        '#leave-form-error'
    );
    const submitButton = app.querySelector<HTMLButtonElement>(
        '#submit-leave-button'
    );

    if (!form || !startDateInput || !endDateInput || !errorElement || !submitButton) {
        return;
    }

    const today = new Date().toISOString().split('T')[0];
    startDateInput.min = today;
    endDateInput.min = today;

    startDateInput.addEventListener('change', () => {
        endDateInput.min = startDateInput.value || today;

        if (endDateInput.value && endDateInput.value < startDateInput.value) {
            endDateInput.value = startDateInput.value;
        }
    });

    form.addEventListener('submit', async (event: SubmitEvent) => {
        event.preventDefault();
        errorElement.textContent = '';

        const formData = new FormData(form);

        const startDate = String(formData.get('startDate'));
        const endDate = String(formData.get('endDate'));

        if (endDate < startDate) {
            errorElement.textContent = 'End date cannot be before start date.';
            return;
        }

        const request: CreateLeaveRequest = {
            leaveType: String(formData.get('leaveType')) as LeaveType,
            startDate,
            endDate,
            reason: String(formData.get('reason')).trim()
        };

        if (!request.reason) {
            errorElement.textContent = 'Please provide a reason for your leave.';
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = 'Submitting...';

        try {
            await submitLeaveRequest(request);
            options.onSubmitted();
        } catch (error) {
            errorElement.textContent =
                error instanceof Error
                    ? error.message
                    : 'Failed to submit leave request.';
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = 'Submit Request';
        }
    });
}