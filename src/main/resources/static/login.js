// login.js
document.addEventListener('DOMContentLoaded', () => {

    // --- 1. Theme Management for Login Page ---
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

    // --- 2. Login Flow ---

    // If already logged in, kick them straight to the dashboard
    if (localStorage.getItem('jwt')) {
        window.location.href = 'index.html';
    }

    document.getElementById('loginForm').addEventListener('submit', async function(event) {
        event.preventDefault(); // Stop the page from reloading

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        // AWS Configuration
        const clientId = "276ooik5vjov8ijgjfutv6vn4b";
        const region = "ca-central-1";

        try {
            // Make the direct API call to AWS Cognito
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
                // Success! Grab the IdToken
                const jwt = data.AuthenticationResult.IdToken;

                // Save it securely in the browser
                localStorage.setItem('jwt', jwt);

                console.log("Login successful!");

                // Redirect the user to your PayloadWatch dashboard
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