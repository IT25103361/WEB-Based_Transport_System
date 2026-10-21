/**
 * tracking.js — Live delivery tracking logic
 */

let currentDelivery = null;
let refreshInterval = null;

const STATUSES = ['PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED'];
const STATUS_ICONS = {
  PENDING: '⏳', ASSIGNED: '👤', PICKED_UP: '📦', IN_TRANSIT: '🛵', DELIVERED: '✅', FAILED: '❌'
};

// ── INIT ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  const params = new URLSearchParams(window.location.search);
  const deliveryId = params.get('deliveryId');
  const orderId = params.get('orderId');

  if (deliveryId) {
    document.getElementById('delivery-id-input').value = deliveryId;
    await trackById(deliveryId);
  } else if (orderId) {
    document.getElementById('order-id-input').value = orderId;
    await trackByOrderId(orderId);
  }

  await loadActiveDeliveries();
  setupEventListeners();
});

function setupEventListeners() {
  document.getElementById('btn-track').addEventListener('click', async () => {
    const dId = document.getElementById('delivery-id-input').value.trim();
    const oId = document.getElementById('order-id-input').value.trim();
    if (dId) {
      await trackById(dId);
    } else if (oId) {
      await trackByOrderId(oId);
    } else {
      toast('Please enter a Delivery ID or Order ID', 'error');
    }
  });

  document.getElementById('btn-refresh').addEventListener('click', async () => {
    if (currentDelivery) await trackById(currentDelivery.id);
    await loadActiveDeliveries();
  });

  // Allow Enter key
  ['delivery-id-input', 'order-id-input'].forEach(id => {
    document.getElementById(id)?.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') document.getElementById('btn-track').click();
    });
  });
}

// ── TRACK ─────────────────────────────────────────────────
async function trackById(id) {
  try {
    const delivery = await api.get(`/api/deliveries/${id}`);
    showDelivery(delivery);
  } catch {
    document.getElementById('tracking-content').style.display = 'none';
    document.getElementById('tracking-empty').style.display = 'block';
    toast('Delivery not found', 'error');
  }
}

async function trackByOrderId(orderId) {
  try {
    const delivery = await api.get(`/api/deliveries/order/${orderId}`);
    showDelivery(delivery);
  } catch {
    document.getElementById('tracking-content').style.display = 'none';
    document.getElementById('tracking-empty').style.display = 'block';
    toast('No delivery found for this order', 'error');
  }
}

function showDelivery(delivery) {
  currentDelivery = delivery;
  document.getElementById('tracking-content').style.display = 'block';
  document.getElementById('tracking-empty').style.display = 'none';

  document.getElementById('track-title').textContent = `Delivery #${delivery.id}`;
  document.getElementById('track-status').innerHTML = statusBadge(delivery.status);
  document.getElementById('track-badge').innerHTML = `
    <span style="font-size:2.5rem">${STATUS_ICONS[delivery.status] || '📦'}</span>`;

  // Timeline
  document.getElementById('track-timeline').innerHTML = renderTimeline(delivery.status);

  // Location
  const loc = delivery.currentLocation || 'Location not available';
  document.getElementById('track-location').textContent = `📍 ${loc}`;
  if (delivery.latitude && delivery.longitude) {
    document.getElementById('track-coords').textContent =
      `Lat: ${delivery.latitude.toFixed(5)}, Lng: ${delivery.longitude.toFixed(5)}`;
  } else {
    document.getElementById('track-coords').textContent = '';
  }

  // Info blocks
  document.getElementById('track-order-id').textContent = `#${delivery.order?.id || '—'}`;
  document.getElementById('track-partner').textContent = delivery.deliveryPartner?.fullName || 'Not assigned';
  document.getElementById('track-address').textContent = delivery.deliveryAddress || '—';
  document.getElementById('track-assigned').textContent = formatDateTime(delivery.assignedAt);
  document.getElementById('track-picked').textContent = formatDateTime(delivery.pickedUpAt);
  document.getElementById('track-delivered').textContent = formatDateTime(delivery.deliveredAt);

  // Auto refresh for active deliveries
  if (refreshInterval) clearInterval(refreshInterval);
  if (!['DELIVERED', 'FAILED'].includes(delivery.status)) {
    refreshInterval = setInterval(async () => {
      try {
        const updated = await api.get(`/api/deliveries/${delivery.id}`);
        showDelivery(updated);
      } catch {}
    }, 15000); // Refresh every 15 seconds
  }
}

function renderTimeline(status) {
  const currentIdx = STATUSES.indexOf(status);
  return STATUSES.map((step, i) => `
    <div class="status-step ${i < currentIdx ? 'done' : i === currentIdx ? 'active' : ''}">
      <div class="status-step-dot" title="${humanizeStatus(step)}">
        ${i <= currentIdx ? STATUS_ICONS[step] || '✓' : ''}
      </div>
      ${i < STATUSES.length - 1 ? `<div class="status-step-line"></div>` : ''}
    </div>`).join('');
}

// ── ACTIVE DELIVERIES LIST ────────────────────────────────
async function loadActiveDeliveries() {
  showLoading('active-deliveries-container');
  try {
    const all = await api.get('/api/deliveries');
    const active = all.filter(d => !['DELIVERED', 'FAILED'].includes(d.status));
    if (!active.length) {
      showEmptyState('active-deliveries-container', 'No active deliveries right now', '✅');
      return;
    }
    document.getElementById('active-deliveries-container').innerHTML = `
      <div class="grid-auto">
        ${active.map(d => `
          <div class="card" style="cursor:pointer" onclick="trackById(${d.id});document.getElementById('delivery-id-input').value=${d.id}">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:var(--sp-sm)">
              <div style="font-weight:600">Delivery #${d.id}</div>
              ${statusBadge(d.status)}
            </div>
            <div style="font-size:0.8rem;color:var(--clr-text-muted);margin-bottom:4px">📍 ${escapeHtml(d.deliveryAddress || '—')}</div>
            <div style="font-size:0.8rem;color:var(--clr-text-muted)">👤 ${escapeHtml(d.deliveryPartner?.fullName || 'Unassigned')}</div>
            ${d.currentLocation ? `<div style="font-size:0.78rem;color:var(--clr-primary);margin-top:4px">📌 ${escapeHtml(d.currentLocation)}</div>` : ''}
          </div>`).join('')}
      </div>`;
  } catch (err) {
    showEmptyState('active-deliveries-container', 'Failed to load active deliveries', '⚠️');
  }
}

function escapeHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
