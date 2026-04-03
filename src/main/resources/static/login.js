// login.js
document.addEventListener('DOMContentLoaded', () => {

    // --- 1. Theme Management for Login Page ---
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

    // --- 2. Login Flow ---
    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');

    // If already logged in, kick them straight to the dashboard
    if (Auth.isAuthenticated()) {
        window.location.href = 'index.html';
    }

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        loginBtn.innerText = 'Authenticating...';
        loginBtn.disabled = true;

        try {
            // === MOCK COGNITO LOGIN (REPLACE WITH REAL FETCH LATER) ===
            await new Promise(resolve => setTimeout(resolve, 800)); // Simulate network delay

            // For MVP testing, any email/password works.
            // In production, this fetch will hit your Spring Boot /api/auth/login endpoint
            const mockJwtToken = 'mock-cognito-jwt-' + btoa(email);

            // Save token and redirect
            Auth.setToken(mockJwtToken);
            window.location.href = 'index.html';

        } catch (error) {
            console.error('Login failed:', error);
            alert('Invalid credentials. Please try again.');
            loginBtn.innerText = 'Sign In';
            loginBtn.disabled = false;
        }
    });
});