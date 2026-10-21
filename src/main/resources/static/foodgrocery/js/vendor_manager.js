/**
 * vendor_manager.js — Vendor Dashboard logic
 */

let myVendor = null;
let stompClient = null;

document.addEventListener('DOMContentLoaded', async () => {
  const user = requireAuth(['VENDOR_MANAGER']);
  if (!user) return;
  
  await initVendorData(user.id);
  setupEventListeners();
});

async function initVendorData(userId) {
  try {
    // Get all vendors/shops and find the ones where this user is the manager
    const shops = await api.get('/api/shops/all');
    // NOTE: If the backend doesn't link shop to managerId directly, we assume VENDOR_MANAGER 
    // uses the products/orders endpoint for their vendor.
    // For now, let's load orders and products by assuming they belong to a vendor entity 
    // or just loading all and filtering (in a real app, backend filters this).
    
    // We will just load the dashboard data
    await loadDashboard();
    connectWebSocket();
  } catch (err) {
    console.error(err);
  }
}

async function loadDashboard() {
  try {
    const [rawOrders, rawProducts] = await Promise.all([
      api.get('/api/orders/all'),
      api.get('/api/products')
    ]);

    const user = getCurrentUser();
    let orders = rawOrders;
    let products = rawProducts;
    
    if (user && user.role === 'VENDOR_MANAGER') {
      orders = rawOrders.filter(o => o.shop?.managerId === user.id);
      products = rawProducts.filter(p => p.shop?.managerId === user.id);
    }
    
    const pending = orders.filter(o => o.status === 'PENDING');
    const preparing = orders.filter(o => o.status === 'PREPARING' || o.status === 'ACCEPTED' || o.status === 'ON_THE_WAY');
    const completed = orders.filter(o => o.status === 'DELIVERED');
    
    document.getElementById('stat-new').textContent = pending.length;
    document.getElementById('stat-preparing').textContent = preparing.length;
    document.getElementById('stat-products').textContent = products.length;
    document.getElementById('stat-sales').textContent = completed.reduce((sum, o) => sum + o.totalAmount, 0).toLocaleString();

    renderLiveOrders([...pending, ...preparing]);
  } catch (err) {
    toast('Error loading dashboard data', 'error');
  }
}

function renderLiveOrders(orders) {
  const container = document.getElementById('live-orders-container');
  if (!orders.length) {
    showEmptyState('live-orders-container', 'No active orders right now', '🎉');
    return;
  }

  orders.sort((a,b) => b.id - a.id);

  container.innerHTML = orders.map(o => `
    <div class="card" style="border-left:4px solid ${o.status === 'PENDING' ? 'var(--clr-warning)' : 'var(--clr-info)'}">
      <div style="display:flex;justify-content:space-between;margin-bottom:8px">
        <div style="font-weight:700">Order #${o.id}</div>
        ${statusBadge(o.status)}
      </div>
      <div style="font-size:0.85rem;margin-bottom:4px">👤 ${escapeHtml(o.customer?.fullName || 'Guest')}</div>
      <div style="font-size:0.85rem;color:var(--clr-text-muted);margin-bottom:12px">📍 ${escapeHtml(o.deliveryAddress || '')}</div>
      
      <div style="background:var(--clr-surface-2);padding:8px;border-radius:var(--r-sm);margin-bottom:12px;font-size:0.8rem">
        ${o.items?.map(i => `<div>${i.quantity}x ${escapeHtml(i.product?.name)}</div>`).join('') || 'No items'}
      </div>

      <div style="display:flex;justify-content:space-between;align-items:center">
        <div style="font-weight:600;color:var(--clr-primary)">${formatCurrency(o.totalAmount)}</div>
        <div style="display:flex;gap:4px">
          ${o.status === 'PENDING' 
            ? `<button class="btn btn-sm btn-primary" onclick="updateOrderStatus(${o.id}, 'ACCEPTED')">Accept</button>`
            : (o.status === 'ACCEPTED' || o.status === 'PREPARING')
              ? `<button class="btn btn-sm btn-success" onclick="createDelivery(${o.id})">Create Delivery</button>`
              : ''
          }
        </div>
      </div>
    </div>
  `).join('');
}

async function updateOrderStatus(id, status) {
  try {
    await api.put(`/api/orders/${id}/status`, { status });
    toast('Order status updated', 'success');
    loadDashboard();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  }
}

function setupEventListeners() {
  document.getElementById('btn-refresh').addEventListener('click', loadDashboard);
}

function connectWebSocket() {
  const ctx = (typeof getContextPath === 'function') ? getContextPath() : (window.APP_CONTEXT_PATH || '');
  const socket = new SockJS(ctx + '/ws');
  stompClient = Stomp.over(socket);
  stompClient.debug = null;
  stompClient.connect({}, () => {
    stompClient.subscribe('/topic/food/orders', (message) => {
      toast('New order update received!', 'info');
      loadDashboard();
    });
  });
}

function escapeHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

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
