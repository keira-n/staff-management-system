import { changePassword } from '../../services/authService';

type ChangePasswordPageOptions = {
    onPasswordChanged: () => void;
};

export function renderChangePasswordPage(
    app: HTMLDivElement,
    options: ChangePasswordPageOptions
): void {

    app.innerHTML = `
        <div class="auth-page">
            <div class="container">
                <header>
                    <h1>Staff Management</h1>
                    <p>Secure your account before continuing</p>
                </header>

                <section class="form-section">
                    <h2>Change Your Password</h2>

                    <p>
                        You are using a temporary password.
                        Please create a new password to access
                        your organisation's dashboard.
                    </p>

                    <form id="changePasswordForm">

                        <input
                            name="currentPassword"
                            type="password"
                            placeholder="Current Temporary Password"
                            autocomplete="current-password"
                            required
                        >

                        <input
                            name="newPassword"
                            type="password"
                            placeholder="New Password"
                            autocomplete="new-password"
                            minlength="8"
                            required
                        >

                        <input
                            name="confirmPassword"
                            type="password"
                            placeholder="Confirm New Password"
                            autocomplete="new-password"
                            minlength="8"
                            required
                        >

                        <p>
                            Your new password must contain at least
                            8 characters.
                        </p>

                        <div class="form-buttons">
                            <button type="submit">
                                Change Password
                            </button>
                        </div>

                    </form>

                    <p
                        id="changePasswordError"
                        class="error-message"
                    ></p>

                    <p id="changePasswordSuccess"></p>
                </section>
            </div>
        </div>
    `;

    const form =
        app.querySelector<HTMLFormElement>('#changePasswordForm');

    const errorElement =
        app.querySelector<HTMLElement>('#changePasswordError');

    const successElement =
        app.querySelector<HTMLElement>('#changePasswordSuccess');

    form?.addEventListener('submit', async event => {
        event.preventDefault();

        if (!form || !errorElement || !successElement) {
            return;
        }

        const formData = new FormData(form);

        const currentPassword = String(
            formData.get('currentPassword')
        );

        const newPassword = String(
            formData.get('newPassword')
        );

        const confirmPassword = String(
            formData.get('confirmPassword')
        );

        const submitButton =
            form.querySelector<HTMLButtonElement>(
                'button[type="submit"]'
            );

        errorElement.textContent = '';
        successElement.textContent = '';

        if (newPassword.length < 8) {
            errorElement.textContent =
                'Your new password must contain at least 8 characters.';
            return;
        }

        if (newPassword !== confirmPassword) {
            errorElement.textContent =
                'The new passwords do not match.';
            return;
        }

        if (currentPassword === newPassword) {
            errorElement.textContent =
                'Your new password must be different from your temporary password.';
            return;
        }

        if (submitButton) {
            submitButton.disabled = true;
            submitButton.textContent = 'Changing Password...';
        }

        try {
            await changePassword({
                currentPassword,
                newPassword
            });

            successElement.textContent =
                'Password changed successfully. Redirecting to your dashboard...';

            form.reset();

            options.onPasswordChanged();

        } catch (error) {
            console.error(error);

            errorElement.textContent =
                error instanceof Error
                    ? error.message
                    : 'Unable to change your password. Please try again.';

        } finally {
            if (submitButton) {
                submitButton.disabled = false;
                submitButton.textContent = 'Change Password';
            }
        }
    });
}
