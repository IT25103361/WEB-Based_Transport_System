const SVG_ICONS = {
  dashboard: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>`,
  student: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>`,
  tracking: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="3 11 22 2 13 21 11 13 3 11"></polygon></svg>`,
  schedule: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>`,
  notifications: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>`,
  profile: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>`,
  sun: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>`,
  moon: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>`,
  bell: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>`,
  brand: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"></polygon><polyline points="2 17 12 22 22 17"></polyline><polyline points="2 12 12 17 22 12"></polyline></svg>`,
  logout: `<svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>`,
  check: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>`,
  menu: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>`,
  close: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>`
};

const Pages = {
  dashboard: ["Dashboard", "dashboard", SVG_ICONS.dashboard],
  student: ["My Student", "student", SVG_ICONS.student],
  tracking: ["Live Tracking", "tracking", SVG_ICONS.tracking],
  schedule: ["Schedule", "schedule", SVG_ICONS.schedule],
  notifications: ["Notifications", "notifications", SVG_ICONS.notifications],
  profile: ["Profile", "profile", SVG_ICONS.profile]
};

/* -----------------------------------------------------------------------------
   Theme Engine (Light Theme as DEFAULT)
   ----------------------------------------------------------------------------- */
function getAppTheme() {
  return localStorage.getItem("nextgo_theme") || "light";
}

function setAppTheme(theme) {
  const t = theme === "dark" ? "dark" : "light";
  localStorage.setItem("nextgo_theme", t);
  document.documentElement.setAttribute("data-theme", t);
  if (document.body) document.body.setAttribute("data-theme", t);

  const isDark = t === "dark";
  document.querySelectorAll(".theme-toggle-btn").forEach(btn => {
    btn.innerHTML = `<span class="theme-toggle-icon">${isDark ? SVG_ICONS.sun : SVG_ICONS.moon}</span> <span class="theme-toggle-label">${isDark ? 'Light' : 'Dark'}</span>`;
    btn.setAttribute("title", `Switch to ${isDark ? 'Light' : 'Dark'} Mode`);
    btn.setAttribute("aria-label", `Switch to ${isDark ? 'Light' : 'Dark'} Mode`);
  });
}

function toggleAppTheme() {
  const current = getAppTheme();
  setAppTheme(current === "dark" ? "light" : "dark");
}

// Immediately apply theme before rendering to avoid flashes
(() => {
  const initial = getAppTheme();
  document.documentElement.setAttribute("data-theme", initial);
  document.addEventListener("DOMContentLoaded", () => {
    setAppTheme(getAppTheme());
  });
})();

/* -----------------------------------------------------------------------------
   Student Selection & Topbar State
   ----------------------------------------------------------------------------- */
function selectedStudent() { return localStorage.getItem("nextgo_selected_student"); }
function setStudent(id) { if (id) localStorage.setItem("nextgo_selected_student", String(id)); }
function getCachedStudents() { try { const raw = localStorage.getItem("nextgo_cached_students"); return raw ? JSON.parse(raw) : []; } catch { return []; } }
function setCachedStudents(students) { try { if (Array.isArray(students) && students.length) { localStorage.setItem("nextgo_cached_students", JSON.stringify(students)); } } catch {} }
function switchStudent(id) { if (!id) return; setStudent(id); location.reload(); }
function escapeHtml(value = "") { const d = document.createElement("div"); d.textContent = value; return d.innerHTML; }

/* -----------------------------------------------------------------------------
   Notification Engine & Popover State (Top-Right, Left of "YOUR CHILD")
   ----------------------------------------------------------------------------- */
function getLocalNotifications() {
  try {
    const raw = localStorage.getItem("nextgo_local_notifications");
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function saveLocalNotifications(list) {
  try {
    localStorage.setItem("nextgo_local_notifications", JSON.stringify(list));
  } catch {}
}

function playNotificationSound() {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    const ctx = new AudioContext();
    const now = ctx.currentTime;

    // Dual-tone harmonic chime
    const osc1 = ctx.createOscillator();
    const gain1 = ctx.createGain();
    osc1.type = "sine";
    osc1.frequency.setValueAtTime(659.25, now); // E5
    gain1.gain.setValueAtTime(0, now);
    gain1.gain.linearRampToValueAtTime(0.18, now + 0.02);
    gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.22);
    osc1.connect(gain1);
    gain1.connect(ctx.destination);
    osc1.start(now);
    osc1.stop(now + 0.25);

    const osc2 = ctx.createOscillator();
    const gain2 = ctx.createGain();
    osc2.type = "sine";
    osc2.frequency.setValueAtTime(880, now + 0.12); // A5
    gain2.gain.setValueAtTime(0, now + 0.12);
    gain2.gain.linearRampToValueAtTime(0.22, now + 0.14);
    gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.45);
    osc2.connect(gain2);
    gain2.connect(ctx.destination);
    osc2.start(now + 0.12);
    osc2.stop(now + 0.48);
  } catch (e) {}
}

const _recentNotifications = new Map();

function normalizeNotifTime(ts) {
  if (!ts) return 0;
  const d = new Date(ts).getTime();
  return isNaN(d) ? 0 : d;
}

async function addSystemNotification(title, message, type = "info", studentId = null) {
  const activeSid = String(studentId || selectedStudent() || "1");
  const normTitle = (title || "").trim();
  const normMessage = (message || "").trim();
  const notifDedupeKey = `${activeSid}::${normTitle}::${normMessage}`;
  const now = Date.now();
  const lastFired = _recentNotifications.get(notifDedupeKey) || 0;

  // Prevent duplicate triggers within 20 seconds (e.g. multi-tab simulation ticks)
  if (now - lastFired < 20000) {
    return;
  }
  _recentNotifications.set(notifDedupeKey, now);

  // Play pleasant notification sound & immediate toast for fluid user feedback
  playNotificationSound();
  showGlobalToast(title, message);

  // Also notify backend if parentId exists
  const pid = localStorage.getItem("nextgo_parent_id");
  let backendSaved = false;
  if (typeof Api !== "undefined" && Api.sendNotification) {
    try {
      const res = await Api.sendNotification(pid, { title, message, type, studentId: activeSid });
      if (res && (res.id != null || res.notificationId != null)) {
        backendSaved = true;
      }
    } catch (err) {
      console.warn("Could not save notification to backend:", err);
    }
  }

  // Only store in localStorage if the backend failed to persist it (offline fallback)
  if (!backendSaved) {
    const newNotif = {
      id: "loc_" + Date.now() + "_" + Math.random().toString(36).substr(2, 4),
      studentId: activeSid,
      title: title,
      message: message,
      type: type,
      createdAt: new Date().toISOString(),
      isRead: false,
      read: false,
      readStatus: false
    };
    const list = getLocalNotifications();
    list.unshift(newNotif);
    saveLocalNotifications(list.slice(0, 30));
  } else {
    // If backend saved successfully, clean up any old local notices with this title & message
    const list = getLocalNotifications();
    const filtered = list.filter(n => !(n.title === title && n.message === message));
    saveLocalNotifications(filtered);
  }

  // Refresh badges and popovers immediately
  await refreshNotificationsUI();
}

let _globalToastTimer = null;

function dismissGlobalToast(e) {
  if (e) {
    e.stopPropagation();
    e.preventDefault();
  }
  const toast = document.getElementById("nextgoGlobalToast");
  if (toast) {
    toast.classList.remove("active");
    if (_globalToastTimer) {
      clearTimeout(_globalToastTimer);
      _globalToastTimer = null;
    }
  }
}

function showGlobalToast(title, message) {
  let toast = document.getElementById("nextgoGlobalToast");
  if (!toast) {
    toast = document.createElement("div");
    toast.id = "nextgoGlobalToast";
    toast.className = "nextgo-global-toast";
    document.body.appendChild(toast);
  }
  if (_globalToastTimer) {
    clearTimeout(_globalToastTimer);
  }
  toast.innerHTML = `
    <div class="toast-bell">${SVG_ICONS.bell}</div>
    <div class="toast-content">
      <strong>${escapeHtml(title)}</strong>
      <p>${escapeHtml(message)}</p>
    </div>
    <button class="toast-cancel-btn" onclick="dismissGlobalToast(event)" title="Cancel / Close notification" aria-label="Cancel notification">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
    </button>
  `;
  toast.classList.add("active");
  _globalToastTimer = setTimeout(() => {
    toast?.classList.remove("active");
    _globalToastTimer = null;
  }, 5000);
}

async function fetchCombinedNotifications(filterByAssigned = true) {
  let apiNotices = [];
  try {
    if (typeof Api !== "undefined" && Api.getNotifications) {
      apiNotices = apiList(await Api.getNotifications());
    }
  } catch {}

  const localNotices = getLocalNotifications();
  const activeSid = selectedStudent();

  // If we have API notifications, clean up any local fallback notices that already exist in API
  let cleanedLocal = localNotices;
  if (apiNotices.length > 0) {
    cleanedLocal = localNotices.filter(loc => {
      const locTime = normalizeNotifTime(loc.createdAt || loc.time);
      const isAlreadyInApi = apiNotices.some(apiN => {
        if ((loc.title || "").trim().toLowerCase() === (apiN.title || "").trim().toLowerCase() &&
            (loc.message || "").trim().toLowerCase() === (apiN.message || "").trim().toLowerCase()) {
          const apiTime = normalizeNotifTime(apiN.createdAt || apiN.time);
          if (!locTime || !apiTime || Math.abs(locTime - apiTime) < 15 * 60 * 1000) {
            return true;
          }
        }
        return false;
      });
      return !isAlreadyInApi;
    });

    if (cleanedLocal.length !== localNotices.length) {
      saveLocalNotifications(cleanedLocal);
    }
  }

  // Prioritize API notifications (which have real numeric IDs) over local fallbacks
  const merged = [...apiNotices, ...cleanedLocal];
  const deduped = [];

  for (const raw of merged) {
    const isRead = Boolean(raw.readStatus === true || raw.read === true || raw.isRead === true);
    const n = {
      ...raw,
      read: isRead,
      isRead: isRead,
      readStatus: isRead
    };

    // Filter to only show notifications assigned to this student / parent
    if (filterByAssigned && activeSid) {
      const nSid = n.studentId != null ? String(n.studentId) : null;
      if (nSid && nSid !== String(activeSid)) {
        continue; // Different student's notification
      }
    }

    const nTime = normalizeNotifTime(n.createdAt || n.time);
    const nTitle = (n.title || "").trim().toLowerCase();
    const nMsg = (n.message || "").trim().toLowerCase();

    // Check if duplicate of an already kept notification
    const isDuplicate = deduped.some(existing => {
      // Direct ID match
      if (n.id != null && existing.id != null && String(n.id) === String(existing.id)) {
        return true;
      }
      // Same title and message
      const exTitle = (existing.title || "").trim().toLowerCase();
      const exMsg = (existing.message || "").trim().toLowerCase();
      if (nTitle === exTitle && nMsg === exMsg) {
        const exTime = normalizeNotifTime(existing.createdAt || existing.time);
        // If created within 10 minutes of each other or either time is missing/invalid: duplicate!
        if (!nTime || !exTime || Math.abs(nTime - exTime) < 10 * 60 * 1000) {
          return true;
        }
      }
      return false;
    });

    if (!isDuplicate) {
      deduped.push(n);
    }
  }
  return deduped;
}

async function markAllNotificationsAsRead(e) {
  if (e) {
    e.stopPropagation();
    e.preventDefault();
  }

  // 1. Mark in backend
  try {
    if (typeof Api !== "undefined" && Api.markAllNotificationsRead) {
      await Api.markAllNotificationsRead();
    }
  } catch (err) {
    console.warn("Could not mark all read in backend:", err);
  }

  // 2. Mark in local storage
  const list = getLocalNotifications();
  list.forEach(n => { n.read = true; n.isRead = true; n.readStatus = true; });
  saveLocalNotifications(list);

  // 3. Update popover & badge immediately - everything disappears from popup!
  const badge = document.getElementById("topbarNotifBadge");
  if (badge) {
    badge.textContent = "0";
    badge.style.display = "none";
  }
  const countEl = document.getElementById("notifPopoverCount");
  if (countEl) {
    countEl.textContent = "0 unread";
  }
  const listEl = document.getElementById("notifPopoverList");
  if (listEl) {
    listEl.innerHTML = '<div class="notif-popover-empty">All caught up! No unread notifications</div>';
  }

  // 4. If currently on notifications, refresh that view too
  if (typeof window.refreshNotificationsPage === "function") {
    window.refreshNotificationsPage();
  } else {
    document.querySelectorAll(".notice.unread").forEach(n => n.classList.remove("unread"));
    document.querySelectorAll(".notice button[data-notice]").forEach(b => b.remove());
    document.querySelectorAll(".unread-dot").forEach(d => d.remove());
  }
}

async function refreshNotificationsUI() {
  const notices = await fetchCombinedNotifications(true);
  const unreadNotices = notices.filter(n => !(n.read || n.isRead || n.readStatus));
  const unreadCount = unreadNotices.length;

  const badge = document.getElementById("topbarNotifBadge");
  if (badge) {
    if (unreadCount > 0) {
      badge.textContent = unreadCount > 9 ? "9+" : String(unreadCount);
      badge.style.display = "inline-flex";
    } else {
      badge.style.display = "none";
    }
  }

  const countEl = document.getElementById("notifPopoverCount");
  if (countEl) {
    countEl.textContent = `${unreadCount} unread`;
  }

  const listEl = document.getElementById("notifPopoverList");
  if (listEl) {
    if (!unreadNotices.length) {
      listEl.innerHTML = '<div class="notif-popover-empty">All caught up! No unread notifications</div>';
    } else {
      listEl.innerHTML = unreadNotices.slice(0, 5).map(n => {
        const title = escapeHtml(field(n, "title", "subject", "Notification"));
        const msg = escapeHtml(field(n, "message", "body", "content", ""));
        const timeStr = formatNotifTime(field(n, "createdAt", "timestamp", "time"));
        return `
          <div class="notif-popover-item unread" onclick="openNotificationDetail('${escapeHtml(String(n.id || ''))}')">
            <div class="notif-popover-icon">${SVG_ICONS.bell}</div>
            <div class="notif-popover-text">
              <div class="notif-item-title">${title} <span class="unread-dot"></span></div>
              <div class="notif-item-msg">${msg}</div>
              <div class="notif-item-time">${timeStr}</div>
            </div>
            <button class="notif-popover-cancel" onclick="cancelPopoverNotification(event, '${escapeHtml(String(n.id || ''))}')" title="Dismiss notification" aria-label="Dismiss notification">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            </button>
          </div>
        `;
      }).join("");
    }
  }
}

async function cancelPopoverNotification(e, id) {
  if (e) {
    e.stopPropagation();
    e.preventDefault();
  }
  if (!id) return;

  // Mark read in local storage
  const list = getLocalNotifications();
  const item = list.find(n => String(n.id) === String(id));
  if (item) {
    item.read = true;
    item.isRead = true;
    item.readStatus = true;
    saveLocalNotifications(list);
  }

  // Mark read in backend if numeric ID
  if (typeof Api !== "undefined" && Api.markNotificationRead && !String(id).startsWith("loc_") && !isNaN(Number(id))) {
    try { await Api.markNotificationRead(id); } catch {}
  }

  // Immediately refresh UI badges & list
  refreshNotificationsUI();
}

function formatNotifTime(ts) {
  if (!ts) return "Just now";
  try {
    const d = new Date(ts);
    if (isNaN(d.getTime())) return String(ts);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } catch {
    return "Recently";
  }
}

async function openNotificationDetail(id) {
  if (id) {
    // Mark as read in local storage
    const list = getLocalNotifications();
    const item = list.find(n => String(n.id) === String(id));
    if (item) {
      item.read = true;
      item.isRead = true;
      item.readStatus = true;
      saveLocalNotifications(list);
    }
    // Mark as read in backend if it is a numeric ID
    if (typeof Api !== "undefined" && Api.markNotificationRead && !String(id).startsWith("loc_") && !isNaN(Number(id))) {
      try { await Api.markNotificationRead(id); } catch {}
    }
  }
  location.href = "notifications";
}

function toggleNotifPopover(e) {
  if (e) {
    e.stopPropagation();
    e.preventDefault();
  }
  const popover = document.getElementById("topbarNotifPopover");
  if (!popover) return;
  const isVisible = popover.classList.contains("active");
  if (isVisible) {
    popover.classList.remove("active");
  } else {
    popover.classList.add("active");
    refreshNotificationsUI();
  }
}

// Close notification popover when clicking outside
document.addEventListener("click", (e) => {
  const wrap = document.getElementById("topbarNotifWrap");
  const popover = document.getElementById("topbarNotifPopover");
  if (popover && popover.classList.contains("active") && wrap && !wrap.contains(e.target)) {
    popover.classList.remove("active");
  }
});

/* -----------------------------------------------------------------------------
   Topbar Renderers (Order: 1. Bell Icon -> 2. YOUR CHILD -> 3. Theme Toggle)
   ----------------------------------------------------------------------------- */
function renderNotificationBellHtml() {
  return `
    <div class="topbar-notif-wrap" id="topbarNotifWrap">
      <button class="topbar-notif-btn" id="topbarNotifBtn" type="button" onclick="toggleNotifPopover(event)" aria-label="Notifications" title="Notifications">
        ${SVG_ICONS.bell}
        <span class="notif-badge" id="topbarNotifBadge" style="display:none">0</span>
      </button>
      <div class="topbar-notif-popover" id="topbarNotifPopover">
        <div class="notif-popover-header">
          <div class="notif-popover-header-left">
            <span class="notif-popover-title">Notifications</span>
            <span class="notif-popover-count" id="notifPopoverCount">0 unread</span>
          </div>
          <button class="notif-popover-mark-btn" id="popoverMarkAllRead" type="button" onclick="markAllNotificationsAsRead(event)">Mark as read</button>
        </div>
        <div class="notif-popover-list" id="notifPopoverList">
          <div class="notif-popover-loading">Loading updates...</div>
        </div>
        <div class="notif-popover-footer">
          <a href="notifications" class="notif-view-all-link">View all notifications &rarr;</a>
        </div>
      </div>
    </div>
  `;
}

function renderThemeToggleHtml() {
  const isDark = getAppTheme() === "dark";
  return `
    <button class="theme-toggle-btn" id="themeToggleBtn" type="button" onclick="toggleAppTheme()" title="Switch to ${isDark ? 'Light' : 'Dark'} Mode" aria-label="Toggle theme">
      <span class="theme-toggle-icon">${isDark ? SVG_ICONS.sun : SVG_ICONS.moon}</span>
      <span class="theme-toggle-label">${isDark ? 'Light' : 'Dark'}</span>
    </button>
  `;
}

function renderChildSelectorHtml(students = null, activeId = null) {
  const list = (students && students.length) ? students : getCachedStudents();
  if (!list || !list.length) return '<div class="topbar-child-selector" id="topbarChildSelector"></div>';
  const currentId = String(activeId || selectedStudent() || list[0]?.id || list[0]?.studentId || "");
  if (list.length === 1) {
    const s = list[0], name = personName(s);
    return `<div class="topbar-child-selector" id="topbarChildSelector"><span class="child-selector-label">YOUR CHILD</span><div class="child-box single">${escapeHtml(name)}</div></div>`;
  }
  const options = list.map(s => {
    const id = String(s.id || s.studentId), name = personName(s), sel = (id === currentId) ? "selected" : "";
    return `<option value="${escapeHtml(id)}" ${sel}>${escapeHtml(name)}</option>`;
  }).join("");
  return `<div class="topbar-child-selector" id="topbarChildSelector"><span class="child-selector-label">YOUR CHILD</span><div class="child-select-wrap"><select class="child-select" id="topbarChildSelect" onchange="switchStudent(this.value)" aria-label="Select Child">${options}</select><span class="child-select-arrow" aria-hidden="true"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg></span></div></div>`;
}

function renderTopControls(students = null, activeId = null) {
  return `
    <div class="topbar-right-controls" id="topbarRightControls">
      ${renderNotificationBellHtml()}
      ${renderChildSelectorHtml(students, activeId)}
      ${renderThemeToggleHtml()}
    </div>
  `;
}

async function syncTopbarChildSelector(students = null) {
  try {
    let list = (students && students.length) ? students : null;
    if (!list && typeof Api !== "undefined" && Api.getStudents && localStorage.getItem("nextgo_token")) {
      list = apiList(await Api.getStudents());
    }
    if (list && list.length) {
      setCachedStudents(list);
      const studentExists = list.some(s => String(s.id || s.studentId) === String(selectedStudent()));
      if (!studentExists) {
        setStudent(list[0].id || list[0].studentId);
      }
      const el = document.getElementById("topbarRightControls");
      if (el) {
        el.outerHTML = renderTopControls(list, selectedStudent());
      } else {
        const selEl = document.getElementById("topbarChildSelector");
        if (selEl) {
          const parentWrap = selEl.closest(".topbar-right-controls");
          if (parentWrap) {
            parentWrap.outerHTML = renderTopControls(list, selectedStudent());
          } else {
            selEl.outerHTML = renderTopControls(list, selectedStudent());
          }
        }
      }
    }
    refreshNotificationsUI();
    ensureMobileNavToggle();
  } catch (e) {}
}

function renderMobileNavToggle() {
  return `
    <button class="mobile-nav-toggle" id="mobileNavToggle" type="button" onclick="toggleMobileNav(event)" aria-label="Open Navigation Menu" title="Menu">
      ${SVG_ICONS.menu}
    </button>
  `;
}

function renderTopbarLeft(title, subtitle = "School Student Transport") {
  return `
    <div class="topbar-left-group">
      ${renderMobileNavToggle()}
      <div class="topbar-title-text">
        <h1>${escapeHtml(title)}</h1>
        <p>${escapeHtml(subtitle)}</p>
      </div>
    </div>
  `;
}

function toggleMobileNav(e) {
  if (e) {
    e.stopPropagation();
    e.preventDefault();
  }
  const sidebar = document.querySelector(".sidebar");
  if (sidebar?.classList.contains("open")) {
    closeMobileNav();
  } else {
    openMobileNav();
  }
}

function openMobileNav() {
  const sidebar = document.querySelector(".sidebar");
  let backdrop = document.getElementById("sidebarBackdrop");
  if (!backdrop) {
    backdrop = document.createElement("div");
    backdrop.id = "sidebarBackdrop";
    backdrop.className = "sidebar-backdrop";
    backdrop.onclick = closeMobileNav;
    document.body.appendChild(backdrop);
  }
  sidebar?.classList.add("open");
  backdrop?.classList.add("open");
  document.body.classList.add("sidebar-open");
}

function closeMobileNav(e) {
  if (e && e.stopPropagation) e.stopPropagation();
  const sidebar = document.querySelector(".sidebar");
  const backdrop = document.getElementById("sidebarBackdrop");
  sidebar?.classList.remove("open");
  backdrop?.classList.remove("open");
  document.body.classList.remove("sidebar-open");
}

// Close drawer on Escape key
document.addEventListener("keydown", e => {
  if (e.key === "Escape") closeMobileNav();
});

// Auto-inject mobile-nav-toggle to any dynamic topbar missing it
function ensureMobileNavToggle() {
  document.querySelectorAll(".topbar").forEach(topbar => {
    if (!topbar.querySelector(".mobile-nav-toggle")) {
      const firstChild = topbar.firstElementChild;
      if (firstChild && !firstChild.classList.contains("topbar-right-controls")) {
        const toggleBtnHtml = renderMobileNavToggle();
        if (firstChild.classList.contains("topbar-left-group")) {
          firstChild.insertAdjacentHTML("afterbegin", toggleBtnHtml);
        } else {
          const wrapper = document.createElement("div");
          wrapper.className = "topbar-left-group";
          topbar.insertBefore(wrapper, firstChild);
          wrapper.innerHTML = toggleBtnHtml;
          firstChild.classList.add("topbar-title-text");
          wrapper.appendChild(firstChild);
        }
      }
    }
  });
}

function pageShell(title, subtitle, content) {
  const page = document.body.dataset.page;
  const links = Object.entries(Pages).map(([key, [label, href, icon]]) => `<a class="nav-link ${key === page ? "active" : ""}" href="${href}" onclick="closeMobileNav()"><span class="nav-icon">${icon}</span><span>${label}</span></a>`).join("");
  const mobile = Object.entries(Pages).slice(0, 5).map(([key, [, href, icon]]) => `<a class="${key === page ? "active" : ""}" href="${href}" aria-label="${key}">${icon}</a>`).join("");

  document.getElementById("app").innerHTML = `
    <div class="app-shell">
      <div class="sidebar-backdrop" id="sidebarBackdrop" onclick="closeMobileNav(event)"></div>
      <aside class="sidebar">
        <div class="sidebar-header">
          <a class="brand" href="dashboard"><span class="brand-icon">${SVG_ICONS.brand}</span> NEXTGO</a>
          <button class="sidebar-close-btn" type="button" onclick="closeMobileNav(event)" aria-label="Close menu" title="Close">
            ${SVG_ICONS.close}
          </button>
        </div>
        <nav class="nav-list" aria-label="Main navigation">${links}</nav>
        <div class="sidebar-footer">
          <button class="nav-link logout" onclick="Api.logout()"><span class="nav-icon">${SVG_ICONS.logout}</span><span>Logout</span></button>
        </div>
      </aside>
      <main class="app-main">
        <header class="topbar">
          ${renderTopbarLeft(title, subtitle)}
          ${renderTopControls()}
        </header>
        ${content}
      </main>
      <nav class="mobile-nav" aria-label="Mobile navigation">${mobile}</nav>
    </div>
  `;
  syncTopbarChildSelector();
  refreshNotificationsUI();
  ensureMobileNavToggle();
}

function errorView(message) { return `<section class="glass error-state"><strong>We couldn't load this information.</strong><p>${escapeHtml(message)}</p></section>`; }
function skeletons(count = 3) { return `<div class="student-list">${Array.from({ length: count }, () => '<div class="skeleton"></div>').join("")}</div>`; }
function apiList(result) { return Array.isArray(result) ? result : (result?.content || result?.items || result?.students || result?.notifications || result?.data || []); }
function transportBus(t) { return t?.bus || t?.assignedBus || t?.vehicle || {}; }
function field(obj, ...keys) { for (const key of keys) { if (obj?.[key] !== undefined && obj?.[key] !== null) return obj[key]; } const fallback = keys.find(key => typeof key === "string" && (key.includes(" ") || /^[A-Z]+$/.test(key) || key === "S")); return fallback ?? "—"; }
function personName(person = {}) { return person.name || person.fullName || person.studentName || [person.firstName, person.lastName].filter(Boolean).join(" ") || "—"; }

document.addEventListener("click", event => {
  const target = event.target.closest("[data-student]");
  if (target) { setStudent(target.dataset.student); }
});

// Background Bus Simulation Lifecycle on Non-Tracking Pages
document.addEventListener("DOMContentLoaded", () => {
  const page = document.body?.dataset?.page;
  if (page && page !== "tracking" && typeof NextGoSimulation !== "undefined") {
    NextGoSimulation.init(selectedStudent());
    NextGoSimulation.startBackgroundLoop();
  }
});

