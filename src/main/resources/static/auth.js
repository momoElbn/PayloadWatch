// auth.js
const Auth = {
    TOKEN_KEY: 'payloadwatch_jwt',

    setToken(token) {
        localStorage.setItem(this.TOKEN_KEY, token);
    },

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        // Redirect to the new login page!
        window.location.href = 'login.html';
    },

    isAuthenticated() {
        return !!this.getToken();
    }
};

// Global Route Guard: Protect the dashboard
// If a user loads index.html without a token, instantly kick them to login
document.addEventListener('DOMContentLoaded', () => {
    // Only run the route guard if we are NOT on the login page
    if (!window.location.pathname.includes('login.html')) {
        if (!Auth.isAuthenticated()) {
            window.location.replace('login.html');
        }
    }

    // Wire up the sidebar Log Out button (if it exists on the page)
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            Auth.logout();
        });
    }
});