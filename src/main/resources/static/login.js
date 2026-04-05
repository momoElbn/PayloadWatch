// login.js
document.addEventListener('DOMContentLoaded', () => {

    // theme setup
    const loginThemeSelect = document.getElementById('loginThemeSelect');
    const savedTheme = localStorage.getItem('payloadwatch_theme') || 'light';

    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
        loginThemeSelect.value = 'dark';
    }

    loginThemeSelect.addEventListener('change', (e) => {
        if (e.target.value === 'dark') {
            document.body.classList.add('dark-mode');
            localStorage.setItem('payloadwatch_theme', 'dark');
        } else {
            document.body.classList.remove('dark-mode');
            localStorage.setItem('payloadwatch_theme', 'light');
        }
    });

    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const emailError = document.getElementById('loginEmailError');
    const passwordError = document.getElementById('loginPasswordError');
    const formError = document.getElementById('loginFormError');
    const successMessage = document.getElementById('loginSuccessMessage');
    const loginBtn = document.getElementById('loginBtn');

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('verified') === 'true' && successMessage) {
        successMessage.classList.add('show');
        // Remove query params so refresh does not repeatedly display the banner.
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    function setFieldError(inputEl, errorEl, message) {
        if (inputEl) inputEl.classList.add('error');
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.classList.add('show');
        }
    }

    function clearFieldError(inputEl, errorEl) {
        if (inputEl) inputEl.classList.remove('error');
        if (errorEl) {
            errorEl.textContent = '';
            errorEl.classList.remove('show');
        }
    }

    function setFormError(message) {
        if (!formError) return;
        formError.textContent = message;
        formError.classList.add('show');
    }

    function clearFormError() {
        if (!formError) return;
        formError.textContent = '';
        formError.classList.remove('show');
    }

    function clearSuccessMessage() {
        if (!successMessage) return;
        successMessage.classList.remove('show');
    }

    [emailInput, passwordInput].forEach((input) => {
        if (!input) return;
        input.addEventListener('input', () => {
            clearFormError();
            clearSuccessMessage();
            if (input === emailInput) clearFieldError(emailInput, emailError);
            if (input === passwordInput) clearFieldError(passwordInput, passwordError);
        });
    });

    const togglePasswordBtn = document.getElementById('toggleLoginPassword');
    if (togglePasswordBtn && passwordInput) {
        const eyeOpenIcon = togglePasswordBtn.querySelector('.eye-open');
        const eyeClosedIcon = togglePasswordBtn.querySelector('.eye-closed');

        function syncPasswordToggleState() {
            const isVisible = passwordInput.type === 'text';
            togglePasswordBtn.setAttribute('aria-pressed', String(isVisible));
            togglePasswordBtn.setAttribute('aria-label', isVisible ? 'Hide password' : 'Show password');
            if (eyeOpenIcon) eyeOpenIcon.classList.toggle('is-hidden', !isVisible);
            if (eyeClosedIcon) eyeClosedIcon.classList.toggle('is-hidden', isVisible);
        }

        syncPasswordToggleState();

        togglePasswordBtn.addEventListener('click', () => {
            passwordInput.type = passwordInput.type === 'password' ? 'text' : 'password';
            syncPasswordToggleState();
        });
    }

    // skip if logged in
    if (localStorage.getItem('jwt')) {
        window.location.href = 'index.html';
    }

    document.getElementById('loginForm').addEventListener('submit', async function(event) {
        event.preventDefault();

        clearFieldError(emailInput, emailError);
        clearFieldError(passwordInput, passwordError);
        clearFormError();
        clearSuccessMessage();

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        if (!email) {
            setFieldError(emailInput, emailError, 'Email is required.');
            return;
        }
        if (!password) {
            setFieldError(passwordInput, passwordError, 'Password is required.');
            return;
        }

        try {
            loginBtn.disabled = true;
            loginBtn.innerText = 'Signing In...';

            // fetch config from backend
            const configResponse = await fetch('/api/public/config');
            if (!configResponse.ok) throw new Error('Failed to load configuration');
            const config = await configResponse.json();

            const clientId = config.cognitoClientId;
            const region = config.cognitoRegion;

            // call cognito
            const response = await fetch(`https://cognito-idp.${region}.amazonaws.com/`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-amz-json-1.1',
                    'X-Amz-Target': 'AWSCognitoIdentityProviderService.InitiateAuth'
                },
                body: JSON.stringify({
                    AuthFlow: 'USER_PASSWORD_AUTH',
                    ClientId: clientId,
                    AuthParameters: {
                        USERNAME: email,
                        PASSWORD: password
                    }
                })
            });

            const data = await response.json();

            if (response.ok && data.AuthenticationResult) {
                const jwt = data.AuthenticationResult.IdToken;
                localStorage.setItem('jwt', jwt);
                window.location.href = '/index.html';
                return;
            }

            const message = data.message || 'Invalid credentials.';
            if (message.toLowerCase().includes('password')) {
                setFieldError(passwordInput, passwordError, message);
            } else if (message.toLowerCase().includes('user') || message.toLowerCase().includes('email')) {
                setFieldError(emailInput, emailError, message);
            } else {
                setFormError(message);
            }
        } catch (error) {
            console.error('Network error during login:', error);
            setFormError('Could not connect to the authentication server.');
        } finally {
            loginBtn.disabled = false;
            loginBtn.innerText = 'Sign In';
        }
    });
});