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
    const contractsContainer = document.getElementById('contractsContainer');

    let monitorToDelete = null;

    // --- 1. Dashboard Initialization & Auto-Refresh ---
    loadMonitors();
    setInterval(loadMonitors, 60000); // 60s Heartbeat

    async function loadMonitors() {
        // Render Skeleton Loader
        tableCard.style.display = 'block';
        emptyState.style.display = 'none';
        tableBody.innerHTML = `
            <tr><td colspan="6"><div class="skeleton"></div></td></tr>
            <tr><td colspan="6"><div class="skeleton" style="width: 80%"></div></td></tr>
        `;

        try {
            // Uses the new API object!
            const monitors = await API.request('/monitors') || [];

            if (monitors.length === 0) {
                tableCard.style.display = 'none';
                emptyState.style.display = 'block';
            } else {
                renderTable(monitors);
            }
        } catch (error) {
            window.showToast('Failed to load monitors', 'error');
            tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--status-down);">Failed to load data.</td></tr>`;
        }
    }

    function renderTable(monitors) {
        tableBody.innerHTML = '';
        monitors.forEach(m => {
            const statusColor = m.status === 'UP' ? 'up' : 'down';
            
            // Expected backend property is "isActive" 
            const isActive = m.isActive !== false; 
            const activityClass = isActive ? 'active' : 'inactive';
            const activityText = isActive ? 'Active' : 'Inactive';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><a href="#" class="btn-link" style="color: var(--primary-green); font-weight:600;" onclick="window.openHistory(${m.id}, '${m.name}')">${m.name}</a></td>
                <td style="font-family: monospace; color: var(--text-secondary);">
                    <span class="method-badge method-${m.httpMethod || 'GET'}">${m.httpMethod || 'GET'}</span>
                    ${m.url}
                </td>
                <td>${m.interval} min</td>
                <td><span class="status-dot ${statusColor}"></span>${m.status}</td>
                <td>
                    <button id="activity-btn-${m.id}" class="btn btn-activity ${activityClass}" onclick="window.toggleActivity(${m.id}, true)">
                        Loading...
                    </button>
                </td>
                <td class="cell-actions">
                    <button class="btn btn-secondary" style="padding: 6px 12px; font-size:12px;" onclick='window.editMonitor(${JSON.stringify(m)})'>Edit</button>
                    <button class="btn btn-danger" onclick="window.confirmDelete(${m.id}, '${m.name}')">Delete</button>
                </td>
            `;
            tableBody.appendChild(tr);

            // Fetch the actual activity status from the backend
            fetchActivityStatus(m.id);
        });
    }

    async function fetchActivityStatus(id) {
        try {
            const isActive = await API.request(`/monitors/${id}/activity`);
            const btn = document.getElementById(`activity-btn-${id}`);
            if (btn) {
                btn.className = `btn btn-activity ${isActive ? 'active' : 'inactive'}`;
                btn.innerText = isActive ? 'Active' : 'Inactive';
                btn.setAttribute('onclick', `window.toggleActivity(${id}, ${isActive})`);
            }
        } catch (error) {
            console.error(`Failed to fetch activity status for monitor ${id}`, error);
        }
    }

    // --- Activity Toggle Skeleton ---
    window.toggleActivity = async (id, currentStatus) => {
        try {
            const newStatus = !currentStatus;

            // Optimistically update the UI button immediately
            const btn = document.getElementById(`activity-btn-${id}`);
            if (btn) {
                btn.className = `btn btn-activity ${newStatus ? 'active' : 'inactive'}`;
                btn.innerText = newStatus ? 'Active' : 'Inactive';
                btn.setAttribute('onclick', `window.toggleActivity(${id}, ${newStatus})`);
            }
            
            // Append isActive as a query parameter to match @RequestParam
            await API.request(`/monitors/${id}/activity?isActive=${newStatus}`, {
                method: 'PATCH'
            });

            window.showToast(`Monitor ${newStatus ? 'activated' : 'deactivated'}`, 'success');
            // Remove loadMonitors() to stop it from overwriting the UI with stale backend state
        } catch (err) {
            // Revert on error
            const btn = document.getElementById(`activity-btn-${id}`);
            if (btn) {
                btn.className = `btn btn-activity ${currentStatus ? 'active' : 'inactive'}`;
                btn.innerText = currentStatus ? 'Active' : 'Inactive';
                btn.setAttribute('onclick', `window.toggleActivity(${id}, ${currentStatus})`);
            }
            window.showToast('Failed to change monitor activity', 'error');
        }
    };

    // --- 2. Modal Management ---
    window.openMonitorModal = () => {
        monitorForm.reset();
        document.getElementById('monitorId').value = '';
        document.getElementById('modalTitle').innerText = 'Create Monitor';
        document.getElementById('monitorMethod').value = 'GET';

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

        contractsContainer.innerHTML = '';
        if (monitor.contracts && monitor.contracts.length > 0) {
            monitor.contracts.forEach(c => window.addContractRow(c.expectedKey, c.expectedType));
        } else {
            window.addContractRow();
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

        const contractRows = document.querySelectorAll('.contract-row');
        const contractsArray = [];
        contractRows.forEach(row => {
            const key = row.querySelector('.contract-key').value.trim();
            const type = row.querySelector('.contract-type').value;
            if (key) {
                contractsArray.push({ expectedKey: key, expectedType: type });
            }
        });

        const id = document.getElementById('monitorId').value;
        const payload = {
            name: document.getElementById('monitorName').value,
            url: document.getElementById('monitorUrl').value,
            httpMethod: document.getElementById('monitorMethod').value,
            interval: document.getElementById('monitorInterval').value,
            contracts: contractsArray
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

    // --- 4. Log History ---
    function renderHistoryRows(logs = []) {
        const tbody = document.getElementById('historyTableBody');
        tbody.innerHTML = '';

        if (logs.length === 0) {
            tbody.innerHTML = `<tr><td colspan="3" style="text-align:center; color: var(--text-secondary);">No logs found for this period.</td></tr>`;
            return;
        }

        logs.forEach(log => {
            const isUp = log.statusCode >= 200 && log.statusCode < 300;
            const dotClass = isUp ? 'up' : 'down';

            let latencyColor = 'var(--status-up)';
            if (log.latency > 800) latencyColor = 'var(--status-down)';
            else if (log.latency >= 200) latencyColor = 'var(--latency-warn)';

            // Include error message if status is down and message exists
            let errorHtml = '';
            if (!isUp && log.errorMessage) {
                // If it's down and we have a message, create a tiny red subtitle
                errorHtml = `<div style="font-size: 11px; color: var(--status-down); margin-top: 4px; line-height: 1.2;">
                                ${log.errorMessage}
                             </div>`;
            }

            tbody.innerHTML += `
                <tr>
                    <td style="color: var(--text-secondary); vertical-align: top; padding-top: 14px;">${log.timestamp}</td>
                    <td style="vertical-align: top; padding-top: 12px;">
                        <div><span class="status-dot ${dotClass}"></span>${log.statusCode}</div>
                        ${errorHtml} </td>
                    <td style="color: ${latencyColor}; font-family: monospace; font-weight: 600; vertical-align: top; padding-top: 14px;">${log.latency} ms</td>
                </tr>
            `;
        });
    }

    async function loadHistory(monitorId, range = '24h') {
        const tbody = document.getElementById('historyTableBody');
        tbody.innerHTML = `<tr><td colspan="3"><div class="skeleton"></div></td></tr>`;

        try {
            const logs = await API.request(`/health-logs/${monitorId}?range=${encodeURIComponent(range)}`) || [];
            renderHistoryRows(logs);
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="3" style="color: var(--status-down);">Failed to fetch history</td></tr>`;
        }
    }

    window.openHistory = async (id, name) => {
        document.getElementById('historyModalTitle').innerText = `Log History: ${name}`;
        document.getElementById('currentMonitorId').value = id;
        historyModal.classList.add('active');

        const activeRangeBtn = document.querySelector('.btn-range.active');
        const range = activeRangeBtn ? activeRangeBtn.getAttribute('data-range') : '24h';
        await loadHistory(id, range || '24h');
    };

    // --- 5. UX Polish: Toasts & Time Ranges ---
    window.showToast = (message, type = 'success') => {
        const container = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerText = message;
        container.appendChild(toast);
        toast.offsetHeight;
        toast.classList.add('show');
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    };

    document.querySelectorAll('.btn-range').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            document.querySelectorAll('.btn-range').forEach(b => b.classList.remove('active'));
            e.target.classList.add('active');

            const range = e.target.getAttribute('data-range') || '24h';
            const monitorId = document.getElementById('currentMonitorId').value;
            if (!monitorId) return;
            await loadHistory(monitorId, range);
        });
    });

    // --- 6. Dynamic Contract Builder Logic ---
    window.addContractRow = (key = '', type = 'STRING') => {
        const row = document.createElement('div');
        row.className = 'contract-row';
        row.style.cssText = 'display: flex; gap: 10px; align-items: center;';

        row.innerHTML = `
        <input type="text" class="contract-key" placeholder="e.g., status" value="${key}" style="flex: 2;" required>
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

    document.getElementById('addContractBtn').addEventListener('click', () => {
        window.addContractRow();
    });

    // --- 7. Logout Flow ---
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.removeItem('jwt');
            window.location.href = 'login.html';
        });
    }
});