/**
 * deliveries.js — Delivery management logic
 */

let allDeliveries = [];
let deliveryPartners = [];
let currentStatusFilter = 'ALL';

// ── INIT ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  await Promise.all([loadDeliveries(), loadPartners()]);
  setupEventListeners();
});

async function loadDeliveries() {
  showLoading('deliveries-table-container');
  try {
    const rawDeliveries = await api.get('/api/deliveries');
    const user = getCurrentUser();
    if (user && user.role === 'VENDOR_MANAGER') {
      allDeliveries = rawDeliveries.filter(d => d.order?.shop?.managerId === user.id);
    } else {
      allDeliveries = rawDeliveries;
    }
    
    updateStats(allDeliveries);
    renderTable(applyFilter(allDeliveries));
  } catch (err) {
    toast('Failed to load deliveries: ' + err.message, 'error');
    showEmptyState('deliveries-table-container', 'Failed to load deliveries', '⚠️');
  }
}

async function loadPartners() {
  try {
    const users = await api.get('/api/auth/users');
    deliveryPartners = users.filter(u => u.role === 'DELIVERY_PARTNER' && u.active);
    const sel = document.getElementById('assign-partner');
    deliveryPartners.forEach(p => {
      const opt = document.createElement('option');
      opt.value = p.id;
      opt.textContent = p.fullName + (p.phoneNumber ? ' — ' + p.phoneNumber : '');
      sel.appendChild(opt);
    });
  } catch {}
}

function setupEventListeners() {
  document.getElementById('btn-refresh').addEventListener('click', loadDeliveries);

  document.querySelectorAll('.filter-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      currentStatusFilter = chip.dataset.status;
      renderTable(applyFilter(allDeliveries));
    });
  });

  document.getElementById('search-input').addEventListener('input', debounce(() => {
    renderTable(applyFilter(allDeliveries));
  }, 300));
}

// ── FILTER ────────────────────────────────────────────────
function applyFilter(deliveries) {
  const query = document.getElementById('search-input')?.value.toLowerCase() || '';
  return deliveries.filter(d => {
    const matchStatus = currentStatusFilter === 'ALL' || d.status === currentStatusFilter;
    const matchQuery = !query
      || String(d.order?.id || d.id).includes(query)
      || (d.deliveryAddress || '').toLowerCase().includes(query)
      || (d.deliveryPartner?.fullName || '').toLowerCase().includes(query);
    return matchStatus && matchQuery;
  });
}

// ── STATS ─────────────────────────────────────────────────
function updateStats(deliveries) {
  document.getElementById('stat-total').textContent = deliveries.length;
  document.getElementById('stat-pending').textContent = deliveries.filter(d => d.status === 'PENDING').length;
  document.getElementById('stat-transit').textContent =
    deliveries.filter(d => ['IN_TRANSIT','PICKED_UP'].includes(d.status)).length;
  document.getElementById('stat-delivered').textContent = deliveries.filter(d => d.status === 'DELIVERED').length;
}

// ── RENDER TABLE ──────────────────────────────────────────
function renderTable(deliveries) {
  if (!deliveries.length) {
    showEmptyState('deliveries-table-container', 'No deliveries found', '🚚');
    return;
  }
  document.getElementById('deliveries-table-container').innerHTML = `
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Order</th>
          <th>Delivery Address</th>
          <th>Partner</th>
          <th>Status</th>
          <th>Current Location</th>
          <th>Assigned</th>
          <th>Delivered</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        ${deliveries.map(renderRow).join('')}
      </tbody>
    </table>`;
}

function renderRow(d) {
  const partner = d.deliveryPartner;
  const user = getCurrentUser();
  const isVM = user && user.role === 'VENDOR_MANAGER';
  return `
    <tr>
      <td style="color:var(--clr-text-faint);font-size:0.8rem">#${d.id}</td>
      <td>
        <div style="font-weight:600">Order #${d.order?.id || '—'}</div>
        <div style="font-size:0.72rem;color:var(--clr-text-muted)">${formatCurrency(d.order?.totalAmount)}</div>
      </td>
      <td style="font-size:0.8rem;max-width:180px">
        <div style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap" title="${escapeHtml(d.deliveryAddress)}">
          📍 ${escapeHtml(d.deliveryAddress || '—')}
        </div>
      </td>
      <td>
        ${partner
          ? `<div style="display:flex;align-items:center;gap:4px"><div class="avatar" style="width:24px;height:24px;font-size:0.65rem">${partner.fullName?.charAt(0)}</div><span style="font-size:0.85rem">${escapeHtml(partner.fullName)}</span></div>`
          : `<button class="btn btn-sm btn-primary" onclick="openAssignModal(${d.id})">Assign</button>`}
      </td>
      <td>${statusBadge(d.status)}</td>
      <td style="font-size:0.8rem;color:var(--clr-text-muted)">${escapeHtml(d.currentLocation || '—')}</td>
      <td style="font-size:0.78rem;color:var(--clr-text-muted)">${formatDateTime(d.assignedAt)}</td>
      <td style="font-size:0.78rem;color:var(--clr-text-muted)">${formatDateTime(d.deliveredAt)}</td>
      <td>
        <div class="table-actions">
          <button class="btn btn-sm btn-secondary" onclick="viewDelivery(${d.id})">👁</button>
          <button class="btn btn-sm btn-primary" onclick="openDeliveryStatusModal(${d.id},'${d.status}')">✏️</button>
          ${!partner && d.status === 'PENDING' ? `<button class="btn btn-sm btn-success" onclick="openAssignModal(${d.id})">👤 Assign</button>` : ''}
          <a href="${getContextPath()}/food/tracking?deliveryId=${d.id}" class="btn btn-sm btn-secondary">🗺️</a>
        </div>
      </td>
    </tr>`;
}

function escapeHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

// ── ASSIGN MODAL ──────────────────────────────────────────
function openAssignModal(deliveryId) {
  document.getElementById('assign-delivery-id').value = deliveryId;
  document.getElementById('assign-partner').value = '';
  openModal('assign-modal');
}

async function confirmAssign() {
  const deliveryId = document.getElementById('assign-delivery-id').value;
  const partnerId = document.getElementById('assign-partner').value;
  if (!partnerId) { toast('Please select a partner', 'error'); return; }
  try {
    await api.post(`/api/deliveries/${deliveryId}/assign`, { partnerId: parseInt(partnerId) });
    toast('Delivery partner assigned!', 'success');
    closeModal('assign-modal');
    await loadDeliveries();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  }
}

// ── STATUS MODAL ──────────────────────────────────────────
function openDeliveryStatusModal(deliveryId, currentStatus) {
  document.getElementById('delivery-update-id').value = deliveryId;
  document.getElementById('delivery-new-status').value = currentStatus;
  document.getElementById('delivery-location').value = '';
  openModal('delivery-status-modal');
}

async function confirmDeliveryStatus() {
  const id = document.getElementById('delivery-update-id').value;
  const status = document.getElementById('delivery-new-status').value;
  const location = document.getElementById('delivery-location').value.trim() || null;
  try {
    await api.put(`/api/deliveries/${id}/status`, { status, location });
    toast('Delivery status updated!', 'success');
    closeModal('delivery-status-modal');
    await loadDeliveries();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  }
}

// ── VIEW DETAIL ───────────────────────────────────────────
function viewDelivery(id) {
  const d = allDeliveries.find(x => x.id === id);
  if (!d) return;
  document.getElementById('delivery-view-title').textContent = `Delivery #${d.id}`;
  document.getElementById('delivery-view-body').innerHTML = `
    <div style="display:flex;flex-direction:column;gap:var(--sp-sm)">
      <div style="display:flex;justify-content:space-between;margin-bottom:var(--sp-sm)">
        <div>${statusBadge(d.status)}</div>
        <div style="font-size:0.8rem;color:var(--clr-text-muted)">Order #${d.order?.id || '—'}</div>
      </div>

      <!-- Status timeline -->
      <div class="status-timeline" style="margin-bottom:var(--sp-md)">
        ${renderTimeline(d.status)}
      </div>

      ${infoRowD('📍 Delivery Address', d.deliveryAddress)}
      ${infoRowD('📌 Current Location', d.currentLocation)}
      ${infoRowD('👤 Partner', d.deliveryPartner?.fullName)}
      ${infoRowD('⏰ Assigned At', formatDateTime(d.assignedAt))}
      ${infoRowD('📦 Picked Up', formatDateTime(d.pickedUpAt))}
      ${infoRowD('✅ Delivered', formatDateTime(d.deliveredAt))}
      ${infoRowD('💰 Order Total', formatCurrency(d.order?.totalAmount))}
      ${d.notes ? infoRowD('📝 Notes', d.notes) : ''}
    </div>
    <div style="margin-top:var(--sp-md);display:flex;gap:var(--sp-sm)">
      <button class="btn btn-primary btn-sm" onclick="closeModal('delivery-view-modal');openDeliveryStatusModal(${d.id},'${d.status}')">Update Status</button>
      ${!d.deliveryPartner ? `<button class="btn btn-success btn-sm" onclick="closeModal('delivery-view-modal');openAssignModal(${d.id})">Assign Partner</button>` : ''}
      <a href="${getContextPath()}/food/tracking?deliveryId=${d.id}" class="btn btn-secondary btn-sm">🗺️ Track</a>
    </div>`;
  openModal('delivery-view-modal');
}

function renderTimeline(status) {
  const steps = ['PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED'];
  const icons = { PENDING: '⏳', ASSIGNED: '👤', PICKED_UP: '📦', IN_TRANSIT: '🛵', DELIVERED: '✅' };
  const currentIdx = steps.indexOf(status);
  return steps.map((step, i) => `
    <div class="status-step ${i < currentIdx ? 'done' : i === currentIdx ? 'active' : ''}">
      <div class="status-step-dot" title="${humanizeStatus(step)}">${i <= currentIdx ? icons[step] : ''}</div>
      ${i < steps.length - 1 ? '<div class="status-step-line"></div>' : ''}
    </div>`).join('');
}

function infoRowD(label, value) {
  return `
    <div style="display:flex;justify-content:space-between;align-items:center;padding:6px 0;border-bottom:1px solid var(--clr-border)">
      <span style="font-size:0.8rem;color:var(--clr-text-muted)">${label}</span>
      <span style="font-size:0.875rem;font-weight:500">${escapeHtml(String(value || '—'))}</span>
    </div>`;
}
