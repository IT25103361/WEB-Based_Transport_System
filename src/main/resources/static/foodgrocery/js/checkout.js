/**
 * checkout.js — Logic for Checkout page
 */

function getCartKey() {
  const user = (typeof getCurrentUser === 'function') ? getCurrentUser() : null;
  return user ? `food_cart_${user.id || user.userId}` : 'cart';
}

let cart = JSON.parse(localStorage.getItem(getCartKey()) || localStorage.getItem('cart') || '[]');
let cartVendorId = localStorage.getItem('cartVendorId');
let deliveryFee = parseFloat(localStorage.getItem('cartDeliveryFee') || 0);

document.addEventListener('DOMContentLoaded', async () => {
  const user = requireAuth();
  if (!user) return;

  if (cart.length === 0) {
    const ctx = (typeof getContextPath === 'function') ? getContextPath() : '';
    window.location.href = ctx + '/food/customer';
    return;
  }

  await loadUserAddress(user.id);
  renderSummary();

  document.getElementById('btn-place-order').addEventListener('click', () => placeOrder(user.id));
});

async function loadUserAddress(userId) {
  try {
    const user = await api.get(`/api/auth/users/${userId}`);
    document.getElementById('chk-address').value = user.address || '';
  } catch(e) {
    console.error('Failed to load address', e);
  }
}

function renderSummary() {
  const itemsEl = document.getElementById('summaryItems');
  let subtotal = 0;
  
  itemsEl.innerHTML = cart.map(item => {
    subtotal += item.price * item.quantity;
    return `
      <div class="order-summary-item">
        <div>
          <span style="font-weight:700; color:var(--clr-primary)">${item.quantity}x</span> ${escapeHtml(item.name)}
        </div>
        <div style="font-weight:500">${formatCurrency(item.price * item.quantity)}</div>
      </div>
    `;
  }).join('');
  
  document.getElementById('sum-subtotal').textContent = formatCurrency(subtotal);
  document.getElementById('sum-delivery').textContent = formatCurrency(deliveryFee);
  document.getElementById('sum-total').textContent = formatCurrency(subtotal + deliveryFee);
}

async function placeOrder(customerId) {
  const address = document.getElementById('chk-address').value.trim();
  const instructions = document.getElementById('chk-instructions').value.trim();
  
  if (!address) {
    toast('Delivery address is required', 'error'); 
    return;
  }
  
  const btn = document.getElementById('btn-place-order');
  btn.disabled = true;
  btn.textContent = 'Placing Order...';
  
  const payload = {
    customerId: customerId,
    vendorId: parseInt(cartVendorId),
    items: cart.map(i => ({ productId: i.id, quantity: i.quantity })),
    deliveryAddress: address,
    specialInstructions: instructions || null
  };
  
  try {
    const order = await api.post('/api/orders', payload);
    if (order && order.id) {
      localStorage.removeItem(getCartKey());
      localStorage.removeItem('cart');
      localStorage.removeItem('cartVendorId');
      localStorage.removeItem('cartDeliveryFee');
      const ctx = (typeof getContextPath === 'function') ? getContextPath() : '';
      window.location.href = `${ctx}/food/tracking?orderId=${order.id}`;
    } else {
      toast('Failed to place order', 'error');
      btn.disabled = false;
      btn.textContent = 'Place Order';
    }
  } catch (err) {
    toast('Error: ' + err.message, 'error');
    btn.disabled = false;
    btn.textContent = 'Place Order';
  }
}
