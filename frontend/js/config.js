/**
 * CampusTrack — Global Application Configuration
 * Dynamically resolves API Base URL between local development (Tomcat) and production Vercel deployment.
 * Supports VITE_API_BASE_URL environment variable convention.
 */
const AppConfig = (function () {
    // Default local Tomcat endpoint
    const LOCAL_DEFAULT = 'http://localhost:8080/api';
    
    // Default production backend placeholder (overridden by VITE_API_BASE_URL or UI settings)
    const PROD_DEFAULT = 'https://campustrack-backend.up.railway.app/api';

    function getStoredBaseUrl() {
        return localStorage.getItem('CAMPUS_TRACK_API_BASE');
    }

    function setStoredBaseUrl(url) {
        if (!url || url.trim() === '') {
            localStorage.removeItem('CAMPUS_TRACK_API_BASE');
        } else {
            let clean = url.trim().replace(/\/+$/, '');
            localStorage.setItem('CAMPUS_TRACK_API_BASE', clean);
        }
    }

    function resolveApiBaseUrl() {
        // 1. User manual override from UI modal or local storage
        const stored = getStoredBaseUrl();
        if (stored) return stored;

        // 2. Query parameter override e.g. ?api=http://localhost:8080/api
        try {
            const urlParams = new URLSearchParams(window.location.search);
            const queryApi = urlParams.get('api');
            if (queryApi && queryApi.trim()) {
                const clean = queryApi.trim().replace(/\/+$/, '');
                setStoredBaseUrl(clean);
                return clean;
            }
        } catch (ignored) {}

        // 3. Meta tag injected by build system / Vercel
        const metaTag = document.querySelector('meta[name="api-base-url"]');
        if (metaTag && metaTag.content && metaTag.content.trim() !== '') {
            return metaTag.content.trim().replace(/\/+$/, '');
        }

        // 4. Window environment object (e.g., from VITE_API_BASE_URL / env.js)
        if (window.__ENV__ && window.__ENV__.VITE_API_BASE_URL) {
            return window.__ENV__.VITE_API_BASE_URL.replace(/\/+$/, '');
        }
        if (window.__ENV__ && window.__ENV__.API_BASE_URL) {
            return window.__ENV__.API_BASE_URL.replace(/\/+$/, '');
        }
        if (window.VITE_API_BASE_URL) {
            return window.VITE_API_BASE_URL.replace(/\/+$/, '');
        }

        // 5. Hostname inspection: use local Tomcat if running on localhost/127.0.0.1 or file://
        const isLocal = window.location.hostname === 'localhost' || 
                        window.location.hostname === '127.0.0.1' || 
                        window.location.protocol === 'file:';

        return isLocal ? LOCAL_DEFAULT : PROD_DEFAULT;
    }

    // Health check helper to test backend connectivity
    async function checkBackendHealth() {
        const base = resolveApiBaseUrl();
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 4000);
            const res = await fetch(`${base}/health`, { signal: controller.signal });
            clearTimeout(timeoutId);
            if (res.ok) {
                const data = await res.json();
                return { online: true, url: base, data };
            }
        } catch (e) {
            // Unreachable
        }
        return { online: false, url: base };
    }

    return {
        getApiBaseUrl: resolveApiBaseUrl,
        setApiBaseUrl: setStoredBaseUrl,
        getStoredApiBaseUrl: getStoredBaseUrl,
        checkHealth: checkBackendHealth,
        LOCAL_DEFAULT,
        PROD_DEFAULT
    };
})();

// Attach to window
window.AppConfig = AppConfig;
