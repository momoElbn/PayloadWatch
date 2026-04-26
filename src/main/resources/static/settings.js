// settings.js - SPA Routing, Theme Management, and Settings Logic

document.addEventListener('DOMContentLoaded', () => {
    // setup section
    const navDashboard = document.getElementById('navDashboard');
    const navSettings = document.getElementById('navSettings');
    const dashboardView = document.getElementById('dashboardView');
    const settingsView = document.getElementById('settingsView');
    const headerTitle = document.querySelector('.header h1');
    const headerCreateBtn = document.querySelector('.header .btn-primary');

    navDashboard.addEventListener('click', (e) => {
        e.preventDefault();
        navSettings.classList.remove('active');
        navDashboard.classList.add('active');
        settingsView.style.display = 'none';
        dashboardView.style.display = 'block';
        headerTitle.innerText = 'Monitors';
        headerCreateBtn.style.display = 'block';
    });

    navSettings.addEventListener('click', (e) => {
        e.preventDefault();
        navDashboard.classList.remove('active');
        navSettings.classList.add('active');
        dashboardView.style.display = 'none';
        settingsView.style.display = 'block';
        headerTitle.innerText = 'Settings';
        headerCreateBtn.style.display = 'none';
        
        // Ensure data is fresh every time the user opens the Settings view
        if (typeof window.loadSettings === 'function') {
            window.loadSettings();
        }
    });

    // setup section
    const settingsTabs = document.querySelectorAll('.settings-tab');
    const settingsPanes = document.querySelectorAll('.settings-pane');

    settingsTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            settingsTabs.forEach(t => t.classList.remove('active'));
            settingsPanes.forEach(p => p.classList.remove('active'));
            tab.classList.add('active');
            const targetId = tab.getAttribute('data-target');
            document.getElementById(targetId).classList.add('active');
        });
    });

    // setup section
    const themeSelect = document.getElementById('themeSelect');
    const timezoneSelect = document.getElementById('timezoneSelect');
    const emailAlertToggle = document.getElementById('emailAlertToggle'); // Assuming this is a checkbox

    window.loadSettings = async function() {
        try {
            // Uses your global api.js object!
            const user = await API.request('/users/me');

            if (user) {
                // Populate the text fields (Make sure these IDs match your HTML spans/divs!)
                const emailEl = document.getElementById('settingEmail');
                const accountIdEl = document.getElementById('settingAccountId');
                const planEl = document.getElementById('settingPlan');
                const countEl = document.getElementById('settingMonitorCount');

                if (emailEl) emailEl.value = user.email;

                if (headerCreateBtn) {
                    headerCreateBtn.style.opacity = '1';
                    headerCreateBtn.style.pointerEvents = 'auto';
                    headerCreateBtn.innerText = '+ Create Monitor';
                }

                // Set the toggles to match DB state
                if (timezoneSelect) timezoneSelect.value = user.timeZonePreference || 'UTC';

                if (themeSelect) {
                    themeSelect.value = user.themePreference || 'light';
                    // Apply theme immediately on load based on DB preference
                    if (themeSelect.value === 'dark') {
                        document.body.classList.add('dark-mode');
                        localStorage.setItem('payloadwatch_theme', 'dark');
                    }
                }
                if (emailAlertToggle) emailAlertToggle.checked = user.emailAlertsEnabled;
            }
        } catch (error) {
            console.error("Failed to load user settings:", error);
        }
    };

    // Load everything when page opens
    window.loadSettings();

    // setup section
    async function saveSettingsToServer() {
        const payload = {
            emailAlertsEnabled: emailAlertToggle ? emailAlertToggle.checked : false,
            themePreference: themeSelect ? themeSelect.value : 'light',
            timeZonePreference: timezoneSelect ? timezoneSelect.value : 'UTC'
        };

        try {
            await API.request('/users/me/settings', {
                method: 'PUT',
                body: JSON.stringify(payload)
            });
            if (window.showToast) window.showToast('Preferences saved!', 'success');
        } catch (error) {
            if (window.showToast) window.showToast('Error saving preferences', 'error');
        }
    }

    // Attach listeners to trigger the save function whenever a user changes an input
    if (timezoneSelect) timezoneSelect.addEventListener('change', saveSettingsToServer);
    if (emailAlertToggle) emailAlertToggle.addEventListener('change', saveSettingsToServer);

    // Theme needs special logic to update the UI instantly before saving to server
    if (themeSelect) {
        themeSelect.addEventListener('change', (e) => {
            if (e.target.value === 'dark') {
                document.body.classList.add('dark-mode');
                localStorage.setItem('payloadwatch_theme', 'dark');
            } else {
                document.body.classList.remove('dark-mode');
                localStorage.setItem('payloadwatch_theme', 'light');
            }
            // Save the choice to the database!
            saveSettingsToServer();
        });
    }

    // Password Change Logic
    const changePasswordForm = document.getElementById('changePasswordForm');
    if (changePasswordForm) {
        changePasswordForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const errorEl = document.getElementById('changePasswordError');
            const btn = document.getElementById('changePasswordBtn');
            
            if (errorEl) {
                errorEl.textContent = '';
                errorEl.classList.remove('show');
            }

            try {
                btn.disabled = true;
                btn.innerText = 'Updating...';

                await API.request('/users/me/password', {
                    method: 'PUT',
                    body: JSON.stringify({ currentPassword, newPassword })
                });

                if (window.showToast) window.showToast('Password updated successfully!', 'success');
                changePasswordForm.reset();
            } catch (error) {
                if (errorEl) {
                    errorEl.textContent = error.message || 'Error updating password';
                    errorEl.classList.add('show');
                }
            } finally {
                btn.disabled = false;
                btn.innerText = 'Update Password';
            }
        });
    }
});
