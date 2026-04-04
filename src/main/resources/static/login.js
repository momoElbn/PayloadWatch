// login.js
document.addEventListener('DOMContentLoaded', () => {

    // theme setup
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

    // login handler

    // skip if logged in
    if (localStorage.getItem('jwt')) {
        window.location.href = 'index.html';
    }

    document.getElementById('loginForm').addEventListener('submit', async function(event) {
        event.preventDefault(); // prevent reload

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        try {
            // fetch config from backend
            const configResponse = await fetch('/api/public/config');
            if (!configResponse.ok) throw new Error('Failed to load configuration');
            const config = await configResponse.json();

            const clientId = config.cognitoClientId;
            const region = config.cognitoRegion;

            // call cognito
            const response = await fetch(`https://cognito-idp.${region}.amazonaws.com/`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-amz-json-1.1',
                    'X-Amz-Target': 'AWSCognitoIdentityProviderService.InitiateAuth'
                },
                body: JSON.stringify({
                    AuthFlow: "USER_PASSWORD_AUTH",
                    ClientId: clientId,
                    AuthParameters: {
                        "USERNAME": email,
                        "PASSWORD": password
                    }
                })
            });

            const data = await response.json();

            if (response.ok && data.AuthenticationResult) {
                // get token
                const jwt = data.AuthenticationResult.IdToken;

                // save token
                localStorage.setItem('jwt', jwt);

                console.log("Login successful!");

                // go to dashboard
                window.location.href = "/index.html";
            } else {
                console.error("Login failed:", data.message);
                alert("Login failed: " + (data.message || "Invalid credentials"));
            }

        } catch (error) {
            console.error("Network error during login:", error);
            alert("Could not connect to the authentication server.");
        }
    });
});