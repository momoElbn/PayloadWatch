document.getElementById('verifyForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const code = document.getElementById('verificationCode').value;
    // We need the email again. Easiest way is to grab it from a URL param
    // or just ask the user to type it again.
    const email = new URLSearchParams(window.location.search).get('email');

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
        alert("Account verified! You can now log in.");
        window.location.href = "login.html";
    } else {
        const data = await response.json();
        alert("Verification failed: " + data.message);
    }
});