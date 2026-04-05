document.addEventListener('DOMContentLoaded', () => {

    // configure theme
    const signupThemeSelect = document.getElementById('signupThemeSelect');
    const savedTheme = localStorage.getItem('payloadwatch_theme') || 'light';

    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
        signupThemeSelect.value = 'dark';
    }

    signupThemeSelect.addEventListener('change', (e) => {
        if (e.target.value === 'dark') {
            document.body.classList.add('dark-mode');
            localStorage.setItem('payloadwatch_theme', 'dark');
        } else {
            document.body.classList.remove('dark-mode');
            localStorage.setItem('payloadwatch_theme', 'light');
        }
    });

    const emailInput = document.getElementById('signupEmail');
    const passwordInput = document.getElementById('signupPassword');
    const emailError = document.getElementById('signupEmailError');
    const passwordError = document.getElementById('signupPasswordError');
    const formError = document.getElementById('signupFormError');
    const signupBtn = document.getElementById('signupBtn');

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

    [emailInput, passwordInput].forEach((input) => {
        if (!input) return;
        input.addEventListener('input', () => {
            clearFormError();
            if (input === emailInput) clearFieldError(emailInput, emailError);
            if (input === passwordInput) clearFieldError(passwordInput, passwordError);
        });
    });

    const togglePasswordBtn = document.getElementById('toggleSignupPassword');
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

    // handle signup
    document.getElementById('signupForm').addEventListener('submit', async function(event) {
        event.preventDefault();

        clearFieldError(emailInput, emailError);
        clearFieldError(passwordInput, passwordError);
        clearFormError();

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
            signupBtn.disabled = true;
            signupBtn.innerText = 'Creating...';

            // fetch config from backend
            const configResponse = await fetch('/api/public/config');
            if (!configResponse.ok) throw new Error('Failed to load configuration');
            const config = await configResponse.json();

            const clientId = config.cognitoClientId;
            const region = config.cognitoRegion;

            const response = await fetch(`https://cognito-idp.${region}.amazonaws.com/`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-amz-json-1.1',
                    'X-Amz-Target': 'AWSCognitoIdentityProviderService.SignUp'
                },
                body: JSON.stringify({
                    ClientId: clientId,
                    Username: email,
                    Password: password
                })
            });

            const data = await response.json();

            if (response.ok) {
                const userEmail = emailInput.value;
                window.location.href = `verify.html?email=${encodeURIComponent(userEmail)}`;
                return;
            }

            const message = data.message || 'Please check your password requirements.';
            if (message.toLowerCase().includes('password')) {
                setFieldError(passwordInput, passwordError, message);
            } else if (message.toLowerCase().includes('email') || message.toLowerCase().includes('username')) {
                setFieldError(emailInput, emailError, message);
            } else {
                setFormError(message);
            }

        } catch (error) {
            console.error('Network error during sign up:', error);
            setFormError('Could not connect to the authentication server.');
        } finally {
            signupBtn.disabled = false;
            signupBtn.innerText = 'Sign Up';
        }
    });
});