/**
 * FoodGrocer — Shared Utilities
 * api.js — API helpers, toast, auth, sidebar, modal, utils
 */

/* ──────────────────────────────────────────────────
   API BASE & PATH RESOLUTION
────────────────────────────────────────────────── */
function getContextPath() {
  if (typeof window.APP_CONTEXT_PATH === 'string') {
    return window.APP_CONTEXT_PATH.replace(/\/$/, '');
  }
  return '';
}

function resolveApiPath(path) {
  const ctx = getContextPath();
  let p = path || '';
  if (p.startsWith('/api/food/')) {
    return ctx + p;
  }
  if (p.startsWith('/api/')) {
    return ctx + p.replace('/api/', '/api/food/');
  }
  if (p.startsWith('api/')) {
    return ctx + '/' + p.replace('api/', 'api/food/');
  }
  if (!p.startsWith('/')) {
    return ctx + '/api/food/' + p;
  }
  return ctx + p;
}

async function apiFetch(path, options = {}) {
  const defaults = {
    headers: { 'Content-Type': 'application/json' }
  };
  const config = { ...defaults, ...options };
  if (options.headers) {
    config.headers = { ...defaults.headers, ...options.headers };
  }
  
  const user = getCurrentUser();
  if (user) {
    config.headers['X-User-Id'] = user.userId || user.id;
    config.headers['X-User-Role'] = user.role;
  }
  try {
    const fullUrl = resolveApiPath(path);
    const res = await fetch(fullUrl, config);
    if (!res.ok) {
      const text = await res.text();
      try {
        const json = JSON.parse(text);
        throw new Error(json.message || text);
      } catch (parseErr) {
        if (parseErr.message !== text) {
          throw new Error(text || `HTTP ${res.status}`);
        } else {
          throw parseErr; // Re-throw the original JSON error
        }
      }
    }
    const ct = res.headers.get('content-type') || '';
    if (ct.includes('application/json')) {
      return await res.json();
    }
    return null;
  } catch (err) {
    console.error('[API Error]', path, err);
    throw err;
  }
}

const api = {
  get:    (path) => apiFetch(path),
  post:   (path, data) => apiFetch(path, { method: 'POST',   body: JSON.stringify(data) }),
  put:    (path, data) => apiFetch(path, { method: 'PUT',    body: JSON.stringify(data) }),
  delete: (path)       => apiFetch(path, { method: 'DELETE' }),
  patch:  (path, data) => apiFetch(path, { method: 'PATCH',  body: JSON.stringify(data) }),
};

/* ──────────────────────────────────────────────────
   TOAST
────────────────────────────────────────────────── */
function toast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }
  const icons = { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' };
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.innerHTML = `<span>${icons[type] || 'ℹ️'}</span> ${message}`;
  container.appendChild(el);
  setTimeout(() => el.remove(), 4000);
}

/* ──────────────────────────────────────────────────
   MODAL
────────────────────────────────────────────────── */
function openModal(id) {
  const m = document.getElementById(id);
  if (m) {
    m.classList.add('open');
    m.style.display = 'flex';
  }
}

function closeModal(id) {
  const m = document.getElementById(id);
  if (m) {
    m.classList.remove('open');
    m.style.display = 'none';
  }
}

// Close on overlay click
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('open');
  }
});

// Close on Escape
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    document.querySelectorAll('.modal-overlay.open').forEach(m => m.classList.remove('open'));
  }
});

/* ──────────────────────────────────────────────────
   SIDEBAR
────────────────────────────────────────────────── */
function initSidebar() {
  const currentPath = window.location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('.sidebar-nav-item a').forEach(a => {
    const href = a.getAttribute('href')?.split('/').pop();
    if (href === currentPath) {
      a.classList.add('active');
    }
  });
}

/* ──────────────────────────────────────────────────
   AUTH HELPERS
────────────────────────────────────────────────── */
function getCurrentUser() {
  if (window.SERVER_FOOD_USER && window.SERVER_FOOD_USER.id) {
    return window.SERVER_FOOD_USER;
  }
  try {
    const raw = sessionStorage.getItem('fg_user') || localStorage.getItem('fg_user');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function setCurrentUser(user) {
  if (user) {
    sessionStorage.setItem('fg_user', JSON.stringify(user));
  }
}

function clearCurrentUser() {
  sessionStorage.removeItem('fg_user');
  localStorage.removeItem('fg_user');
  window.SERVER_FOOD_USER = null;
}

function requireAuth(allowedRoles = []) {
  const user = getCurrentUser();
  const ctx = getContextPath();
  if (!user) {
    window.location.href = ctx + '/food/login';
    return null;
  }
  if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    window.location.href = ctx + '/food/customer';
    return null;
  }
  return user;
}

/* ──────────────────────────────────────────────────
   FORMATTING HELPERS
────────────────────────────────────────────────── */
function formatCurrency(amount) {
  if (amount == null) return '—';
  return `Rs. ${Number(amount).toFixed(2)}`;
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatDateTime(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

function capitalize(str) {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

function humanizeStatus(status) {
  if (!status) return '—';
  return status.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
}

/* ──────────────────────────────────────────────────
   STATUS BADGE
────────────────────────────────────────────────── */
const statusBadgeMap = {
  PENDING:    'badge-warning',
  ACCEPTED:   'badge-info',
  PREPARING:  'badge-info',
  ON_THE_WAY: 'badge-primary',
  DELIVERED:  'badge-success',
  CANCELLED:  'badge-danger',
  FAILED:     'badge-danger',
  ASSIGNED:   'badge-primary',
  PICKED_UP:  'badge-info',
  IN_TRANSIT: 'badge-primary',
  ACTIVE:     'badge-success',
  INACTIVE:   'badge-muted',
  FOOD:       'badge-primary',
  GROCERY:    'badge-success',
};

function statusBadge(status) {
  const cls = statusBadgeMap[status?.toUpperCase()] || 'badge-muted';
  return `<span class="badge ${cls}">${humanizeStatus(status)}</span>`;
}

/* ──────────────────────────────────────────────────
   CONFIRM DIALOG (custom)
────────────────────────────────────────────────── */
function confirmDialog(message) {
  return new Promise((resolve) => {
    const overlay = document.createElement('div');
    overlay.style.position = 'fixed';
    overlay.style.top = '0';
    overlay.style.left = '0';
    overlay.style.width = '100vw';
    overlay.style.height = '100vh';
    overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.75)';
    overlay.style.display = 'flex';
    overlay.style.alignItems = 'center';
    overlay.style.justifyContent = 'center';
    overlay.style.zIndex = '9999';
    overlay.style.backdropFilter = 'blur(4px)';

    const box = document.createElement('div');
    box.style.background = 'var(--bg-card, #1e293b)';
    box.style.border = '1px solid var(--clr-border, #334155)';
    box.style.padding = '30px';
    box.style.borderRadius = '16px';
    box.style.width = '400px';
    box.style.maxWidth = '90%';
    box.style.boxShadow = '0 20px 40px rgba(0,0,0,0.4)';
    box.style.textAlign = 'center';
    box.style.fontFamily = 'Inter, sans-serif';
    
    const isDanger = message.toLowerCase().includes('delete') || message.toLowerCase().includes('deactivate') || message.toLowerCase().includes('remove');
    
    box.innerHTML = `
      <div style="margin-bottom: 20px;">
        <i class="fa-solid fa-triangle-exclamation" style="font-size: 3.5rem; color: ${isDanger ? '#ef4444' : '#f59e0b'}; drop-shadow: 0 0 15px ${isDanger ? 'rgba(239, 68, 68, 0.4)' : 'rgba(245, 158, 11, 0.4)'}"></i>
      </div>
      <h3 style="margin: 0 0 12px 0; color: var(--clr-text, #f8fafc); font-weight: 700;">Are you sure?</h3>
      <p style="color: var(--clr-text-muted, #94a3b8); margin-bottom: 25px; line-height: 1.5; font-size: 0.95rem;">${message}</p>
      <div style="display: flex; justify-content: center; gap: 12px;">
        <button id="custom-confirm-cancel" class="btn btn-secondary" style="min-width: 100px;">Cancel</button>
        <button id="custom-confirm-ok" class="btn ${isDanger ? 'btn-danger' : 'btn-primary'}" style="min-width: 100px;">${isDanger ? 'Yes, proceed' : 'Confirm'}</button>
      </div>
    `;
    
    overlay.appendChild(box);
    document.body.appendChild(overlay);
    
    // Add subtle entrance animation
    box.animate([
      { transform: 'scale(0.9)', opacity: 0 },
      { transform: 'scale(1)', opacity: 1 }
    ], { duration: 200, easing: 'ease-out' });
    
    const cleanup = () => {
      box.animate([
        { transform: 'scale(1)', opacity: 1 },
        { transform: 'scale(0.9)', opacity: 0 }
      ], { duration: 150, easing: 'ease-in' }).onfinish = () => {
        if (document.body.contains(overlay)) {
          document.body.removeChild(overlay);
        }
      };
    };
    
    document.getElementById('custom-confirm-cancel').addEventListener('click', () => {
      cleanup();
      resolve(false);
    });
    
    document.getElementById('custom-confirm-ok').addEventListener('click', () => {
      cleanup();
      resolve(true);
    });
  });
}

function promptDialog(message, defaultValue = '') {
  return new Promise((resolve) => {
    const overlay = document.createElement('div');
    overlay.style.position = 'fixed';
    overlay.style.top = '0';
    overlay.style.left = '0';
    overlay.style.width = '100vw';
    overlay.style.height = '100vh';
    overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.75)';
    overlay.style.display = 'flex';
    overlay.style.alignItems = 'center';
    overlay.style.justifyContent = 'center';
    overlay.style.zIndex = '9999';
    overlay.style.backdropFilter = 'blur(4px)';

    const box = document.createElement('div');
    box.style.background = 'var(--bg-card, #1e293b)';
    box.style.border = '1px solid var(--clr-border, #334155)';
    box.style.padding = '30px';
    box.style.borderRadius = '16px';
    box.style.width = '400px';
    box.style.maxWidth = '90%';
    box.style.boxShadow = '0 20px 40px rgba(0,0,0,0.4)';
    box.style.textAlign = 'center';
    box.style.fontFamily = 'Inter, sans-serif';
    
    box.innerHTML = `
      <h3 style="margin: 0 0 12px 0; color: var(--clr-text, #f8fafc); font-weight: 600; font-size: 1.1rem;">${message}</h3>
      <input type="text" id="custom-prompt-input" value="${defaultValue}" class="form-control bg-dark text-light border-secondary" style="width: 100%; margin-bottom: 25px;" />
      <div style="display: flex; justify-content: center; gap: 12px;">
        <button id="custom-prompt-cancel" class="btn btn-secondary" style="min-width: 100px;">Cancel</button>
        <button id="custom-prompt-ok" class="btn btn-primary" style="min-width: 100px;">OK</button>
      </div>
    `;
    
    overlay.appendChild(box);
    document.body.appendChild(overlay);
    
    const input = document.getElementById('custom-prompt-input');
    input.focus();
    
    // Add subtle entrance animation
    box.animate([
      { transform: 'scale(0.9)', opacity: 0 },
      { transform: 'scale(1)', opacity: 1 }
    ], { duration: 200, easing: 'ease-out' });
    
    const cleanup = () => {
      box.animate([
        { transform: 'scale(1)', opacity: 1 },
        { transform: 'scale(0.9)', opacity: 0 }
      ], { duration: 150, easing: 'ease-in' }).onfinish = () => {
        if (document.body.contains(overlay)) {
          document.body.removeChild(overlay);
        }
      };
    };
    
    document.getElementById('custom-prompt-cancel').addEventListener('click', () => {
      cleanup();
      resolve(null);
    });
    
    document.getElementById('custom-prompt-ok').addEventListener('click', () => {
      cleanup();
      resolve(input.value);
    });
    
    input.addEventListener('keyup', (e) => {
      if (e.key === 'Enter') {
        cleanup();
        resolve(input.value);
      } else if (e.key === 'Escape') {
        cleanup();
        resolve(null);
      }
    });
  });
}

/* ──────────────────────────────────────────────────
   LOADING STATE
────────────────────────────────────────────────── */
function showLoading(containerId) {
  const el = document.getElementById(containerId);
  if (el) {
    el.innerHTML = `<div class="loading"><div class="spinner"></div> Loading...</div>`;
  }
}

function showEmptyState(containerId, message = 'No data found', icon = '📭') {
  const el = document.getElementById(containerId);
  if (el) {
    el.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">${icon}</div>
        <p>${message}</p>
      </div>`;
  }
}

/* ──────────────────────────────────────────────────
   DEBOUNCE
────────────────────────────────────────────────── */
function debounce(fn, delay = 300) {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), delay);
  };
}

/* ──────────────────────────────────────────────────
   SIDEBAR HTML BUILDER
────────────────────────────────────────────────── */
function buildSidebar(role) {
  const user = getCurrentUser();
  const ctx = getContextPath();
  const navItems = {
    ADMIN: [
      { section: 'Overview', items: [
        { icon: '📊', label: 'Dashboard', href: ctx + '/food/admin' },
      ]},
      { section: 'Grocery / Food', items: [
        { icon: '🏪', label: 'Shops', href: ctx + '/food/shops' },
        { icon: '📦', label: 'Products', href: ctx + '/food/products' },
        { icon: '📈', label: 'Stock', href: ctx + '/food/stock' },
      ]},
      { section: 'Delivery', items: [
        { icon: '🛒', label: 'Orders', href: ctx + '/food/orders' },
        { icon: '🚚', label: 'Deliveries', href: ctx + '/food/deliveries' },
        { icon: '🗺️', label: 'Live Tracking', href: ctx + '/food/tracking' },
      ]},
      { section: 'Management', items: [
        { icon: '🏬', label: 'Vendors', href: ctx + '/food/vendor_manager' },
        { icon: '👥', label: 'Users', href: ctx + '/food/admin#users' },
      ]},
    ],
    VENDOR_MANAGER: [
      { section: 'My Shop', items: [
        { icon: '🏪', label: 'My Shops', href: ctx + '/food/shops' },
        { icon: '📦', label: 'Products', href: ctx + '/food/products' },
        { icon: '📈', label: 'Stock', href: ctx + '/food/stock' },
        { icon: '🛒', label: 'Orders', href: ctx + '/food/orders' },
      ]},
    ],
    DELIVERY_PARTNER: [
      { section: 'Deliveries', items: [
        { icon: '🚚', label: 'My Deliveries', href: ctx + '/food/partner' },
        { icon: '🗺️', label: 'Track', href: ctx + '/food/tracking' },
      ]},
    ],
    CUSTOMER: [
      { section: 'Shop', items: [
        { icon: '🏠', label: 'Home', href: ctx + '/food/customer' },
        { icon: '🛒', label: 'My Orders', href: ctx + '/food/orders' },
        { icon: '🗺️', label: 'Track Order', href: ctx + '/food/tracking' },
      ]},
    ],
  };

  const sections = navItems[role] || navItems.CUSTOMER;
  let html = `
    <a href="${ctx}/food/customer" class="sidebar-logo">
      <div class="sidebar-logo-icon">🍔</div>
      <span class="sidebar-logo-text">FoodGrocer</span>
    </a>`;

  sections.forEach(section => {
    html += `<div class="sidebar-section">
      <div class="sidebar-section-title">${section.section}</div>
      <ul class="sidebar-nav">`;
    section.items.forEach(item => {
      html += `<li class="sidebar-nav-item">
        <a href="${item.href}">
          <span class="nav-icon">${item.icon}</span>
          ${item.label}
        </a>
      </li>`;
    });
    html += `</ul></div>`;
  });

  // User section at bottom
  if (user) {
    html += `
      <div style="margin-top:auto;padding:1rem;border-top:1px solid var(--clr-border);">
        <div style="display:flex;align-items:center;gap:0.5rem;margin-bottom:0.75rem;">
          <div class="avatar">${user.fullName?.charAt(0) || '?'}</div>
          <div>
            <div style="font-size:0.8rem;font-weight:600">${user.fullName}</div>
            <div style="font-size:0.7rem;color:var(--clr-text-muted)">${capitalize(user.role?.replace('_',' '))}</div>
          </div>
        </div>
        <button onclick="logout()" class="btn btn-secondary btn-sm" style="width:100%">🚪 Logout</button>
      </div>`;
  }

  return html;
}

function injectSidebar() {
  const user = getCurrentUser();
  const role = user?.role || 'CUSTOMER';
  
  // 1. If old sidebar exists (e.g. index.html), use old buildSidebar logic
  const sidebar = document.getElementById('sidebar');
  if (sidebar) {
    sidebar.innerHTML = buildSidebar(role);
    initSidebar();
    return;
  }
  
  // 2. Otherwise we are on the JSP pages using sidebar.jsp
  // Update Profile details
  if (user) {
    const nameEl = document.getElementById('sidebar-name');
    if (nameEl) nameEl.textContent = user.fullName;
    
    const roleEl = document.getElementById('sidebar-role');
    if (roleEl) roleEl.innerHTML = `<i class="fa-solid fa-user-shield me-1"></i>${role.replace('_', ' ')}`;
    
    const avatarEl = document.getElementById('sidebar-avatar');
    if (avatarEl) avatarEl.textContent = user.fullName.charAt(0).toUpperCase();
  }

  // Filter links by role
  document.querySelectorAll('[data-roles]').forEach(el => {
    const allowed = el.getAttribute('data-roles').split(',');
    if (!allowed.includes(role)) {
      el.remove();
    }
  });
}

async function logout() {
  clearCurrentUser();
  const ctx = getContextPath();
  try {
    await fetch(ctx + '/api/food/auth/logout', { method: 'POST' });
  } catch (e) {}
  window.location.href = ctx + '/food/login';
}

/* ──────────────────────────────────────────────────
   FILE UPLOAD
────────────────────────────────────────────────── */
async function uploadImageFile(inputEl, targetInputId) {
  if (!inputEl.files || inputEl.files.length === 0) return;
  const file = inputEl.files[0];
  const formData = new FormData();
  formData.append('file', file);

  const targetEl = document.getElementById(targetInputId);
  const originalValue = targetEl.value;
  targetEl.value = 'Uploading...';
  targetEl.disabled = true;

  try {
    const user = getCurrentUser();
    const headers = {};
    if (user) {
      headers['X-User-Id'] = user.userId || user.id;
      headers['X-User-Role'] = user.role;
    }
    
    const uploadUrl = resolveApiPath('/api/upload');
    const res = await fetch(uploadUrl, {
      method: 'POST',
      body: formData,
      headers: headers
    });
    
    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || 'Upload failed');
    }
    const data = await res.json();
    targetEl.value = data.url;
    toast('Image uploaded successfully', 'success');
  } catch (err) {
    targetEl.value = originalValue;
    toast('Failed to upload image: ' + err.message, 'error');
  } finally {
    targetEl.disabled = false;
    inputEl.value = ''; // reset file input
  }
}

/* ──────────────────────────────────────────────────
   INIT
────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  injectSidebar();
});
