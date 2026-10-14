/**
 * CampusTrack — Authentication & Session Management
 */
const Auth = (function () {
    // Check if user is logged in
    function isAuthenticated() {
        return !!ApiClient.getToken();
    }

    // Protect private pages
    function requireAuth() {
        if (!isAuthenticated()) {
            window.location.href = 'index.html';
        }
    }

    // Redirect logged in user from landing page
    function redirectIfAuthenticated() {
        if (isAuthenticated()) {
            window.location.href = 'dashboard.html';
        }
    }

    // Login with credentials
    async function login(email, password) {
        try {
            const data = await ApiClient.post('/auth/login', { email, password });
            ApiClient.setToken(data.token);
            ApiClient.setUser({
                id: data.studentId,
                name: data.name,
                email: data.email,
                department: data.department,
                college: data.college,
                admissionYear: data.admissionYear
            });
            ApiClient.showToast(`Welcome back, ${data.name}!`, 'success');
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 800);
            return true;
        } catch (error) {
            ApiClient.showToast(error.message || 'Login failed', 'danger');
            return false;
        }
    }

    // Register a new student
    async function register(studentData) {
        try {
            const data = await ApiClient.post('/auth/register', studentData);
            ApiClient.setToken(data.token);
            ApiClient.setUser({
                id: data.studentId,
                name: data.name,
                email: data.email,
                department: data.department,
                college: data.college,
                admissionYear: data.admissionYear
            });
            ApiClient.showToast('Account created successfully!', 'success');
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 900);
            return true;
        } catch (error) {
            ApiClient.showToast(error.message || 'Registration failed', 'danger');
            return false;
        }
    }

    // Render student profile name in navbar & initialize connectivity pill
    function initNavbar() {
        const user = ApiClient.getUser();
        const userNameEl = document.getElementById('navStudentName');
        const userDeptEl = document.getElementById('navStudentDept');
        if (userNameEl && user) {
            userNameEl.textContent = user.name || 'Student';
        }
        if (userDeptEl && user) {
            userDeptEl.textContent = user.department || 'Undergraduate';
        }

        // Logout handlers
        const logoutBtn = document.getElementById('navLogoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                ApiClient.logout();
            });
        }

        // Backend Connectivity Health Check
        updateConnectivityPill();
    }

    async function updateConnectivityPill() {
        const pill = document.getElementById('apiStatusPill');
        if (!pill) return;

        pill.innerHTML = `
            <span class="spinner-border spinner-border-sm me-1" role="status" style="width: 10px; height: 10px;"></span>
            <span>Checking API...</span>
        `;
        pill.className = 'api-pill offline';

        const health = await AppConfig.checkHealth();
        if (health.online) {
            pill.className = 'api-pill online';
            pill.innerHTML = `
                <span class="status-dot green"></span>
                <span>API Connected</span>
            `;
            pill.title = `Backend URL: ${health.url} (Active Tomcat / REST API)`;
        } else {
            pill.className = 'api-pill offline';
            pill.innerHTML = `
                <span class="status-dot yellow"></span>
                <span>API Offline (Click to Config)</span>
            `;
            pill.title = `Attempted URL: ${health.url}. Click to configure backend endpoint.`;
        }
    }

    return {
        isAuthenticated,
        requireAuth,
        redirectIfAuthenticated,
        login,
        register,
        initNavbar,
        updateConnectivityPill
    };
})();

window.Auth = Auth;
