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
        // logout and redirect
        window.location.href = 'login.html';
    },

    isAuthenticated() {
        return !!this.getToken();
    }
};

// route guard
document.addEventListener('DOMContentLoaded', () => {
    // enforce auth on protected pages
    if (!window.location.pathname.includes('login.html')) {
        if (!Auth.isAuthenticated()) {
            window.location.replace('login.html');
        }
    }

    // handle logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            Auth.logout();
        });
    }
});