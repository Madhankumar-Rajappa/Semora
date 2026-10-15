/**
 * CampusTrack — Student Registration Controller
 */
document.addEventListener('DOMContentLoaded', () => {
    // Redirect if student already has an active session
    Auth.redirectIfAuthenticated();
    Auth.updateConnectivityPill();

    const form = document.getElementById('registrationForm');
    const submitBtn = document.getElementById('submitRegisterBtn');
    const alertContainer = document.getElementById('alertContainer');
    const alertMessage = document.getElementById('alertMessage');
    const alertText = document.getElementById('alertText');

    // Password visibility toggles
    setupPasswordToggle('password', 'togglePasswordBtn');
    setupPasswordToggle('confirmPassword', 'toggleConfirmBtn');

    function setupPasswordToggle(inputId, toggleBtnId) {
        const input = document.getElementById(inputId);
        const btn = document.getElementById(toggleBtnId);
        if (!input || !btn) return;

        btn.addEventListener('click', () => {
            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';
            btn.innerHTML = isPassword ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
        });
    }

    function showAlert(msg, isSuccess = false) {
        alertText.textContent = msg;
        alertMessage.className = `alert ${isSuccess ? 'alert-success' : 'alert-danger'} d-flex align-items-center gap-2 py-2 px-3 rounded-3`;
        alertContainer.classList.remove('d-none');
        alertContainer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function hideAlert() {
        alertContainer.classList.add('d-none');
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const fullName = document.getElementById('fullName').value.trim();
        const registrationNumber = document.getElementById('registrationNumber').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const collegeName = document.getElementById('collegeName').value.trim();
        const department = document.getElementById('department').value.trim();
        const admissionYear = parseInt(document.getElementById('admissionYear').value, 10);
        const currentAcademicYear = document.getElementById('currentAcademicYear').value.trim();

        // 1. Validation checks
        if (!fullName) {
            showAlert('Please enter your full name.');
            document.getElementById('fullName').focus();
            return;
        }

        if (!registrationNumber) {
            showAlert('Please enter your student ID or registration number.');
            document.getElementById('registrationNumber').focus();
            return;
        }

        const emailRegex = /^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\.[A-Za-z]{2,})$/;
        if (!email || !emailRegex.test(email)) {
            showAlert('Please enter a valid email address (e.g., student@university.edu).');
            document.getElementById('email').focus();
            return;
        }

        if (!password || password.length < 6) {
            showAlert('Password must be at least 6 characters long.');
            document.getElementById('password').focus();
            return;
        }

        if (password !== confirmPassword) {
            showAlert('Passwords do not match. Please re-enter.');
            document.getElementById('confirmPassword').focus();
            return;
        }

        if (!collegeName) {
            showAlert('Please enter your college or institution name.');
            document.getElementById('collegeName').focus();
            return;
        }

        if (!department) {
            showAlert('Please enter your department or branch.');
            document.getElementById('department').focus();
            return;
        }

        if (isNaN(admissionYear) || admissionYear < 1990 || admissionYear > 2050) {
            showAlert('Please enter a valid admission year.');
            document.getElementById('admissionYear').focus();
            return;
        }

        if (!currentAcademicYear) {
            showAlert('Please specify your current academic year (e.g., 2024-2025).');
            document.getElementById('currentAcademicYear').focus();
            return;
        }

        // 2. Submit to backend
        submitBtn.disabled = true;
        submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status"></span> Registering Account...`;

        try {
            const result = await Auth.register({
                name: fullName,
                fullName: fullName,
                registrationNumber: registrationNumber,
                email: email,
                password: password,
                college: collegeName,
                collegeName: collegeName,
                department: department,
                admissionYear: admissionYear,
                currentAcademicYear: currentAcademicYear
            });

            if (result.success) {
                showAlert('Account registered successfully! Redirecting to sign in...', true);
            } else {
                showAlert(result.message || 'Registration failed. Please check your details.');
                submitBtn.disabled = false;
                submitBtn.innerHTML = `<i class="bi bi-check2-circle me-1"></i> Register Account`;
            }
        } catch (err) {
            showAlert(err.message || 'Network error connecting to backend API.');
            submitBtn.disabled = false;
            submitBtn.innerHTML = `<i class="bi bi-check2-circle me-1"></i> Register Account`;
        }
    });
});
