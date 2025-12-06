// API helper - points to local backend for kiosk, otherwise to same origin /api
const API_BASE_URL = (function(){
    // If kiosk uses local backend inside server, uncomment the next line:
    // return 'http://127.0.0.1:8080/api';
    // Otherwise use current origin + /api (works when reverse-proxy or same host)
    return window.location.hostname === '127.0.0.1' || window.location.hostname === 'localhost'
        ? 'http://127.0.0.1:8080/api'
        : window.location.origin.replace(/\/$/, '') + '/api';
})();

// login helper used across pages
function checkDeviceAuth() {
    try {
        const loggedIn = localStorage.getItem('deviceLoggedIn') === 'true';
        const onLoginPage = window.location.pathname.endsWith('login.html');
        if (!loggedIn && !onLoginPage) window.location.href = 'login.html';
        return loggedIn;
    } catch(e) {
        return false;
    }
}

function getLocationId() {
    const v = localStorage.getItem('deviceLocationId') || localStorage.getItem('locationId');
    return v ? parseInt(v,10) : null;
}

async function submitFeedbackToBackend(payload) {
    const res = await fetch(`${API_BASE_URL.replace(/\/$/,'')}/feedback/submit`, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify(payload)
    });
    return res.json();
}

async function fetchVideosForLocation(locationId) {
    if (!locationId) return { success:false, videos:[] };
    const res = await fetch(`${API_BASE_URL.replace(/\/$/,'').replace(/\/api$/,'')}/api/video/location/${locationId}`, {cache:'no-store'});
    return res.ok ? res.json() : { success:false, videos:[] };
}
