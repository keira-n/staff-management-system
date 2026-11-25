import {
    getMyLeaveRequests,
    getReviewableLeaveRequests,
    approveLeaveRequest,
    rejectLeaveRequest
} from '../../services/leaveService';

import type { LeaveRequest } from '../../types/Leave';

type LeaveRequestListOptions = {
    canReview?: boolean;
    onUpdated?: () => void;
};

function escapeHtml(value: unknown): string {
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

function formatDate(value: string): string {
    if (!value) return '-';

    return new Date(`${value}T00:00:00`).toLocaleDateString('en-GB');
}

function renderRequest(request: LeaveRequest, canReview: boolean): string {
    const leaveType =
        request.leaveType === 'SICK_LEAVE'
            ? 'Sick Leave'
            : 'Paid Vacation';

    const statusClass = request.status.toLowerCase();

    return `
        <div class="leave-request-card">
            <div class="leave-request-header">
                <h3>${escapeHtml(leaveType)}</h3>
                <span class="leave-status ${escapeHtml(statusClass)}">
                    ${escapeHtml(request.status)}
                </span>
            </div>

            ${
        canReview
            ? `
                        <p><strong>Employee:</strong> ${escapeHtml(request.employeeName)}</p>
                        <p><strong>Email:</strong> ${escapeHtml(request.employeeEmail)}</p>
                        <p><strong>Department:</strong> ${escapeHtml(request.department || '-')}</p>
                    `
            : ''
    }

            <p>
                <strong>Dates:</strong>
                ${formatDate(request.startDate)} –
                ${formatDate(request.endDate)}
            </p>

            <p><strong>Total days:</strong> ${escapeHtml(request.totalDays)}</p>
            <p><strong>Reason:</strong> ${escapeHtml(request.reason)}</p>

            ${
        request.reviewComment
            ? `<p><strong>Review comment:</strong> ${escapeHtml(request.reviewComment)}</p>`
            : ''
    }

            ${
        canReview && request.status === 'PENDING'
            ? `
                        <div class="leave-review-actions">
                            <textarea
                                class="review-comment"
                                data-id="${escapeHtml(request.id)}"
                                placeholder="Optional review comment..."
                                rows="2"
                            ></textarea>

                            <button
                                type="button"
                                class="approve-leave-button"
                                data-id="${escapeHtml(request.id)}"
                            >
                                Approve
                            </button>

                            <button
                                type="button"
                                class="reject-leave-button"
                                data-id="${escapeHtml(request.id)}"
                            >
                                Reject
                            </button>
                        </div>
                    `
            : ''
    }
        </div>
    `;
}

export async function renderLeaveRequestList(
    app: HTMLDivElement,
    options: LeaveRequestListOptions = {}
): Promise<void> {
    const canReview = options.canReview ?? false;

    app.innerHTML = `
        <div class="leave-list-container">
            <h2>${canReview ? 'Leave Requests to Review' : 'My Leave Requests'}</h2>
            <p class="loading-message">Loading leave requests...</p>
        </div>
    `;

    try {
        const requests = canReview
            ? await getReviewableLeaveRequests()
            : await getMyLeaveRequests();

        app.innerHTML = `
            <div class="leave-list-container">
                <h2>${canReview ? 'Leave Requests to Review' : 'My Leave Requests'}</h2>

                ${
            requests.length === 0
                ? '<p>No leave requests found.</p>'
                : `<div class="leave-request-list">
                            ${requests
                    .map(request => renderRequest(request, canReview))
                    .join('')}
                        </div>`
        }

                <p id="leave-list-error" class="error-message"></p>
            </div>
        `;

        if (!canReview) return;

        const errorElement = app.querySelector<HTMLParagraphElement>(
            '#leave-list-error'
        );

        app.querySelectorAll<HTMLButtonElement>('.approve-leave-button')
            .forEach(button => {
                button.addEventListener('click', async () => {
                    await reviewRequest(button, 'approve');
                });
            });

        app.querySelectorAll<HTMLButtonElement>('.reject-leave-button')
            .forEach(button => {
                button.addEventListener('click', async () => {
                    await reviewRequest(button, 'reject');
                });
            });

        async function reviewRequest(
            button: HTMLButtonElement,
            action: 'approve' | 'reject'
        ): Promise<void> {
            const id = button.dataset.id;

            if (!id) return;

            const commentInput = app.querySelector<HTMLTextAreaElement>(
                `.review-comment[data-id="${CSS.escape(id)}"]`
            );

            const comment = commentInput?.value.trim() ?? '';

            button.disabled = true;

            try {
                if (action === 'approve') {
                    await approveLeaveRequest(id, comment);
                } else {
                    await rejectLeaveRequest(id, comment);
                }

                await renderLeaveRequestList(app, options);
                options.onUpdated?.();
            } catch (error) {
                if (errorElement) {
                    errorElement.textContent =
                        error instanceof Error
                            ? error.message
                            : 'Failed to review leave request.';
                }

                button.disabled = false;
            }
        }
    } catch (error) {
        app.innerHTML = `
            <div class="leave-list-container">
                <h2>${canReview ? 'Leave Requests to Review' : 'My Leave Requests'}</h2>
                <p class="error-message">
                    ${escapeHtml(
            error instanceof Error
                ? error.message
                : 'Failed to load leave requests.'
        )}
                </p>
            </div>
        `;
    }
}