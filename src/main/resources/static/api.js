// api.js
const API = {
    async request(endpoint, options = {}) {
        const token = Auth.getToken();

        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = { ...options, headers };

        try {
            // === MOCK API LOGIC (REPLACE WITH REAL FETCH LATER) ===
            console.log(`[API Interceptor] ${config.method || 'GET'} /api${endpoint}`);
            return await this.mockNetworkResponse(endpoint, config);

            /* === REAL SPRING BOOT FETCH LOGIC ===
            const response = await fetch(`/api${endpoint}`, config);

            if (response.status === 401) {
                Auth.logout();
                throw new Error('Unauthorized');
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `API Error: ${response.status}`);
            }

            // Handle 204 No Content for DELETE
            if (response.status === 204) return null;
            return await response.json();
            ========================================= */

        } catch (error) {
            console.error('API Request Failed:', error);
            throw error;
        }
    },

    // Realistic Mock Data Generator
    mockNetworkResponse(endpoint, config) {
        return new Promise((resolve) => {
            setTimeout(() => { // Simulate latency
                if (endpoint === '/monitors' && (!config.method || config.method === 'GET')) {
                    resolve([
                        { id: 1, name: 'Production API', url: 'https://api.myapp.com/v1', interval: 1, status: 'UP', contracts: [{ expected_key: 'status', expected_type: 'STRING' }] },
                        { id: 2, name: 'Payment Gateway', url: 'https://payments.myapp.com/ping', interval: 5, status: 'DOWN', contracts: [] }
                    ]);
                } else if (endpoint.startsWith('/logs/')) {
                    resolve([
                        { timestamp: new Date().toISOString(), status: '200 OK', latency: 150 },
                        { timestamp: new Date(Date.now() - 60000).toISOString(), status: '500 ERROR', latency: 950 },
                        { timestamp: new Date(Date.now() - 120000).toISOString(), status: '200 OK', latency: 450 }
                    ]);
                } else {
                    // Mocks POST/PUT/DELETE success
                    resolve({ success: true });
                }
            }, 600);
        });
    }
};