/**
 * customer.js - Customer Dashboard logic
 */

let allProducts = [];
let cart = [];
let myOrders = [];
let currentShopId = null; 

function getCartStorageKey() {
    const user = (typeof getCurrentUser === 'function') ? getCurrentUser() : null;
    return user ? `food_cart_${user.id || user.userId}` : 'food_cart_guest';
}

function loadPersistedCart() {
    try {
        const raw = localStorage.getItem(getCartStorageKey());
        if (raw) cart = JSON.parse(raw);
    } catch(e) { cart = []; }
}

function persistCart() {
    try {
        localStorage.setItem(getCartStorageKey(), JSON.stringify(cart));
    } catch(e) {}
}

// Base fee and per km fee
const BASE_FEE = 120;
const PER_KM_FEE = 40;
let calculatedDistanceKm = 0;
let deliveryFee = 0;

let checkoutMap = null;
let customerMarker = null;

const SHOP_LAT = 6.9271; // Dummy shop location (Colombo)
const SHOP_LNG = 79.8612;

document.addEventListener('DOMContentLoaded', async () => {
    // Tab switching
    document.querySelectorAll('.nav-item[data-target]').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
            item.classList.add('active');
            
            document.querySelectorAll('.view-section').forEach(v => v.style.display = 'none');
            document.getElementById('view-' + item.dataset.target).style.display = 'block';
            
            if (item.dataset.target === 'orders') {
                loadOrders();
            }
        });
    });

    loadPersistedCart();
    renderCart();

    const urlShopId = new URLSearchParams(window.location.search).get('shopId');
    await loadProducts(urlShopId);

    // Initial user details setup
    const user = getCurrentUser();
    if (user) {
        document.getElementById('user-name-display').textContent = user.fullName || user.email;
    }
});

async function loadProducts(shopId) {
    try {
        let url = '/api/products';
        if (shopId) {
            url = `/api/products/shop/${shopId}`;
            currentShopId = shopId;
        }
        allProducts = await api.get(url);
        renderProducts(allProducts);
    } catch (err) {
        console.error(err);
        toast('Failed to load products', 'error');
    }
}

function renderProducts(products) {
    const list = document.getElementById('product-list');
    list.innerHTML = '';
    if (products.length === 0) {
        list.innerHTML = '<div style="grid-column:1/-1; padding:2rem; text-align:center; color:var(--text-muted)">No products found.</div>';
        return;
    }
    products.forEach(p => {
        list.innerHTML += `
        <div class="prod-card" onclick="viewProduct(${p.id})">
            <div class="prod-img">
                ${p.imageUrl ? '<img src="'+p.imageUrl+'" style="width:100%;height:100%;object-fit:cover;border-radius:12px;"/>' : '🍔'}
            </div>
            <div class="prod-info">
                <div class="prod-name">${escapeHtml(p.name)}</div>
                <div style="font-size:0.75rem; color:var(--text-muted); margin-bottom:0.5rem;">${escapeHtml(p.shop?.name || 'Shop')}</div>
                <div class="prod-bottom">
                    <span class="prod-price">Rs. ${p.price.toFixed(2)}</span>
                    ${p.stockQuantity > 0 ? 
                        `<button class="btn-add" onclick="event.stopPropagation(); addToCart(${p.id})"><i class="fa-solid fa-plus"></i></button>` : 
                        `<span style="color:#ef4444; font-size:0.8rem; font-weight:700; background:#fee2e2; padding:4px 8px; border-radius:6px;">Out of Stock</span>`
                    }
                </div>
            </div>
        </div>`;
    });
}

function viewProduct(id) {
    const p = allProducts.find(x => x.id === id);
    if (!p) return;
    
    document.getElementById('modal-prod-img').innerHTML = p.imageUrl ? `<img src="${p.imageUrl}" style="width:100%;height:100%;object-fit:cover;border-radius:12px;"/>` : '🍔';
    document.getElementById('modal-prod-name').textContent = p.name;
    document.getElementById('modal-prod-desc').textContent = p.description || 'No description available.';
    document.getElementById('modal-prod-price').textContent = `Rs. ${p.price.toFixed(2)}`;
    document.getElementById('modal-prod-shop').textContent = `Shop: ${p.shop?.name || 'Unknown'}`;
    
    const btn = document.getElementById('modal-add-btn');
    btn.onclick = () => {
        addToCart(p.id);
        closeModal('product-modal');
    };
    
    openModal('product-modal');
}

function addToCart(productId) {
    const p = allProducts.find(x => x.id === productId);
    if (!p) return;
    
    // Simple rule: Only 1 shop per order for now (can expand later)
    if (cart.length > 0 && cart[0].product.shop?.id !== p.shop?.id) {
        toast('You can only order from one shop at a time. Please clear your cart first.', 'error');
        return;
    }

    currentShopId = p.shop?.id;

    const existing = cart.find(item => item.product.id === productId);
    const currentQty = existing ? existing.qty : 0;
    
    if (currentQty + 1 > p.stockQuantity) {
        toast(`Not enough stock available. Only ${p.stockQuantity} left.`, 'warning');
        return;
    }

    if (existing) {
        existing.qty++;
    } else {
        cart.push({ product: p, qty: 1 });
    }
    toast(`${p.name} added to cart`, 'success');
    persistCart();
    renderCart();
}

function updateCartQty(productId, delta) {
    const item = cart.find(x => x.product.id === productId);
    if (!item) return;
    
    if (delta > 0 && item.qty + delta > item.product.stockQuantity) {
        toast(`Cannot add more. Only ${item.product.stockQuantity} in stock.`, 'warning');
        return;
    }
    
    item.qty += delta;
    if (item.qty <= 0) {
        cart = cart.filter(x => x.product.id !== productId);
    }
    if (cart.length === 0) currentShopId = null;
    persistCart();
    renderCart();
}

function renderCart() {
    const list = document.getElementById('cart-items');
    list.innerHTML = '';
    let subtotal = 0;
    
    if (cart.length === 0) {
        list.innerHTML = '<div style="text-align:center; padding:1rem; color:var(--text-muted)">Cart is empty</div>';
        document.getElementById('cart-subtotal').textContent = 'Rs. 0.00';
        document.getElementById('cart-total').textContent = 'Rs. 0.00';
        document.getElementById('btn-checkout-init').disabled = true;
        return;
    }

    document.getElementById('btn-checkout-init').disabled = false;

    cart.forEach(item => {
        subtotal += item.product.price * item.qty;
        list.innerHTML += `
        <div class="cart-item">
            <div class="cart-item-img">${item.product.imageUrl ? `<img src="${item.product.imageUrl}" style="width:100%;height:100%;object-fit:cover;border-radius:8px;"/>` : '🍔'}</div>
            <div class="cart-item-details">
                <div class="cart-item-name">${escapeHtml(item.product.name)}</div>
                <div class="cart-item-qty">
                    <button onclick="updateCartQty(${item.product.id}, -1)">-</button>
                    <span>${item.qty}</span>
                    <button onclick="updateCartQty(${item.product.id}, 1)">+</button>
                </div>
            </div>
            <div class="cart-item-price">Rs. ${(item.product.price * item.qty).toFixed(2)}</div>
        </div>`;
    });

    document.getElementById('cart-subtotal').textContent = `Rs. ${subtotal.toFixed(2)}`;
    // Total is calculated in checkout modal after map distance
    document.getElementById('cart-total').textContent = `Rs. ${subtotal.toFixed(2)} + Delivery`;
}

// ── CHECKOUT FLOW ─────────────────────────────────────────────
function initCheckout() {
    if (cart.length === 0) return;
    const user = (typeof getCurrentUser === 'function') ? getCurrentUser() : null;
    if (!user) {
        if (typeof toast === 'function') toast('Please log in with your NEXTGO account to place an order', 'info');
        const ctx = (typeof getContextPath === 'function') ? getContextPath() : '';
        setTimeout(() => {
            window.location.href = ctx + '/login?redirect=/food';
        }, 800);
        return;
    }
    document.getElementById('checkout-address').value = user?.address || '';
    document.getElementById('checkout-notes').value = '';
    
    calculatedDistanceKm = 0;
    deliveryFee = 0;
    updateCheckoutTotals();

    openModal('checkout-modal');
    
    // Initialize map after modal is visible so Leaflet sizes correctly
    setTimeout(() => {
        if (!checkoutMap) {
            checkoutMap = L.map('checkout-map').setView([SHOP_LAT, SHOP_LNG], 12);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(checkoutMap);

            // Shop marker (fixed)
            L.marker([SHOP_LAT, SHOP_LNG]).addTo(checkoutMap)
                .bindPopup('Shop Location').openPopup();

            // Customer draggable marker
            customerMarker = L.marker([SHOP_LAT - 0.02, SHOP_LNG + 0.02], {draggable: true}).addTo(checkoutMap)
                .bindPopup('Drag to your delivery location');
            
            customerMarker.on('dragend', function(e) {
                calculateDistance(e.target.getLatLng());
            });
            
            // Initial distance
            calculateDistance(customerMarker.getLatLng());
        } else {
            checkoutMap.invalidateSize();
            calculateDistance(customerMarker.getLatLng());
        }
    }, 300);
}

function calculateDistance(latlng) {
    const shopLatLng = L.latLng(SHOP_LAT, SHOP_LNG);
    const distanceMeters = shopLatLng.distanceTo(latlng);
    calculatedDistanceKm = distanceMeters / 1000;
    
    // Rs. 120 base + Rs. 40 per km
    deliveryFee = BASE_FEE + (calculatedDistanceKm * PER_KM_FEE);
    
    // Estimated time: 15 mins prep time + ~5 mins per km
    const estimatedTimeMins = 15 + Math.round(calculatedDistanceKm * 5);
    
    document.getElementById('calc-distance').textContent = calculatedDistanceKm.toFixed(2) + ' km';
    const timeEl = document.getElementById('calc-time');
    if (timeEl) {
        timeEl.textContent = estimatedTimeMins + ' mins';
    }
    
    updateCheckoutTotals();
}

function updateCheckoutTotals() {
    const subtotal = cart.reduce((sum, item) => sum + (item.product.price * item.qty), 0);
    const total = subtotal + deliveryFee;
    document.getElementById('checkout-subtotal').textContent = `Rs. ${subtotal.toFixed(2)}`;
    document.getElementById('checkout-delivery').textContent = `Rs. ${deliveryFee.toFixed(2)}`;
    document.getElementById('checkout-total').textContent = `Rs. ${total.toFixed(2)}`;
}

async function placeOrder() {
    if (cart.length === 0) return;
    
    const address = document.getElementById('checkout-address').value.trim();
    if (!address) {
        toast('Delivery address is required', 'error');
        return;
    }

    const payload = {
        customerId: getCurrentUser().id,
        shopId: currentShopId,
        items: cart.map(c => ({ productId: c.product.id, quantity: c.qty })),
        deliveryAddress: address,
        specialInstructions: document.getElementById('checkout-notes').value.trim(),
        deliveryFee: deliveryFee
    };

    try {
        const btn = document.getElementById('btn-submit-order');
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Placing...';
        
        await api.post('/api/orders', payload);
        toast('Order placed successfully! Waiting for shop to accept.', 'success');
        
        cart = [];
        persistCart();
        renderCart();
        closeModal('checkout-modal');
        
        // Switch to orders tab
        document.querySelector('.nav-item[data-target="orders"]').click();
    } catch (err) {
        toast('Error placing order: ' + err.message, 'error');
    } finally {
        const btn = document.getElementById('btn-submit-order');
        btn.disabled = false;
        btn.innerHTML = 'Place Order';
    }
}

// ── ORDERS FLOW ─────────────────────────────────────────────
async function loadOrders() {
    const list = document.getElementById('orders-list');
    const user = (typeof getCurrentUser === 'function') ? getCurrentUser() : null;
    const ctx = (typeof getContextPath === 'function') ? getContextPath() : '';
    if (!user) {
        list.innerHTML = `
            <div style="padding:3rem 1rem;text-align:center;color:var(--text-muted);">
                <i class="fa-solid fa-lock fa-3x mb-3" style="color:var(--text-muted);opacity:0.6;"></i>
                <h4 style="color:var(--text-dark);margin-bottom:0.5rem;">Sign In Required</h4>
                <p style="margin-bottom:1.5rem;">Please log in with your NEXTGO account to view your order history and live deliveries.</p>
                <a href="${ctx}/login?redirect=/food" class="btn-checkout" style="display:inline-block;text-decoration:none;padding:10px 24px;">Log In to NEXTGO</a>
            </div>`;
        return;
    }
    list.innerHTML = '<div style="padding:2rem;text-align:center;">Loading orders...</div>';
    try {
        // The endpoint is paginated, we just take first page for simplicity
        const res = await api.get(`/api/orders/customer/my-orders?customerId=${user.id}&size=50`);
        myOrders = res.content || [];
        renderOrders();
    } catch (err) {
        console.error(err);
        list.innerHTML = '<div style="padding:2rem;text-align:center;color:red;">Failed to load orders.</div>';
    }
}

function renderOrders() {
    const list = document.getElementById('orders-list');
    list.innerHTML = '';
    if (myOrders.length === 0) {
        list.innerHTML = '<div style="padding:2rem;text-align:center;color:var(--text-muted);">You have no orders.</div>';
        return;
    }

    myOrders.forEach(o => {
        let statusColor = 'var(--text-muted)';
        let displayStatus = o.status;
        
        if (o.status === 'PENDING') statusColor = 'var(--clr-warning)';
        else if (o.status === 'ACCEPTED') {
            statusColor = 'var(--clr-accent)';
            displayStatus = 'ORDER CONFIRMED';
        }
        else if (o.status === 'PREPARING') statusColor = 'var(--clr-info)';
        else if (o.status === 'ON_THE_WAY') statusColor = 'var(--clr-primary)';
        else if (o.status === 'DELIVERED') statusColor = 'var(--clr-success)';
        else if (o.status === 'CANCELLED') statusColor = 'var(--clr-danger)';

        let payBtnHtml = '';
        if (o.status === 'ACCEPTED' && !o.paymentMethod) {
            payBtnHtml = `<button style="background:var(--primary-red); color:white; border:none; padding:0.5rem 1rem; border-radius:8px; cursor:pointer; margin-top:0.5rem; font-weight:600;" onclick="openPaymentModal(${o.id})">Pay Now</button>`;
        } else if (o.paymentMethod) {
            payBtnHtml = `<div style="margin-top:0.5rem; font-size:0.8rem; color:var(--clr-success);"><i class="fa-solid fa-check-circle"></i> Paid via ${o.paymentMethod}</div>`;
        }

        list.innerHTML += `
        <div style="background:rgba(255,255,255,0.9); padding:1rem; border-radius:12px; margin-bottom:1rem; border:1px solid var(--border-light); box-shadow:0 4px 15px rgba(0,0,0,0.03);">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem;">
                <h4 style="margin:0; color:var(--text-dark);">Order #${o.id}</h4>
                <span style="background:${statusColor}; color:#fff; padding:0.2rem 0.6rem; border-radius:4px; font-size:0.75rem; font-weight:600;">${displayStatus}</span>
            </div>
            <div style="font-size:0.85rem; color:var(--text-muted); margin-bottom:0.5rem;">
                Shop: <strong style="color:var(--text-dark)">${escapeHtml(o.shop?.name || 'Unknown')}</strong><br>
                Items: ${o.items.length} | Delivery: Rs. ${(o.deliveryFee || 0).toFixed(2)}<br>
                <strong style="color:var(--text-dark); font-size:1rem; display:inline-block; margin-top:0.5rem;">Total: Rs. ${o.totalAmount.toFixed(2)}</strong>
            </div>
            ${payBtnHtml}
        </div>`;
    });
}

// ── PAYMENT FLOW ─────────────────────────────────────────────
let paymentOrderId = null;

function openPaymentModal(orderId) {
    paymentOrderId = orderId;
    openModal('payment-modal');
    setTimeout(toggleCardFields, 100);
}

function toggleCardFields() {
    const method = document.getElementById('payment-method').value;
    const cardSection = document.getElementById('card-details-section');
    if (cardSection) {
        cardSection.style.display = method === 'CARD' ? 'block' : 'none';
    }
}

// Add formatting for card inputs globally
document.addEventListener('DOMContentLoaded', () => {
    const ccInput = document.getElementById('card-number');
    if (ccInput) {
        ccInput.addEventListener('input', function(e) {
            e.target.value = e.target.value.replace(/\D/g, '').replace(/(.{4})/g, '$1 ').trim();
        });
    }
    const expiryInput = document.getElementById('card-expiry');
    if (expiryInput) {
        expiryInput.addEventListener('input', function(e) {
            let val = e.target.value.replace(/\D/g, '');
            if (val.length >= 2) {
                val = val.substring(0,2) + '/' + val.substring(2,4);
            }
            e.target.value = val;
        });
    }
});

async function submitPayment() {
    if (!paymentOrderId) return;
    const method = document.getElementById('payment-method').value;
    
    if (method === 'CARD') {
        const name = document.getElementById('card-name').value.trim();
        const number = document.getElementById('card-number').value.trim();
        const expiry = document.getElementById('card-expiry').value.trim();
        const cvc = document.getElementById('card-cvc').value.trim();
        
        if (!name || number.length < 19 || expiry.length < 5 || cvc.length < 3) {
            toast('Please fill out all card details correctly', 'error');
            return;
        }
    }
    
    try {
        const btn = document.getElementById('btn-submit-payment');
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processing...';
        
        await api.put(`/api/orders/${paymentOrderId}/pay`, { paymentMethod: method });
        toast('Payment successful!', 'success');
        
        closeModal('payment-modal');
        loadOrders();
    } catch (err) {
        toast('Error processing payment: ' + err.message, 'error');
    } finally {
        const btn = document.getElementById('btn-submit-payment');
        btn.disabled = false;
        btn.innerHTML = 'Confirm Payment';
    }
}

function escapeHtml(str) {
    return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
