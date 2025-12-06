document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('deviceLoginForm');
    const msg = document.getElementById('loginMessage');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const deviceCode = document.getElementById('deviceCode').value.trim();
        const devicePassword = document.getElementById('devicePassword').value.trim();

        msg.textContent = "Logging in...";
        msg.className = "message";
        msg.style.display = "block";

        const res = await fetch(`${API_BASE_URL}/device/login`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                device_code: deviceCode,
                device_password: devicePassword
            })
        });

        const data = await res.json();

        if (data.success) {
            localStorage.setItem('deviceLoggedIn', 'true');
            localStorage.setItem('deviceId', data.device_id);
            localStorage.setItem('deviceCode', deviceCode);
            localStorage.setItem('deviceLocationId', data.location_id);
            window.location.href = 'ads.html';
        } else {
            msg.textContent = "❌ " + data.message;
            msg.className = "error";
        }
    });
});
