/**
 * CampusTrack — Student Sign In Controller
 */
document.addEventListener('DOMContentLoaded', () => {
    // If student is already logged in, redirect to dashboard
    Auth.redirectIfAuthenticated();
    Auth.updateConnectivityPill();

    const form = document.getElementById('loginForm');
    const emailInput = document.getElementById('loginEmail');
    const passwordInput = document.getElementById('loginPassword');
    const submitBtn = document.getElementById('loginSubmitBtn');
    const alertContainer = document.getElementById('alertContainer');
    const alertText = document.getElementById('alertText');
    const successContainer = document.getElementById('successContainer');
    const toggleBtn = document.getElementById('toggleLoginPasswordBtn');

    // Parse URL params for registration callback
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('registered') === 'true') {
        successContainer.classList.remove('d-none');
    }
    const emailParam = urlParams.get('email');
    if (emailParam) {
        emailInput.value = emailParam;
        passwordInput.focus();
    }

    // Password visibility toggle
    if (toggleBtn && passwordInput) {
        toggleBtn.addEventListener('click', () => {
            const isPassword = passwordInput.type === 'password';
            passwordInput.type = isPassword ? 'text' : 'password';
            toggleBtn.innerHTML = isPassword ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
        });
    }

    function showAlert(msg) {
        alertText.textContent = msg;
        alertContainer.classList.remove('d-none');
        successContainer.classList.add('d-none');
    }

    function hideAlert() {
        alertContainer.classList.add('d-none');
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        if (!email) {
            showAlert('Please enter your email address.');
            emailInput.focus();
            return;
        }

        if (!password) {
            showAlert('Please enter your password.');
            passwordInput.focus();
            return;
        }

        submitBtn.disabled = true;
        submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status"></span> Signing In...`;

        try {
            const result = await Auth.login(email, password);

            if (result.success) {
                submitBtn.innerHTML = `<i class="bi bi-check-circle me-1"></i> Success! Redirecting...`;
                // Auth.login automatically redirects to dashboard.html
            } else {
                showAlert(result.message || 'Invalid email or password. Please verify your credentials.');
                submitBtn.disabled = false;
                submitBtn.innerHTML = `<i class="bi bi-box-arrow-in-right me-1"></i> Sign In`;
                passwordInput.value = '';
                passwordInput.focus();
            }
        } catch (err) {
            showAlert(err.message || 'Could not connect to the backend server. Please verify the API is running.');
            submitBtn.disabled = false;
            submitBtn.innerHTML = `<i class="bi bi-box-arrow-in-right me-1"></i> Sign In`;
        }
    });
});
