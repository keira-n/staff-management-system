import {
    login,
    registerOrganisation
} from '../../services/authService';

type AuthPageOptions = {
    onAuthenticated: () => void;
};

export function renderAuthPage(
    app: HTMLDivElement,
    options: AuthPageOptions
): void {
    let mode: 'login' | 'register' = 'login';

    function render(): void {
        const registering = mode === 'register';

        app.innerHTML = `
            <div class="auth-page">
                <div class="container">
                    <header>
                        <h1>Staff Management</h1>
                        <p>
                            ${registering
            ? 'Register your organisation to get started'
            : 'Sign in using your work email address'}
                        </p>
                    </header>

                    <section class="form-section">
                        <h2>
                            ${registering
            ? 'Register Organisation'
            : 'Login'}
                        </h2>

                        <form id="authForm">

                            ${registering ? `
                                <input
                                    name="organisationName"
                                    placeholder="Organisation Name"
                                    required
                                >

                                <select name="organisationType" required>
                                    <option value="">Select Organisation Type</option>
                                    <option value="University">University</option>
                                    <option value="Hospital">Hospital</option>
                                    <option value="Company">Company</option>
                                    <option value="School">School</option>
                                    <option value="Other">Other</option>
                                </select>

                                <textarea
                                    name="description"
                                    placeholder="Organisation Description (optional)"
                                    rows="3"
                                ></textarea>
                            ` : ''}

                            <input
                                name="email"
                                type="email"
                                placeholder="Work Email Address"
                                autocomplete="email"
                                required
                            >

                            <input
                                name="password"
                                type="password"
                                placeholder="Password"
                                autocomplete="${registering
            ? 'new-password'
            : 'current-password'}"
                                ${registering ? 'minlength="8"' : ''}
                                required
                            >

                            <div class="form-buttons">
                                <button type="submit">
                                    ${registering
            ? 'Register Organisation'
            : 'Login'}
                                </button>

                                <button type="button" id="switchAuth">
                                    ${registering
            ? 'Already registered? Login'
            : 'Register a New Organisation'}
                                </button>
                            </div>

                        </form>

                        <p id="authError" class="error-message"></p>
                        <div id="registrationResult"></div>
                    </section>
                </div>
            </div>
        `;

        attachEvents();
    }

    function attachEvents(): void {
        document.querySelector('#switchAuth')
            ?.addEventListener('click', () => {
                mode = mode === 'register' ? 'login' : 'register';
                render();
            });

        document.querySelector<HTMLFormElement>('#authForm')
            ?.addEventListener('submit', async event => {
                event.preventDefault();

                const form = event.currentTarget as HTMLFormElement;
                const formData = new FormData(form);

                const errorElement =
                    document.querySelector<HTMLElement>('#authError');

                const resultElement =
                    document.querySelector<HTMLElement>(
                        '#registrationResult'
                    );

                const submitButton =
                    form.querySelector<HTMLButtonElement>(
                        'button[type="submit"]'
                    );

                if (submitButton) {
                    submitButton.disabled = true;
                    submitButton.textContent = 'Please wait...';
                }

                if (errorElement) {
                    errorElement.textContent = '';
                }

                if (resultElement) {
                    resultElement.innerHTML = '';
                }

                try {
                    if (mode === 'register') {
                        const result = await registerOrganisation({
                            organisationName: String(
                                formData.get('organisationName')
                            ).trim(),
                            organisationType: String(
                                formData.get('organisationType')
                            ),
                            description: String(
                                formData.get('description') ?? ''
                            ).trim(),
                            email: String(
                                formData.get('email')
                            ).trim(),
                            password: String(
                                formData.get('password')
                            )
                        });

                        if (resultElement) {
                            const heading =
                                document.createElement('h3');

                            heading.textContent =
                                'Organisation Registered Successfully';

                            const message =
                                document.createElement('p');

                            message.textContent =
                                `Welcome to Staff Management, ${result.organisationName}.`;

                            const details =
                                document.createElement('p');

                            details.textContent =
                                'Your administrator account has been created. You can now access your organisation dashboard.';

                            resultElement.append(
                                heading,
                                message,
                                details
                            );
                        }

                        options.onAuthenticated();

                    } else {
                        await login({
                            email: String(
                                formData.get('email')
                            ).trim(),
                            password: String(
                                formData.get('password')
                            )
                        });

                        options.onAuthenticated();
                    }

                } catch (error) {
                    console.error(error);

                    if (errorElement) {
                        errorElement.textContent =
                            error instanceof Error
                                ? error.message
                                : 'Authentication failed.';
                    }
                } finally {
                    if (submitButton) {
                        submitButton.disabled = false;
                        submitButton.textContent =
                            mode === 'register'
                                ? 'Register Organisation'
                                : 'Login';
                    }
                }
            });
    }

    render();
}