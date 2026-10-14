/**
 * CampusTrack — Student Profile & API Configuration
 */
document.addEventListener('DOMContentLoaded', async () => {
    Auth.requireAuth();
    Auth.initNavbar();

    const profileForm = document.getElementById('profileForm');
    const passwordForm = document.getElementById('passwordForm');
    const apiConfigForm = document.getElementById('apiConfigForm');
    const testApiBtn = document.getElementById('testApiBtn');
    const apiStatusMessage = document.getElementById('apiStatusMessage');

    async function loadProfile() {
        try {
            const student = await ApiClient.get('/auth/me');
            if (student) {
                document.getElementById('profileName').value = student.name || '';
                document.getElementById('profileEmail').value = student.email || '';
                document.getElementById('profileDepartment').value = student.department || '';
                document.getElementById('profileCollege').value = student.college || '';
                document.getElementById('profileAdmissionYear').value = student.admissionYear || 2022;
                document.getElementById('profileGradingScale').value = student.gradingScale || '10-POINT';

                document.getElementById('heroStudentName').textContent = student.name;
                document.getElementById('heroStudentDept').textContent = `${student.department} &bull; Class of ${student.admissionYear + 4}`;
            }
        } catch (e) {
            console.error('Failed to load profile:', e);
            // Fallback to local user
            const local = ApiClient.getUser();
            if (local) {
                document.getElementById('profileName').value = local.name || '';
                document.getElementById('profileEmail').value = local.email || '';
                document.getElementById('profileDepartment').value = local.department || '';
                document.getElementById('profileCollege').value = local.college || '';
                document.getElementById('heroStudentName').textContent = local.name;
            }
        }

        // Populate API Base URL input
        const currentUrlInput = document.getElementById('customApiBaseUrl');
        if (currentUrlInput) {
            currentUrlInput.value = AppConfig.getApiBaseUrl();
        }
    }

    if (profileForm) {
        profileForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('profileName').value.trim();
            const dept = document.getElementById('profileDepartment').value.trim();
            const college = document.getElementById('profileCollege').value.trim();
            const year = parseInt(document.getElementById('profileAdmissionYear').value);
            const scale = document.getElementById('profileGradingScale').value;

            try {
                const updated = await ApiClient.put('/auth/profile', {
                    name,
                    department: dept,
                    college: college,
                    admissionYear: year,
                    gradingScale: scale
                });
                ApiClient.setUser(updated);
                ApiClient.showToast('Profile updated successfully!', 'success');
                Auth.initNavbar();
            } catch (err) {
                ApiClient.showToast(err.message || 'Failed to update profile', 'danger');
            }
        });
    }

    if (passwordForm) {
        passwordForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const oldPassword = document.getElementById('oldPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (newPassword !== confirmPassword) {
                ApiClient.showToast('New passwords do not match!', 'danger');
                return;
            }

            try {
                await ApiClient.put('/auth/password', { oldPassword, newPassword });
                ApiClient.showToast('Password changed successfully!', 'success');
                passwordForm.reset();
            } catch (err) {
                ApiClient.showToast(err.message || 'Failed to change password', 'danger');
            }
        });
    }

    // API Config Form
    if (apiConfigForm) {
        apiConfigForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const newUrl = document.getElementById('customApiBaseUrl').value.trim();
            AppConfig.setApiBaseUrl(newUrl);
            ApiClient.showToast('API endpoint configuration updated!', 'success');
            Auth.updateConnectivityPill();
        });

        document.getElementById('resetApiBtn')?.addEventListener('click', () => {
            AppConfig.setApiBaseUrl(null);
            document.getElementById('customApiBaseUrl').value = AppConfig.getApiBaseUrl();
            ApiClient.showToast('Reset to default auto-detected endpoint.', 'info');
            Auth.updateConnectivityPill();
        });
    }

    if (testApiBtn) {
        testApiBtn.addEventListener('click', async () => {
            apiStatusMessage.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> Testing connection...`;
            apiStatusMessage.className = 'text-info small mt-2';

            const health = await AppConfig.checkHealth();
            if (health.online) {
                apiStatusMessage.innerHTML = `<i class="bi bi-check-circle-fill text-success me-1"></i> Connected! Server Java: ${health.data?.data?.javaVersion || '17'}`;
                apiStatusMessage.className = 'text-success small mt-2';
            } else {
                apiStatusMessage.innerHTML = `<i class="bi bi-x-circle-fill text-danger me-1"></i> Failed to connect to ${health.url}`;
                apiStatusMessage.className = 'text-danger small mt-2';
            }
            Auth.updateConnectivityPill();
        });
    }

    loadProfile();
});
