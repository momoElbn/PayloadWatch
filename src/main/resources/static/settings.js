// settings.js - SPA Routing, Theme Management, and Settings Logic

document.addEventListener('DOMContentLoaded', () => {
    // --- 1. Main SPA Routing (Dashboard vs Settings) ---
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
        headerCreateBtn.style.display = 'none'; // Hide "+ Create Monitor" on Settings page
    });


    // --- 2. Settings Sub-Navigation Logic ---
    const settingsTabs = document.querySelectorAll('.settings-tab');
    const settingsPanes = document.querySelectorAll('.settings-pane');

    settingsTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            // Remove active state from all tabs and panes
            settingsTabs.forEach(t => t.classList.remove('active'));
            settingsPanes.forEach(p => p.classList.remove('active'));

            // Add active state to clicked tab and corresponding pane
            tab.classList.add('active');
            const targetId = tab.getAttribute('data-target');
            document.getElementById(targetId).classList.add('active');
        });
    });


    // --- 3. Deep Earth Theme Management ---
    const themeSelect = document.getElementById('themeSelect');

    // Check local storage for saved theme preference
    const savedTheme = localStorage.getItem('payloadwatch_theme') || 'light';

    // Apply on initial load
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
        themeSelect.value = 'dark';
    }

    // Listen for dropdown changes
    themeSelect.addEventListener('change', (e) => {
        const selectedTheme = e.target.value;
        if (selectedTheme === 'dark') {
            document.body.classList.add('dark-mode');
            localStorage.setItem('payloadwatch_theme', 'dark');
        } else {
            document.body.classList.remove('dark-mode');
            localStorage.setItem('payloadwatch_theme', 'light');
        }
    });

    // --- 4. Interactive Toggles (Mock Functionality) ---
    const timezoneSelect = document.getElementById('timezoneSelect');
    const timeFormatToggle = document.getElementById('timeFormatToggle');
    const emailAlertToggle = document.getElementById('emailAlertToggle');

    [timezoneSelect, timeFormatToggle, emailAlertToggle].forEach(element => {
        element.addEventListener('change', () => {
            // In a real app, this would hit a PUT /api/user/preferences endpoint
            if (window.showToast) {
                window.showToast('Preference saved.', 'success');
            }
        });
    });
});