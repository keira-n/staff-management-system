import { getMyLeaveBalance } from '../../services/leaveService';
import type { LeaveBalance } from '../../types/Leave';

function renderBalanceItem(
    title: string,
    annual: number,
    used: number,
    remaining: number
): string {
    const percentage = annual > 0
        ? Math.min(100, Math.max(0, (used / annual) * 100))
        : 0;

    return `
        <div class="leave-balance-item">
            <h3>${title}</h3>

            <div class="leave-balance-numbers">
                <div>
                    <strong>${remaining}</strong>
                    <span>Remaining</span>
                </div>

                <div>
                    <strong>${used}</strong>
                    <span>Used</span>
                </div>

                <div>
                    <strong>${annual}</strong>
                    <span>Annual allowance</span>
                </div>
            </div>

            <div class="leave-progress-track">
                <div
                    class="leave-progress-fill"
                    style="width: ${percentage}%"
                ></div>
            </div>
        </div>
    `;
}

export async function renderLeaveBalanceCard(
    app: HTMLDivElement
): Promise<void> {
    app.innerHTML = `
        <div class="leave-balance-container">
            <h2>My Leave Balance</h2>
            <p class="loading-message">Loading leave balance...</p>
        </div>
    `;

    try {
        const balance: LeaveBalance = await getMyLeaveBalance();

        app.innerHTML = `
            <div class="leave-balance-container">
                <h2>My Leave Balance</h2>

                <div class="leave-balance-grid">
                    ${renderBalanceItem(
            'Paid Vacation',
            balance.annualPaidVacation,
            balance.usedPaidVacation,
            balance.remainingPaidVacation
        )}

                    ${renderBalanceItem(
            'Sick Leave',
            balance.annualSickLeave,
            balance.usedSickLeave,
            balance.remainingSickLeave
        )}
                </div>
            </div>
        `;
    } catch (error) {
        app.innerHTML = `
            <div class="leave-balance-container">
                <h2>My Leave Balance</h2>
                <p class="error-message">
                    ${error instanceof Error
            ? error.message
            : 'Failed to load leave balance.'}
                </p>
            </div>
        `;
    }
}