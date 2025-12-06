/*
  admin-login.js — safer login flow
  - uses window.location.origin to avoid mixed content
  - stores adminLoggedIn flag and full response
  - stores token/adminLocationId if present
  - small delay then location.replace to avoid back-forward loops
*/
document.addEventListener('DOMContentLoaded', () => {
  // Use window.location.origin + /api for flexibility across deployment types
  const API_BASE_URL = window.location.origin + '/api';
  const form = document.getElementById('adminLoginForm');
  const msg = document.getElementById('loginMessage');

  if (!form) { console.error('adminLoginForm not found'); return; }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = (document.getElementById('username') && document.getElementById('username').value.trim()) || '';
    const password = (document.getElementById('password') && document.getElementById('password').value.trim()) || '';

    if (msg) { msg.textContent = 'Logging in...'; msg.className = 'message'; msg.style.display = 'block'; }

    try {
      const res = await fetch(`${API_BASE_URL}/admin/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
        credentials: 'same-origin'
      });

      const txt = await res.text();
      let data;
      try { data = JSON.parse(txt); } catch (err) { data = { success: false, message: 'Invalid JSON from server', raw: txt }; }

      console.log('DEBUG admin-login response:', data);

      if (data && data.success) {
        try {
          // store common fields defensively
          if (data.admin_id) localStorage.setItem('adminId', String(data.admin_id));
          if (data.adminId) localStorage.setItem('adminId', String(data.adminId));
          if (data.id) localStorage.setItem('adminId', String(data.id));

          const storedUsername = data.username || data.user || username || '';
          localStorage.setItem('adminUsername', storedUsername);

          const role = (data.role || data.admin_role || data.adminRole || '').toString();
          // normalized role (!lowercase to match checks)
          const normalized = role.toLowerCase();
          // keep original style as used by other scripts: e.g., 'super_admin' or 'system_admin' etc.
          if (normalized) localStorage.setItem('adminRole', normalized);

          // token (optional)
          if (data.token) localStorage.setItem('adminToken', data.token);

          // location id (optional)
          if (data.location_id) localStorage.setItem('adminLocationId', String(data.location_id));
          if (data.locationId) localStorage.setItem('adminLocationId', String(data.locationId));

          // always set a explicit flag used by auth checks
          localStorage.setItem('adminLoggedIn', 'true');

          // store full raw response for debugging
          localStorage.setItem('adminLoginResponseRaw', JSON.stringify(data));
        } catch (err) {
          console.warn('Could not write full login info to localStorage', err);
        }

        if (msg) { msg.textContent = '✅ Login successful — redirecting...'; msg.className = 'message success'; }

        // Wait a short time to ensure storage is flushed and other pages can read it,
        // then redirect using replace (prevents back button bouncing).
        setTimeout(() => {
          const role = (localStorage.getItem('adminRole') || '').toLowerCase();
          
          // CRITICAL FIX: All redirects must include the folder prefix: /viewpulse-admin/
          if (role === 'super_admin') location.replace('/viewpulse-admin/super-admin-dashboard.html');
          else if (role === 'system_admin') location.replace('/viewpulse-admin/system-admin-dashboard.html');
          else if (role === 'location_owner') location.replace('/viewpulse-admin/dashboard.html');
          else location.replace('/viewpulse-admin/dashboard.html');
        }, 350);
      } else {
        // failed login
        if (msg) {
          msg.textContent = '❌ ' + (data && data.message ? data.message : 'Login failed');
          msg.className = 'message error';
        }
        console.warn('Login failed response:', data);
      }
    } catch (err) {
      console.error('Admin login fetch error:', err);
      if (msg) { msg.textContent = '❌ Connection error. Check /api proxy & backend'; msg.className = 'message error'; }
    }
  });
});