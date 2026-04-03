// dashboard.js
document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const tableBody = document.getElementById('monitorTableBody');
    const tableCard = document.getElementById('tableCard');
    const emptyState = document.getElementById('emptyState');

    // Modal Elements
    const monitorModal = document.getElementById('monitorModal');
    const historyModal = document.getElementById('historyModal');
    const deleteModal = document.getElementById('deleteModal');
    const monitorForm = document.getElementById('monitorForm');

    let monitorToDelete = null; // State for delete modal

    // --- 1. Dashboard Initialization & Auto-Refresh ---
    loadMonitors();
    setInterval(loadMonitors, 60000); // 60s Heartbeat

    async function loadMonitors() {
        // Render Skeleton Loader
        tableCard.style.display = 'block';
        emptyState.style.display = 'none';
        tableBody.innerHTML = `
            <tr><td colspan="5"><div class="skeleton"></div></td></tr>
            <tr><td colspan="5"><div class="skeleton" style="width: 80%"></div></td></tr>
        `;

        try {
            const monitors = await API.request('/monitors');

            if (monitors.length === 0) {
                tableCard.style.display = 'none';
                emptyState.style.display = 'block';
            } else {
                renderTable(monitors);
            }
        } catch (error) {
            window.showToast('Failed to load monitors', 'error');
            tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center; color: var(--status-down);">Failed to load data.</td></tr>`;
        }
    }

    function renderTable(monitors) {
        tableBody.innerHTML = '';
        monitors.forEach(m => {
            const tr = document.createElement('tr');
            const statusColor = m.status === 'UP' ? 'up' : 'down';

            tr.innerHTML = `
                <td><a href="#" class="btn-link" style="color: var(--primary-green); font-weight:600;" onclick="window.openHistory(${m.id}, '${m.name}')">${m.name}</a></td>
                <td style="font-family: monospace; color: var(--text-secondary);">
                    <span class="method-badge method-${m.httpMethod || 'GET'}">${m.httpMethod || 'GET'}</span>
                    ${m.url}
                </td>
                <td>${m.interval} min</td>
                <td><span class="status-dot ${statusColor}"></span>${m.status}</td>
                <td class="cell-actions">
                    <button class="btn btn-secondary" style="padding: 6px 12px; font-size:12px;" onclick='window.editMonitor(${JSON.stringify(m)})'>Edit</button>
                    <button class="btn btn-danger" onclick="window.confirmDelete(${m.id}, '${m.name}')">Delete</button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    }

    // --- 2. Modal Management ---
    window.openMonitorModal = () => {
        monitorForm.reset();
        document.getElementById('monitorId').value = '';
        document.getElementById('modalTitle').innerText = 'Create Monitor';
        document.getElementById('monitorMethod').value = 'GET';

        // Clear contracts and add one empty row by default
        contractsContainer.innerHTML = '';
        window.addContractRow();

        monitorModal.classList.add('active');
    };

    window.editMonitor = (monitor) => {
        document.getElementById('monitorId').value = monitor.id;
        document.getElementById('monitorName').value = monitor.name;
        document.getElementById('monitorUrl').value = monitor.url;
        document.getElementById('monitorInterval').value = monitor.interval;
        document.getElementById('monitorMethod').value = monitor.httpMethod || 'GET';
        document.getElementById('modalTitle').innerText = 'Edit Monitor';

        // Populate dynamic contract rows
        contractsContainer.innerHTML = '';
        if (monitor.contracts && monitor.contracts.length > 0) {
            monitor.contracts.forEach(c => window.addContractRow(c.expected_key, c.expected_type));
        } else {
            window.addContractRow(); // Fallback empty row
        }

        monitorModal.classList.add('active');
    };

    window.closeModals = () => {
        document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
    };

    monitorForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const btn = document.getElementById('saveMonitorBtn');
        const originalText = btn.innerText;
        btn.innerText = 'Saving...';
        btn.disabled = true;

        // Scrape the dynamic contract rows
        const contractRows = document.querySelectorAll('.contract-row');
        const contractsArray = [];
        contractRows.forEach(row => {
            const key = row.querySelector('.contract-key').value.trim();
            const type = row.querySelector('.contract-type').value;
            if (key) {
                contractsArray.push({ expected_key: key, expected_type: type });
            }
        });

        const id = document.getElementById('monitorId').value;
        const payload = {
            name: document.getElementById('monitorName').value,
            url: document.getElementById('monitorUrl').value,
            httpMethod: document.getElementById('monitorMethod').value,
            interval: document.getElementById('monitorInterval').value,
            contracts: contractsArray // Sends clean array to Spring Boot!
        };

        try {
            await API.request(id ? `/monitors/${id}` : '/monitors', {
                method: id ? 'PUT' : 'POST',
                body: JSON.stringify(payload)
            });
            window.showToast('Monitor saved successfully', 'success');
            window.closeModals();
            loadMonitors();
        } catch (err) {
            window.showToast('Error saving monitor', 'error');
        } finally {
            btn.innerText = originalText;
            btn.disabled = false;
        }
    });

    // --- 3. Delete Flow ---
    window.confirmDelete = (id, name) => {
        monitorToDelete = id;
        document.getElementById('deleteMonitorName').innerText = name;
        deleteModal.classList.add('active');
    };

    document.getElementById('confirmDeleteBtn').addEventListener('click', async () => {
        if (!monitorToDelete) return;

        try {
            await API.request(`/monitors/${monitorToDelete}`, { method: 'DELETE' });
            window.showToast('Monitor deleted', 'success');
            window.closeModals();
            loadMonitors();
        } catch (err) {
            window.showToast('Failed to delete monitor', 'error');
        }
    });

    // --- 4. Log History & Latency Colors ---
    window.openHistory = async (id, name) => {
        document.getElementById('historyModalTitle').innerText = `Log History: ${name}`;

        document.getElementById('currentMonitorId').value = id;

        historyModal.classList.add('active');

        const tbody = document.getElementById('historyTableBody');
        tbody.innerHTML = `<tr><td colspan="3"><div class="skeleton"></div></td></tr>`;

        try {
            const logs = await API.request(`/logs/${id}`);
            tbody.innerHTML = '';

            logs.forEach(log => {
                const isUp = log.status.includes('200');
                const dotClass = isUp ? 'up' : 'down';

                // Earthy Latency Coloring
                let latencyColor = 'var(--status-up)'; // Green < 200
                if (log.latency > 800) latencyColor = 'var(--status-down)'; // Red > 800
                else if (log.latency >= 200) latencyColor = 'var(--latency-warn)'; // Amber 200-800

                tbody.innerHTML += `
                    <tr>
                        <td style="color: var(--text-secondary);">${new Date(log.timestamp).toLocaleString()}</td>
                        <td><span class="status-dot ${dotClass}"></span>${log.status}</td>
                        <td style="color: ${latencyColor}; font-family: monospace; font-weight: 600;">${log.latency} ms</td>
                    </tr>
                `;
            });
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="3" style="color: var(--status-down);">Failed to fetch history</td></tr>`;
        }
    };

    // --- 5. UX Polish: Toasts ---
    window.showToast = (message, type = 'success') => {
        const container = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerText = message;

        container.appendChild(toast);

        // Trigger reflow for animation
        toast.offsetHeight;
        toast.classList.add('show');

        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    };

    // --- 6. Time Range Buttons in History Modal ---
    document.querySelectorAll('.btn-range').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            // 1. UI Switch
            document.querySelectorAll('.btn-range').forEach(b => b.classList.remove('active'));
            e.target.classList.add('active');

            // 2. Fetch new data
            const range = e.target.getAttribute('data-range');
            const monitorId = document.getElementById('currentMonitorId').value;

            console.log(`Fetching logs for Monitor ${monitorId} over the last ${range}`);
            // await API.request(`/logs/${monitorId}?range=${range}`);
            // updateHistoryTable(newData);
        });
    });

// --- 7. Dynamic Contract Builder Logic ---
    const contractsContainer = document.getElementById('contractsContainer');

    window.addContractRow = (key = '', type = 'STRING') => {
        const row = document.createElement('div');
        row.className = 'contract-row';
        row.style.cssText = 'display: flex; gap: 10px; align-items: center;';

        row.innerHTML = `
        <input type="text" class="contract-key" placeholder="e.g., database_status" value="${key}" style="flex: 2;" required>
        <select class="contract-type" style="flex: 1;">
            <option value="STRING" ${type === 'STRING' ? 'selected' : ''}>String</option>
            <option value="NUMBER" ${type === 'NUMBER' ? 'selected' : ''}>Number</option>
            <option value="BOOLEAN" ${type === 'BOOLEAN' ? 'selected' : ''}>Boolean</option>
            <option value="OBJECT" ${type === 'OBJECT' ? 'selected' : ''}>Object</option>
            <option value="ARRAY" ${type === 'ARRAY' ? 'selected' : ''}>Array</option>
        </select>
        <button type="button" class="btn btn-danger" style="padding: 10px; line-height: 1;" onclick="this.parentElement.remove()">&times;</button>
    `;
        contractsContainer.appendChild(row);
    };

// Wire up the "+ Add Key" button
    document.getElementById('addContractBtn').addEventListener('click', () => {
        window.addContractRow();
    });
});