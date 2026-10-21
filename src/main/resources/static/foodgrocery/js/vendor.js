/**
 * vendor.js — Shop Menu and Cart logic for Customers
 */

let vendor = null;
let products = [];
let cart = JSON.parse(localStorage.getItem('cart') || '[]');
let cartVendorId = localStorage.getItem('cartVendorId') || null;

document.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const vendorId = urlParams.get('id');
  if (!vendorId) {
    const ctx = (typeof getContextPath === 'function') ? getContextPath() : '';
    window.location.href = ctx + '/food/customer';
    return;
  }
  
  await loadShopData(vendorId);
  updateCartBadge();
});

async function loadShopData(id) {
  try {
    vendor = await api.get(`/api/shops/${id}`);
    document.title = `${vendor.name} - Menu`;
    
    // Fill Hero
    if (vendor.imageUrl) {
      document.getElementById('vImg').innerHTML = `<img src="${vendor.imageUrl}" class="vendor-hero-img" />`;
    }
    document.getElementById('vName').textContent = vendor.name;
    document.getElementById('vDesc').textContent = vendor.description || '';
    document.getElementById('vRating').textContent = `⭐ ${vendor.rating || '4.5'}`;
    document.getElementById('vAddress').textContent = `📍 ${vendor.city || vendor.address || 'Sri Lanka'}`;
    document.getElementById('vTime').textContent = `🕐 ${vendor.deliveryTimeMinutes || 30} min`;
    document.getElementById('vFee').textContent = formatCurrency(vendor.deliveryFee);

    // Fetch products
    // NOTE: Products endpoint uses shopId param or vendorId. 
    // In our api, it's /api/products then filter by shopId, or /api/shops/{id}/products
    // Assuming /api/shops/{id}/products exists or fallback to filter
    products = await api.get(`/api/products/shop/${id}`);
    renderMenu();
  } catch (err) {
    document.getElementById('vName').textContent = 'Shop Not Found';
    document.getElementById('menuGrid').innerHTML = `<div class="empty-state"><p>Could not load menu.</p></div>`;
  }
}

function renderMenu() {
  const grid = document.getElementById('menuGrid');
  if (!products.length) {
    grid.innerHTML = '<div class="empty-state"><p>No items found.</p></div>';
    return;
  }
  
  grid.innerHTML = products.map(p => `
    <div class="card" style="display:flex;flex-direction:column;height:100%;padding:0;overflow:hidden">
      <div style="height:140px;background:var(--clr-surface-3);display:flex;align-items:center;justify-content:center;font-size:3rem;position:relative">
        ${p.imageUrl ? `<img src="${p.imageUrl}" style="width:100%;height:100%;object-fit:cover"/>` : (p.category?.toLowerCase().includes('drink') ? '🥤' : '🍔')}
        ${p.popular ? '<span class="badge badge-warning" style="position:absolute;top:8px;right:8px">⭐ Popular</span>' : ''}
      </div>
      <div style="padding:var(--sp-md);display:flex;flex-direction:column;flex:1">
        <h3 style="margin-bottom:4px">${escapeHtml(p.name)}</h3>
        <div style="font-size:0.75rem;color:var(--clr-text-muted);margin-bottom:auto;min-height:30px">
          ${escapeHtml(p.description || '')}
        </div>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-top:var(--sp-sm)">
          <div style="font-weight:700;color:var(--clr-primary)">${formatCurrency(p.price)}</div>
          <button class="btn btn-sm btn-primary" onclick="addToCart(${p.id})">+ Add</button>
        </div>
      </div>
    </div>
  `).join('');
}

function addToCart(pid) {
  if (cartVendorId && cartVendorId != vendor.id) {
    if (!confirm('Your cart contains items from another shop. Clear it to add this item?')) return;
    cart = [];
  }
  
  cartVendorId = vendor.id;
  localStorage.setItem('cartVendorId', cartVendorId);
  localStorage.setItem('cartDeliveryFee', vendor.deliveryFee);

  const product = products.find(p => p.id === pid);
  const existing = cart.find(i => i.id === pid);
  
  if (existing) {
    existing.quantity++;
  } else {
    cart.push({ id: product.id, name: product.name, price: product.price, quantity: 1 });
  }
  
  localStorage.setItem('cart', JSON.stringify(cart));
  updateCartBadge();
  toast(`${product.name} added to cart!`, 'success');
}

// ── CART SIDEBAR ──────────────────────────────────────────
function toggleCart() {
  const overlay = document.getElementById('cartOverlay');
  const backdrop = document.getElementById('cartBackdrop');
  if (overlay.classList.contains('open')) {
    overlay.classList.remove('open');
    backdrop.classList.remove('open');
  } else {
    renderCart();
    overlay.classList.add('open');
    backdrop.classList.add('open');
  }
}

function renderCart() {
  const itemsEl = document.getElementById('cartItems');
  const footer = document.getElementById('cartFooter');
  
  if (!cart.length) {
    itemsEl.innerHTML = `<div class="empty-state"><div class="empty-icon">🛒</div><p>Your cart is empty</p></div>`;
    footer.style.display = 'none';
    return;
  }
  
  let subtotal = 0;
  let deliveryFee = parseFloat(localStorage.getItem('cartDeliveryFee') || 0);
  
  itemsEl.innerHTML = cart.map((item, idx) => {
    subtotal += item.price * item.quantity;
    return `
      <div class="cart-item">
        <div class="cart-item-info">
          <div class="cart-item-name">${escapeHtml(item.name)}</div>
          <div class="cart-item-price">${formatCurrency(item.price * item.quantity)}</div>
        </div>
        <div class="qty-controls">
          <button class="qty-btn" onclick="updateCartQty(${idx}, -1)">−</button>
          <span style="font-weight:600;font-size:0.9rem">${item.quantity}</span>
          <button class="qty-btn" onclick="updateCartQty(${idx}, 1)">+</button>
        </div>
      </div>
    `;
  }).join('');
  
  document.getElementById('cartSubtotal').textContent = formatCurrency(subtotal);
  document.getElementById('cartDelivery').textContent = formatCurrency(deliveryFee);
  document.getElementById('cartTotal').textContent = formatCurrency(subtotal + deliveryFee);
  footer.style.display = 'block';
}

function updateCartQty(idx, delta) {
  cart[idx].quantity += delta;
  if (cart[idx].quantity <= 0) cart.splice(idx, 1);
  if (cart.length === 0) {
    localStorage.removeItem('cartVendorId');
    localStorage.removeItem('cartDeliveryFee');
  }
  localStorage.setItem('cart', JSON.stringify(cart));
  updateCartBadge();
  renderCart();
}

function updateCartBadge() {
  const total = cart.reduce((s, i) => s + i.quantity, 0);
  const badge = document.getElementById('cart-badge');
  badge.textContent = total;
  badge.style.display = total > 0 ? 'inline-block' : 'none';
}

function goToCheckout() {
  const user = getCurrentUser();
  const ctx = (typeof getContextPath === 'function') ? getContextPath() : '';
  if (!user) {
    toast('Please sign in to checkout', 'error');
    setTimeout(() => window.location.href = ctx + '/food/login', 1000);
    return;
  }
  window.location.href = ctx + '/food/checkout';
}

function escapeHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
