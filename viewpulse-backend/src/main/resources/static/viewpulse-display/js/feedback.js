// feedback.js - submits feedback and redirects to feedback-success.html showing detected emotion
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('feedbackForm');
    let statusEl = document.getElementById('feedbackStatus');
    if (!statusEl && form && form.parentNode) {
        statusEl = document.createElement('div');
        statusEl.id = 'feedbackStatus';
        statusEl.style.display = 'none';
        form.parentNode.insertBefore(statusEl, form);
    }
    if (!form) {
        console.error('feedbackForm not found');
        return;
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const phone = document.getElementById('customerPhone')?.value.trim() || '';
        const message = document.getElementById('feedbackText')?.value.trim() || '';

        if (statusEl) { statusEl.style.display = 'block'; statusEl.textContent = 'Submitting feedback...'; }

        try {
            const locationId = localStorage.getItem('deviceLocationId') || null;
            const deviceId = localStorage.getItem('deviceId') || null;

            const payload = {
                locationId: locationId ? Number(locationId) : null,
                customerPhone: phone && phone.length ? String(phone) : null,
                feedbackText: (message === null || message === undefined) ? '' : String(message),
                deviceId: deviceId ? Number(deviceId) : null
            };

            // prefer API_BASE_URL from api.js if present; fallback to same-origin /api
            const base = (typeof API_BASE_URL !== 'undefined') ? API_BASE_URL.replace(/\/$/,'') : (window.location.origin.replace(/\/$/,'') + '/api');

            const resp = await fetch(`${base}/feedback/submit`, {
                method: 'POST',
                headers: {'Content-Type':'application/json'},
                body: JSON.stringify(payload)
            });

            const data = await resp.json();

            if (data && data.success) {
                // Build URL to success page with emotion and feedback id
                const emotion = encodeURIComponent(data.emotion || '');
                const confidence = encodeURIComponent(typeof data.confidence !== 'undefined' ? data.confidence : '');
                const fid = encodeURIComponent(data.feedback_id || '');
                // Optional: include original message (URL-encode) if you want to show it (be careful with length)
                const params = `?emotion=${emotion}&confidence=${confidence}&feedback_id=${fid}`;
                // redirect to success page
                window.location.href = 'feedback-success.html' + params;
            } else {
                const msg = data && data.message ? data.message : 'Failed to submit feedback';
                if (statusEl) { statusEl.textContent = '❌ ' + msg; statusEl.className='error'; statusEl.style.display='block'; }
            }
        } catch (err) {
            console.error('Feedback error', err);
            if (statusEl) { statusEl.textContent = '❌ Connection error. Please try again.'; statusEl.className='error'; statusEl.style.display='block'; }
        }
    });
});

// --- Back to ads page helper (needed by feedback.html button) ---
function goBackToAds() {
    try {
        window.location.href = 'ads.html';
    } catch (e) {
        console.error("goBackToAds error", e);
        window.location.href = '/ads.html';
    }
}
