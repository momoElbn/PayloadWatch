document.addEventListener('DOMContentLoaded', () => {
    // Grab the email from the URL
    const urlParams = new URLSearchParams(window.location.search);
    const email = urlParams.get('email');
    const errorDiv = document.getElementById('verifyError');

    // If no email is in the URL, kick them back to login/signup
    if (!email) {
        alert("Session expired or invalid link. Please try signing up or logging in again.");
        window.location.href = "login.html";
        return;
    }

    // 3. Handle the form submission
    document.getElementById('verifyForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const code = document.getElementById('verificationCode').value;
        const btn = document.getElementById('verifyBtn');

        // UI Feedback: Disable button and hide old errors
        btn.disabled = true;
        btn.innerText = "Verifying...";
        errorDiv.style.display = 'none';

        try {
            // fetch config from backend
            const configResponse = await fetch('/api/public/config');
            if (!configResponse.ok) throw new Error('Failed to load configuration');
            const config = await configResponse.json();

            const clientId = config.cognitoClientId;
            const region = config.cognitoRegion;

            const response = await fetch(`https://cognito-idp.${region}.amazonaws.com/`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-amz-json-1.1',
                    'X-Amz-Target': 'AWSCognitoIdentityProviderService.ConfirmSignUp'
                },
                body: JSON.stringify({
                    ClientId: clientId,
                    Username: email,
                    ConfirmationCode: code
                })
            });

            if (response.ok) {
                // success Redirect to login with a success flag
                window.location.href = "login.html?verified=true";
            } else {
                const data = await response.json();
                // Show the specific AWS error to the user cleanly
                errorDiv.innerText = data.message || "Invalid verification code.";
                errorDiv.style.display = 'block';
                btn.disabled = false;
                btn.innerText = "Confirm Account";
            }
        } catch (error) {
            console.error("Fetch error:", error);
            errorDiv.innerText = "Network error. Please try again.";
            errorDiv.style.display = 'block';
            btn.disabled = false;
            btn.innerText = "Confirm Account";
        }
    });
});
