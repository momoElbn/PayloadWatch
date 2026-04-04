document.addEventListener('DOMContentLoaded', () => {
    // 1. Grab the email from the URL
    const urlParams = new URLSearchParams(window.location.search);
    const email = urlParams.get('email');
    const errorDiv = document.getElementById('verifyError');

    // 2. Safety Check: If no email is in the URL, kick them back to login/signup
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
            const response = await fetch(`https://cognito-idp.ca-central-1.amazonaws.com/`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-amz-json-1.1',
                    'X-Amz-Target': 'AWSCognitoIdentityProviderService.ConfirmSignUp'
                },
                body: JSON.stringify({
                    ClientId: "276ooik5vjov8ijgjfutv6vn4b",
                    Username: email,
                    ConfirmationCode: code
                })
            });

            if (response.ok) {
                // Success! Redirect to login with a success flag
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