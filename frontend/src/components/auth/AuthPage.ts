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
            : 'Sign in to your organisation'}
                        </p>
                    </header>

                    <section class="form-section">
                        <h2>
                            ${registering
            ? 'Register Organisation'
            : 'Organisation Login'}
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
                            ` : `
                                <input
                                    name="organisationId"
                                    placeholder="Organisation ID"
                                    required
                                >

                                <small>
                                    Enter the Organisation ID provided during registration.
                                </small>
                            `}

                            <input
                                name="email"
                                type="email"
                                placeholder="Email Address"
                                required
                            >

                            <input
                                name="password"
                                type="password"
                                placeholder="Password"
                                minlength="8"
                                required
                            >

                            <div class="form-buttons">
                                <button type="submit">
                                    ${registering ? 'Register Organisation' : 'Login'}
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
                    document.querySelector<HTMLElement>('#registrationResult');

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
                            resultElement.innerHTML = `
                                <div class="form-section">
                                    <h3>Organisation Registered Successfully</h3>
                                    <p>Your Organisation ID is:</p>
                                    <strong id="newOrganisationId">
                                        ${result.organisationId}
                                    </strong>
                                    <button type="button" id="copyOrganisationId">
                                        Copy Organisation ID
                                    </button>
                                    <p>
                                        Keep this ID safe. You will need it whenever
                                        you log in.
                                    </p>
                                </div>
                            `;

                            document.querySelector('#copyOrganisationId')
                                ?.addEventListener('click', async () => {
                                    try {
                                        await navigator.clipboard.writeText(
                                            result.organisationId
                                        );

                                        const button =
                                            document.querySelector<HTMLButtonElement>(
                                                '#copyOrganisationId'
                                            );

                                        if (button) {
                                            button.textContent = 'Copied!';
                                        }
                                    } catch {
                                        if (errorElement) {
                                            errorElement.textContent =
                                                'Unable to copy the ID. Please copy it manually.';
                                        }
                                    }
                                });
                        }
                    } else {
                        await login({
                            organisationId: String(
                                formData.get('organisationId')
                            ).trim(),
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