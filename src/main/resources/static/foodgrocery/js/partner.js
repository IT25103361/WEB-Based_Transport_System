/**
 * partner.js — Delivery Partner logic
 */

let stompClient = null;

document.addEventListener('DOMContentLoaded', () => {
  const user = requireAuth(['DELIVERY_PARTNER']);
  if (!user) return;
  
  setupTabs();
  loadOrders();
  initWS();
});

function setupTabs() {
  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.tab').forEach(t => {
        t.classList.remove('active');
        t.style.borderBottomColor = 'transparent';
        t.style.color = 'var(--clr-text-muted)';
      });
      tab.classList.add('active');
      tab.style.borderBottomColor = 'var(--clr-primary)';
      tab.style.color = 'var(--clr-primary)';
      
      const tabType = tab.dataset.tab;
      document.getElementById('new-jobs-container').style.display = tabType === 'new' ? 'block' : 'none';
      document.getElementById('active-jobs-container').style.display = tabType === 'active' ? 'block' : 'none';
      document.getElementById('history-jobs-container').style.display = tabType === 'history' ? 'block' : 'none';
      
      loadOrders();
    });
  });
}

async function loadOrders() {
  const user = getCurrentUser();
  if (!user) return;
  
  try {
    const orders = await api.get('/api/orders/all');
    
    // Unassigned orders that are ready or preparing
    const newJobs = orders.filter(o => 
      !o.deliveryPartner && 
      (o.status === 'PENDING' || o.status === 'PREPARING' || o.status === 'ACCEPTED' || o.status === 'READY_FOR_PICKUP')
    );
    
    // Orders assigned to this partner that are not yet delivered
    const activeJobs = orders.filter(o => 
      (o.deliveryPartner?.user?.id === user.id || o.deliveryPartner?.id === user.id) && 
      o.status !== 'DELIVERED' && 
      o.status !== 'CANCELLED'
    );
    
    // Completed deliveries for this partner
    const historyJobs = orders.filter(o => 
      (o.deliveryPartner?.user?.id === user.id || o.deliveryPartner?.id === user.id) && 
      o.status === 'DELIVERED'
    );
    
    renderJobs(newJobs, 'new-jobs-container', 'new');
    renderJobs(activeJobs, 'active-jobs-container', 'active');
    renderJobs(historyJobs, 'history-jobs-container', 'history');
  } catch (err) {
    console.error('Failed to load orders', err);
  }
}

function renderJobs(orders, containerId, tabType) {
  const container = document.getElementById(containerId);
  
  if (!orders.length) {
    container.innerHTML = `<div class="empty-state"><p>No orders here at the moment.</p></div>`;
    return;
  }
  
  container.innerHTML = orders.map(o => `
    <div class="job-card">
      <div class="job-header">
        <span class="job-id">Order #${o.id}</span>
        ${statusBadge(o.status)}
      </div>
      <div class="job-route">
        <div class="route-point">
          <div class="route-label">Pickup</div>
          <div class="route-addr">${escapeHtml(o.shop?.name)} (${escapeHtml(o.shop?.city || o.shop?.address || '')})</div>
        </div>
        <div class="route-point">
          <div class="route-label">Drop-off</div>
          <div class="route-addr">${escapeHtml(o.deliveryAddress)}</div>
        </div>
      </div>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-top:8px">
        <div style="font-size:1.1rem;font-weight:800;color:var(--clr-success)">${formatCurrency(o.deliveryFee || 150)}</div>
        ${tabType === 'new' 
          ? `<button class="btn btn-primary" onclick="acceptOrder(${o.id})">Accept Order</button>`
          : tabType === 'active' 
          ? `<button class="btn btn-success" onclick="markDelivered(${o.id})">Mark Delivered</button>`
          : `<div style="font-weight:600; color:var(--clr-text-muted)">Completed</div>`
        }
      </div>
    </div>
  `).join('');
}

async function acceptOrder(id) {
  const user = getCurrentUser();
  try {
    await api.put(`/api/orders/${id}/status`, { status: 'ACCEPTED', partnerId: user.id });
    toast('Order Accepted! Waiting for customer payment.', 'success');
    document.querySelector('.tab[data-tab="active"]').click();
  } catch(e) {
    toast('Error accepting order', 'error');
  }
}

async function markDelivered(id) {
  if (!await confirmDialog('Confirm order delivered to customer?')) return;
  const user = getCurrentUser();
  try {
    await api.put(`/api/orders/${id}/status`, { status: 'DELIVERED', partnerId: user.id });
    toast('Order marked as delivered. Good job!', 'success');
    loadOrders();
  } catch(e) {
    toast('Error updating order', 'error');
  }
}

function initWS() {
  const ctx = (typeof getContextPath === 'function') ? getContextPath() : (window.APP_CONTEXT_PATH || '');
  const socket = new SockJS(ctx + '/ws');
  stompClient = Stomp.over(socket);
  stompClient.debug = null;
  stompClient.connect({}, () => {
    stompClient.subscribe('/topic/food/orders', () => {
      loadOrders();
    });
  });
}

function escapeHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
