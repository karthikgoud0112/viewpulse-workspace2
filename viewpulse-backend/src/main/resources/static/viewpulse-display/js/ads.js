/*
  schedule-aware ads player for ViewPulse display
  - polls /api/ad-schedule/device/<id>/active
  - uses priority ordering from backend
*/
(() => {
  const DEVICE_ID = parseInt(localStorage.getItem('deviceId') || localStorage.getItem('device_id') || '1', 10);
  const API_BASE = window.location.origin + '/api'; 
  const POLL_MS = 20 * 1000; // 20 seconds
  let currentScheduleId = null;

  let videoEl = document.getElementById('videoPlayer') || document.getElementById('player');
  if (!videoEl) {
    videoEl = document.createElement('video');
    videoEl.id = 'player';
    videoEl.autoplay = true;
    videoEl.muted = true;
    videoEl.controls = false;
    videoEl.style.width = '100%';
    videoEl.playsInline = true;
    document.body.appendChild(videoEl);
  }

  async function fetchSchedules() {
    try {
      // Endpoint returns videos already filtered by active schedule & sorted by priority
      const res = await fetch(`${API_BASE}/ad-schedule/device/${DEVICE_ID}/active`); 
      const j = await res.json();
      if (!j || !j.success) return [];
      return Array.isArray(j.videos) ? j.videos : [];
    } catch (e) {
      console.error('[ads] fetchActiveVideos error', e);
      return [];
    }
  }

  function stopPlayback() {
    try {
      if (!videoEl) return;
      videoEl.pause();
      try { videoEl.removeAttribute('src'); } catch(e) {}
      try { videoEl.load(); } catch(e) {}
      currentScheduleId = null;
      console.log('[ads] stopped playback - no active video');
    } catch (e) {
      console.error('[ads] stopPlayback error', e);
    }
  }

  function safeUrlFromPath(path) {
    if (!path) return null;
    const base = (window.location.hostname === '127.0.0.1' || window.location.hostname === 'localhost')
        ? 'http://127.0.0.1:8080' 
        : window.location.origin.replace(/\/$/,'');
    return `${base}/uploads/videos/${encodeURIComponent(path).replace(/%2F/g, '/')}`;
  }

  function playVideo(v) {
    if (!v) return stopPlayback();
    const path = v.video_path || v.videoPath || v.path; 
    const url = safeUrlFromPath(path);
    if (!url) return stopPlayback();
    
    if (currentScheduleId === v.video_id && videoEl.src === url) {
      return;
    }

    currentScheduleId = v.video_id;
    videoEl.src = url;
    videoEl.load();
    videoEl.play().catch(err => {
      console.warn('[ads] play failed', err);
    });
    
    videoEl.onended = () => {
      setTimeout(() => pollLoop(), 300);
    };
  }

  async function pollLoop() {
    const activeVideos = await fetchSchedules();
    
    if (!activeVideos || activeVideos.length === 0) {
      stopPlayback();
      return setTimeout(pollLoop, POLL_MS);
    }

    // Play the highest priority video that is marked active
    const highestPriorityVideo = activeVideos.find(v => v.video_active !== false);

    if (highestPriorityVideo) {
      playVideo(highestPriorityVideo);
    } else {
      stopPlayback();
    }
    
    return setTimeout(pollLoop, POLL_MS);
  }

  if (typeof checkDeviceAuth === 'function' && !checkDeviceAuth()) {
      return; 
  }

  pollLoop();
})();