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
            window.location.href = 'login.html';
        }
    }

    // Redirect logged in user from landing page / auth pages
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
                id: data.studentId || data.id,
                name: data.name || data.fullName,
                fullName: data.fullName || data.name,
                registrationNumber: data.registrationNumber,
                email: data.email,
                department: data.department,
                college: data.college || data.collegeName,
                collegeName: data.collegeName || data.college,
                admissionYear: data.admissionYear,
                currentAcademicYear: data.currentAcademicYear
            });
            ApiClient.showToast(`Welcome back, ${data.name || 'Student'}!`, 'success');
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 600);
            return { success: true, data };
        } catch (error) {
            return { success: false, message: error.message || 'Invalid email or password.' };
        }
    }

    // Register a new student
    async function register(studentData) {
        try {
            const data = await ApiClient.post('/auth/register', studentData);
            // After successful registration, redirect to Sign In page
            ApiClient.showToast('Registration successful! Please sign in.', 'success');
            setTimeout(() => {
                window.location.href = `login.html?registered=true&email=${encodeURIComponent(studentData.email || '')}`;
            }, 1000);
            return { success: true, data };
        } catch (error) {
            return { success: false, message: error.message || 'Registration failed.' };
        }
    }

    // Render student profile name in navbar & initialize connectivity pill
    function initNavbar() {
        const user = ApiClient.getUser();
        const userNameEl = document.getElementById('navStudentName');
        const userDeptEl = document.getElementById('navStudentDept');
        const userRegEl = document.getElementById('navStudentReg');
        if (userNameEl && user) {
            userNameEl.textContent = user.name || user.fullName || 'Student';
        }
        if (userDeptEl && user) {
            userDeptEl.textContent = user.department || 'Department of Engineering';
        }
        if (userRegEl && user) {
            userRegEl.textContent = user.registrationNumber || '';
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
