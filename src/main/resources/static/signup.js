document.addEventListener('DOMContentLoaded', () => {

    // --- 1. Theme Management ---
    const signupThemeSelect = document.getElementById('signupThemeSelect');
    const savedTheme = localStorage.getItem('payloadwatch_theme') || 'light';

    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
        signupThemeSelect.value = 'dark';
    }

    signupThemeSelect.addEventListener('change', (e) => {
        if (e.target.value === 'dark') {
            document.body.classList.add('dark-mode');
            localStorage.setItem('payloadwatch_theme', 'dark');
        } else {
            document.body.classList.remove('dark-mode');
            localStorage.setItem('payloadwatch_theme', 'light');
        }
    });

    // --- 2. Sign Up Flow ---
    document.getElementById('signupForm').addEventListener('submit', async function(event) {
        event.preventDefault();

        // Grabbing the exact IDs from the HTML above
        const email = document.getElementById('signupEmail').value;
        const password = document.getElementById('signupPassword').value;

        // AWS Configuration
        const clientId = "276ooik5vjov8ijgjfutv6vn4b";
        const region = "ca-central-1";

        try {
            const response = await fetch(`https://cognito-idp.${region}.amazonaws.com/`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-amz-json-1.1',
                    'X-Amz-Target': 'AWSCognitoIdentityProviderService.SignUp'
                },
                body: JSON.stringify({
                    ClientId: clientId,
                    Username: email,
                    Password: password
                })
            });

            const data = await response.json();

            if (response.ok) {
                // Success!
                console.log("Sign up successful!");
                alert("Account successfully created! Please check your email for a verification code, then log in.");

                const userEmail = document.getElementById('signupEmail').value;
                window.location.href = `verify.html?email=${encodeURIComponent(userEmail)}`;
            } else {
                console.error("Sign up failed:", data.message);
                alert("Sign up failed: " + (data.message || "Please check your password requirements."));
            }

        } catch (error) {
            console.error("Network error during sign up:", error);
            alert("Could not connect to the authentication server.");
        }
    });
});