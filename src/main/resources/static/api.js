// api.js
// resolve api base url
const API_BASE_URL = window.location.hostname === 'localhost' && window.location.port !== '8080' ? "http://localhost:8080/api" : "/api";

const API = {
    async request(endpoint, options = {}) {
        const token = localStorage.getItem('jwt');

        // enforce auth
        if (!token) {
            window.location.href = 'login.html';
            return null;
        }

        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`, // pass token
            ...options.headers
        };

        const config = { 
            ...options, 
            headers,
            cache: 'no-store'
        };

        try {
            console.log(`[Network] ${config.method || 'GET'} ${API_BASE_URL}${endpoint}`);
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

            // refresh expired token
            if (response.status === 401) {
                localStorage.removeItem('jwt');
                window.location.href = 'login.html';
                return null;
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `API Error: ${response.status}`);
            }

            // check empty content
            if (response.status === 204) {
                return null;
            }

            // parse body
            const contentType = response.headers.get('content-type') || '';
            if (contentType.includes('application/json')) {
                return await response.json();
            }

            return null;

        } catch (error) {
            console.error('API Request Failed:', error);
            throw error;
        }
    }
};