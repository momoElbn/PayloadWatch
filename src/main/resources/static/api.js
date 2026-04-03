// api.js
const API_BASE_URL = "http://localhost:8080/api";

const API = {
    async request(endpoint, options = {}) {
        const token = localStorage.getItem('jwt');

        // If no token exists, kick them back to login immediately
        if (!token) {
            window.location.href = 'login.html';
            return null;
        }

        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`, // The VIP Pass
            ...options.headers
        };

        const config = { ...options, headers };

        try {
            console.log(`[Network] ${config.method || 'GET'} ${API_BASE_URL}${endpoint}`);
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

            // If Spring Security rejects the token (expired or invalid)
            if (response.status === 401) {
                localStorage.removeItem('jwt');
                window.location.href = 'login.html';
                return null;
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `API Error: ${response.status}`);
            }

            // Handle 204 No Content (Used for DELETE requests)
            if (response.status === 204) {
                return null;
            }

            // Return JSON data
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