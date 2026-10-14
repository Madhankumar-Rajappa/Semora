/**
 * CampusTrack — Unified HTTP API Client & Toast Notification System
 * Reusable API service handling HTTP requests, auth tokens, error handling, and toast feedback.
 */
const ApiClient = (function () {
    const TOKEN_KEY = 'CAMPUS_TRACK_TOKEN';
    const USER_KEY = 'CAMPUS_TRACK_USER';
    const DEV_STUDENT_KEY = 'CAMPUS_TRACK_DEV_STUDENT_ID';

    function getToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function setToken(token) {
        if (token) localStorage.setItem(TOKEN_KEY, token);
        else localStorage.removeItem(TOKEN_KEY);
    }

    function getUser() {
        try {
            const raw = localStorage.getItem(USER_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch (e) {
            return null;
        }
    }

    function setUser(user) {
        if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
        else localStorage.removeItem(USER_KEY);
    }

    function getDevStudentId() {
        const id = localStorage.getItem(DEV_STUDENT_KEY);
        return id ? parseInt(id) : 1; // Default to Student ID 1 (Alex Morgan)
    }

    function setDevStudentId(id) {
        localStorage.setItem(DEV_STUDENT_KEY, String(id));
        // Also clear normal token so dev header takes effect
        localStorage.removeItem(TOKEN_KEY);
    }

    function logout() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        localStorage.removeItem(DEV_STUDENT_KEY);
        window.location.href = 'index.html';
    }

    async function request(endpoint, options = {}) {
        const baseUrl = AppConfig.getApiBaseUrl();
        const url = `${baseUrl}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;

        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        const token = getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        } else {
            // Development fallback token scoped to active test student ID (1 or 2)
            const devId = getDevStudentId();
            headers['Authorization'] = `Bearer dev-student-${devId}`;
        }

        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 10000); // 10s timeout

            const response = await fetch(url, {
                ...options,
                headers,
                signal: controller.signal
            });
            clearTimeout(timeoutId);

            if (response.status === 401) {
                // If token invalid, remove it
                setToken(null);
                setUser(null);
                showToast('Authentication required or session expired.', 'warning');
                throw new Error('Unauthorized');
            }

            let data;
            const text = await response.text();
            try {
                data = text ? JSON.parse(text) : {};
            } catch (jsonErr) {
                throw new Error(`Server returned non-JSON response (${response.status})`);
            }

            if (!response.ok || data.success === false) {
                const message = data.message || `Request failed with status ${response.status}`;
                const err = new Error(message);
                err.status = response.status;
                err.data = data;
                throw err;
            }

            return data.data;
        } catch (err) {
            if (err.name === 'AbortError') {
                throw new Error('Request timed out. Please check if the Java backend server is running.');
            }
            if (err.message && err.message.includes('Failed to fetch')) {
                throw new Error('Cannot connect to backend server at ' + baseUrl + '. Please check that Apache Tomcat is running.');
            }
            throw err;
        }
    }

    // Toast notification utility
    function showToast(message, type = 'info') {
        let container = document.getElementById('toastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            container.style.zIndex = '1090';
            document.body.appendChild(container);
        }

        const toastId = 'toast_' + Date.now();
        const bgClass = type === 'success' ? 'bg-teal-custom text-white' :
                        type === 'danger' || type === 'error' ? 'bg-danger text-white' :
                        type === 'warning' ? 'bg-warning text-dark' : 'bg-primary-custom text-white';

        const toastEl = document.createElement('div');
        toastEl.id = toastId;
        toastEl.className = `toast align-items-center ${bgClass} border-0 shadow-lg`;
        toastEl.setAttribute('role', 'alert');
        toastEl.setAttribute('aria-live', 'assertive');
        toastEl.setAttribute('aria-atomic', 'true');

        toastEl.innerHTML = `
            <div class="d-flex align-items-center">
                <div class="toast-body fw-medium py-3 px-3 flex-grow-1">
                    ${message}
                </div>
                <button type="button" class="btn-close ${type === 'warning' ? '' : 'btn-close-white'} me-3 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        `;

        container.appendChild(toastEl);
        if (window.bootstrap && window.bootstrap.Toast) {
            const bsToast = new bootstrap.Toast(toastEl, { delay: 4000 });
            bsToast.show();
            toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
        } else {
            // Fallback if bootstrap JS is still loading
            setTimeout(() => toastEl.remove(), 4000);
        }
    }

    return {
        getToken,
        setToken,
        getUser,
        setUser,
        getDevStudentId,
        setDevStudentId,
        logout,
        get: (endpoint) => request(endpoint, { method: 'GET' }),
        post: (endpoint, body) => request(endpoint, { method: 'POST', body: JSON.stringify(body) }),
        put: (endpoint, body) => request(endpoint, { method: 'PUT', body: JSON.stringify(body) }),
        patch: (endpoint, body) => request(endpoint, { method: 'PATCH', body: JSON.stringify(body) }),
        delete: (endpoint) => request(endpoint, { method: 'DELETE' }),
        showToast
    };
})();

window.ApiClient = ApiClient;
