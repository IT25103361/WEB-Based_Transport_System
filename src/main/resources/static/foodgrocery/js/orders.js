/**
 * orders.js — Order management logic
 */

let allOrders = [];
let deliveryPartners = [];
let currentStatusFilter = 'ALL';

// ── INIT ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  await Promise.all([loadOrders(), loadPartners()]);
  setupEventListeners();
});

async function loadOrders() {
  showLoading('orders-table-container');
  try {
    const rawOrders = await api.get('/api/orders/all');
    const user = getCurrentUser();
    if (user && user.role === 'VENDOR_MANAGER') {
      allOrders = rawOrders.filter(o => o.shop?.managerId === user.id);
    } else {
      allOrders = rawOrders;
    }
    updateStats(allOrders);
    renderTable(applyFilter(allOrders));
  } catch (err) {
    toast('Failed to load orders: ' + err.message, 'error');
    showEmptyState('orders-table-container', 'Failed to load orders', '⚠️');
  }
}

async function loadPartners() {
  try {
    // Get all delivery partners (users with DELIVERY_PARTNER role)
    const users = await api.get('/api/auth/users');
    deliveryPartners = users.filter(u => u.role === 'DELIVERY_PARTNER' && u.active);
    const sel = document.getElementById('partner-select');
    deliveryPartners.forEach(p => {
      const opt = document.createElement('option');
      opt.value = p.id;
      opt.textContent = p.fullName + ' — ' + (p.phoneNumber || '');
      sel.appendChild(opt);
    });
  } catch {}
}

function setupEventListeners() {
  const btnRefresh = document.getElementById('btn-refresh');
  if (btnRefresh) {
    btnRefresh.addEventListener('click', loadOrders);
  }

  document.querySelectorAll('.filter-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      currentStatusFilter = chip.dataset.filter; // Fixed from dataset.status to dataset.filter
      renderTable(applyFilter(allOrders));
    });
  });

  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    searchInput.addEventListener('input', debounce(() => {
      renderTable(applyFilter(allOrders));
    }, 300));
  }

  const newStatus = document.getElementById('new-status');
  if (newStatus) {
    newStatus.addEventListener('change', (e) => {
      const show = ['ACCEPTED', 'ON_THE_WAY'].includes(e.target.value);
      const partnerGroup = document.getElementById('partner-group');
      if (partnerGroup) partnerGroup.style.display = show ? 'block' : 'none';
    });
  }
}

// ── FILTER ────────────────────────────────────────────────
function applyFilter(orders) {
  const query = document.getElementById('search-input')?.value.toLowerCase() || '';
  return orders.filter(o => {
    const matchStatus = currentStatusFilter === 'ALL' || o.status === currentStatusFilter;
    const matchQuery = !query
      || (o.customer?.fullName || '').toLowerCase().includes(query)
      || (o.shop?.name || '').toLowerCase().includes(query)
      || String(o.id).includes(query);
    return matchStatus && matchQuery;
  });
}

// ── STATS ─────────────────────────────────────────────────
function updateStats(orders) {
  document.getElementById('stat-total').textContent = orders.length;
  document.getElementById('stat-pending').textContent = orders.filter(o => o.status === 'PENDING').length;
  document.getElementById('stat-delivery').textContent = orders.filter(o => o.status === 'ON_THE_WAY').length;
  document.getElementById('stat-completed').textContent = orders.filter(o => o.status === 'DELIVERED').length;
}

// ── RENDER TABLE ──────────────────────────────────────────
function renderTable(orders) {
  if (!orders.length) {
    showEmptyState('orders-table-container', 'No orders found', '🛒');
    return;
  }
  document.getElementById('orders-table-container').innerHTML = `
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Customer</th>
          <th>Vendor</th>
          <th>Items</th>
          <th>Total</th>
          <th>Status</th>
          <th>Delivery Partner</th>
          <th>Placed At</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        ${orders.map(renderRow).join('')}
      </tbody>
    </table>`;
}

function renderRow(o) {
  const partner = o.deliveryPartner;
  const user = getCurrentUser();
  const isVM = user && user.role === 'VENDOR_MANAGER';
  const partnerName = partner ? (partner.name || partner.user?.fullName || 'Unknown') : '';
  
  return `
    <tr>
      <td style="color:var(--clr-text-faint);font-size:0.8rem">#${o.id}</td>
      <td>
        <div style="font-weight:600;font-size:0.875rem">${escapeHtml(o.customer?.fullName || '—')}</div>
        <div style="font-size:0.72rem;color:var(--clr-text-muted)">${escapeHtml(o.customer?.phoneNumber || '')}</div>
      </td>
      <td style="font-size:0.875rem">${escapeHtml(o.shop?.name || '—')}</td>
      <td>
        <span class="badge badge-info">${o.items?.length || 0} item${o.items?.length !== 1 ? 's' : ''}</span>
      </td>
      <td style="font-weight:600;color:var(--clr-primary)">${formatCurrency(o.totalAmount)}</td>
      <td>${statusBadge(o.status)}</td>
      <td style="font-size:0.8rem">
        ${partner ? `<div style="display:flex;align-items:center;gap:4px"><div class="avatar" style="width:24px;height:24px;font-size:0.65rem">${partnerName.charAt(0)}</div>${escapeHtml(partnerName)}</div>` : '<span style="color:var(--clr-text-faint)">Not assigned</span>'}
      </td>
      <td style="font-size:0.78rem;color:var(--clr-text-muted)">${formatDateTime(o.placedAt)}</td>
      <td>
        <div class="table-actions">
          <button class="btn btn-sm btn-secondary" onclick="viewOrder(${o.id})">👁</button>
          ${!isVM ? `<button class="btn btn-sm btn-primary" onclick="openStatusModal(${o.id},'${o.status}')">✏️ Status</button>` : ''}
          ${(o.status === 'ACCEPTED' || o.status === 'PREPARING') ? `<button class="btn btn-sm btn-success" onclick="createDelivery(${o.id})" title="Create Delivery">🚚</button>` : ''}
        </div>
      </td>
    </tr>`;
}

function escapeHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

// ── VIEW ORDER DETAIL ─────────────────────────────────────
async function viewOrder(id) {
  const order = allOrders.find(o => o.id === id);
  if (!order) return;

  const itemsHtml = (order.items || []).map(item => `
    <tr>
      <td>${escapeHtml(item.product?.name || '—')}</td>
      <td>${item.quantity}</td>
      <td>${formatCurrency(item.unitPrice)}</td>
      <td style="font-weight:600">${formatCurrency(item.subtotal || item.unitPrice * item.quantity)}</td>
    </tr>`).join('');

  document.getElementById('modal-title').textContent = `Order #${order.id}`;
  document.getElementById('order-modal-body').innerHTML = `
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:var(--sp-md);margin-bottom:var(--sp-md)">
      <div>
        <div style="font-size:0.75rem;color:var(--clr-text-muted);margin-bottom:4px">CUSTOMER</div>
        <div style="font-weight:600">${escapeHtml(order.customer?.fullName || '—')}</div>
        <div style="font-size:0.8rem;color:var(--clr-text-muted)">${escapeHtml(order.customer?.phoneNumber || '')}</div>
      </div>
      <div>
        <div style="font-size:0.75rem;color:var(--clr-text-muted);margin-bottom:4px">VENDOR</div>
        <div style="font-weight:600">${escapeHtml(order.shop?.name || '—')}</div>
      </div>
      <div>
        <div style="font-size:0.75rem;color:var(--clr-text-muted);margin-bottom:4px">STATUS</div>
        ${statusBadge(order.status)}
      </div>
      <div>
        <div style="font-size:0.75rem;color:var(--clr-text-muted);margin-bottom:4px">PLACED AT</div>
        <div style="font-size:0.85rem">${formatDateTime(order.placedAt)}</div>
      </div>
    </div>
    <div style="margin-bottom:var(--sp-md)">
      <div style="font-size:0.75rem;color:var(--clr-text-muted);margin-bottom:4px">DELIVERY ADDRESS</div>
      <div style="font-size:0.875rem">📍 ${escapeHtml(order.deliveryAddress || '—')}</div>
    </div>
    ${order.specialInstructions ? `<div style="margin-bottom:var(--sp-md)"><div style="font-size:0.75rem;color:var(--clr-text-muted);margin-bottom:4px">SPECIAL INSTRUCTIONS</div><div style="font-size:0.875rem;color:var(--clr-text-muted)">${escapeHtml(order.specialInstructions)}</div></div>` : ''}
    <div class="table-wrapper" style="margin-bottom:var(--sp-md)">
      <table>
        <thead><tr><th>Item</th><th>Qty</th><th>Price</th><th>Subtotal</th></tr></thead>
        <tbody>${itemsHtml}</tbody>
      </table>
    </div>
    <div style="display:flex;justify-content:flex-end;gap:var(--sp-md);align-items:center">
      <span style="color:var(--clr-text-muted);font-size:0.875rem">Delivery Fee: ${formatCurrency(order.deliveryFee)}</span>
      <span style="font-family:'Outfit',sans-serif;font-size:1.3rem;font-weight:800;color:var(--clr-primary)">Total: ${formatCurrency(order.totalAmount)}</span>
    </div>`;
  openModal('order-modal');
}

// ── STATUS MODAL ──────────────────────────────────────────
function openStatusModal(orderId, currentStatus) {
  document.getElementById('status-order-id').value = orderId;
  document.getElementById('new-status').value = currentStatus;
  document.getElementById('partner-group').style.display = 'none';
  openModal('status-modal');
}

async function confirmStatusUpdate() {
  const orderId = document.getElementById('status-order-id').value;
  const newStatus = document.getElementById('new-status').value;
  const partnerId = document.getElementById('partner-select').value || null;

  try {
    await api.put(`/api/orders/${orderId}/status`, {
      status: newStatus,
      partnerId: partnerId
    });
    toast('Order status updated!', 'success');
    closeModal('status-modal');
    await loadOrders();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  }
}

// ── CREATE DELIVERY ───────────────────────────────────────
async function createDelivery(orderId) {
  try {
    const delivery = await api.post(`/api/deliveries/order/${orderId}`, {});
    toast(`Delivery #${delivery.id} created!`, 'success');
    const ctx = (typeof getContextPath === 'function') ? getContextPath() : '';
    setTimeout(() => window.location.href = ctx + '/food/deliveries', 1000);
  } catch (err) {
    toast('Error creating delivery: ' + err.message, 'error');
  }
}
