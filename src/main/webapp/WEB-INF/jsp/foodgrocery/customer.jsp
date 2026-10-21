<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Customer Dashboard - FoodGrocer</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <!-- Leaflet CSS for Map -->
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <link rel="stylesheet" href="${contextPath}/foodgrocery/css/customer.css?v=2">
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@900&display=swap');
        .main-content {
            background: linear-gradient(rgba(255,255,255,0.4), rgba(255,255,255,0.4)), url('${contextPath}/foodgrocery/images/hero-banner.png') center/cover fixed;
            padding: 30px; overflow-y: auto;
        }
        .animated-name {
            font-family: 'Montserrat', sans-serif;
            font-size: 4.5rem;
            font-weight: 900;
            text-transform: uppercase;
            color: #ffffff;
            letter-spacing: 4px;
            text-shadow: 0 5px 15px rgba(0,0,0,0.2), 0 0 20px rgba(255,255,255,0.4);
            animation: float-glow 3s ease-in-out infinite;
            margin-bottom: 0.5rem;
            display: inline-block;
        }
        @keyframes float-glow {
            0% { transform: translateY(0px); text-shadow: 0 5px 15px rgba(0,0,0,0.2), 0 0 20px rgba(255,255,255,0.4); }
            50% { transform: translateY(-10px); text-shadow: 0 15px 25px rgba(0,0,0,0.2), 0 0 40px rgba(255,255,255,0.8); }
            100% { transform: translateY(0px); text-shadow: 0 5px 15px rgba(0,0,0,0.2), 0 0 20px rgba(255,255,255,0.4); }
        }
        .view-section { display: none; }
        .view-section.active { display: block; }
        .modal { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center; backdrop-filter: blur(3px); }
        .modal.active { display: flex; }
        .modal-content { background: var(--bg-white); color: var(--text-dark); padding: 2rem; border-radius: 16px; border: 1px solid var(--border-light); width: 90%; max-width: 500px; max-height: 90vh; overflow-y: auto; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
        .modal-header h2 { margin: 0; font-size: 1.5rem; color: var(--text-dark); }
        .btn-close { background: none; border: none; color: var(--text-muted); font-size: 1.5rem; cursor: pointer; }
        .btn-close:hover { color: var(--primary-red); }
        
        #checkout-map { height: 250px; width: 100%; border-radius: 8px; margin-bottom: 1rem; border: 1px solid var(--border-light); }
        .fee-calc { background: var(--bg-main); padding: 1rem; border-radius: 8px; margin-bottom: 1rem; font-size: 0.9rem; color: var(--text-dark); }
        .fee-calc div { display: flex; justify-content: space-between; margin-bottom: 0.25rem; }
        .form-label { display: block; margin-bottom: 0.5rem; color: var(--text-muted); font-size: 0.9rem; font-weight: 500; }
        .form-control { width: 100%; padding: 0.8rem; border: 1px solid var(--border-light); border-radius: 8px; background: var(--bg-main); color: var(--text-dark); font-family: 'Inter', sans-serif; }
    </style>
</head>
<body>
    <div class="app-container">
        <!-- Sidebar -->
        <aside class="sidebar">
            <div class="logo">
                <i class="fa-solid fa-pizza-slice text-primary"></i>
                <span>FoodGrocer</span>
            </div>
            <nav class="nav-menu">
                <a href="#" class="nav-item active" data-target="dashboard">
                    <i class="fa-solid fa-house"></i> Dashboard
                </a>
                <a href="#" class="nav-item" data-target="orders">
                    <i class="fa-solid fa-box"></i> My Orders
                </a>
                <a href="${contextPath}/ride/home" class="nav-item">
                    <i class="fa-solid fa-arrow-left"></i> NEXTGO Hub
                </a>
            </nav>
            <div class="sidebar-footer">
                <c:choose>
                    <c:when test="${not empty foodUser}">
                        <div style="padding: 0.5rem 1rem; font-size: 0.85rem; color: var(--text-dark); font-weight: 600; display: flex; align-items: center; gap: 0.5rem;">
                            <i class="fa-solid fa-circle-user text-primary"></i>
                            <span class="text-truncate"><c:out value="${foodUser.fullName}" /></span>
                        </div>
                        <a href="javascript:void(0)" onclick="logout()" class="nav-item" style="color:var(--clr-danger)">
                            <i class="fa-solid fa-right-from-bracket"></i> Logout
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${contextPath}/login?redirect=/food" class="nav-item" style="color:var(--clr-primary); font-weight:600;">
                            <i class="fa-solid fa-right-to-bracket"></i> Login / Sign In
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
            <header class="top-bar">
                <div class="search-box">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input type="text" placeholder="Search...">
                </div>
            </header>

            <!-- DASHBOARD VIEW -->
            <div id="view-dashboard" class="view-section" style="display: block;">
                <div class="hero-banner" style="background: rgba(255,255,255,0.3); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(255,255,255,0.5); box-shadow: 0 8px 32px rgba(0,0,0,0.1); padding: 3rem 2rem; border-radius: 16px; margin-bottom: 2rem; min-height: 250px; display: flex; align-items: center;">
                    <div class="hero-content">
                        <h1 class="animated-name">IMETH</h1>
                        <p style="color: var(--text-dark); font-size: 1.1rem; margin: 0; font-weight: 500;">Fresh food & groceries delivered fast.</p>
                    </div>
                </div>

                <section class="products-section" style="background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); padding: 2rem; border-radius: 16px; border: 1px solid rgba(255,255,255,0.5); box-shadow: 0 8px 32px rgba(0,0,0,0.1);">
                    <div class="section-header" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
                        <h2 style="margin:0; color: var(--text-dark);">Available Products</h2>
                    </div>
                    <div class="product-cards" id="product-list" style="display:grid; grid-template-columns:repeat(auto-fill, minmax(200px, 1fr)); gap:1.5rem;">
                        <!-- Products injected here -->
                    </div>
                </section>
            </div>

            <!-- ORDERS VIEW -->
            <div id="view-orders" class="view-section" style="background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); padding: 2rem; border-radius: 16px; border: 1px solid rgba(255,255,255,0.5); box-shadow: 0 8px 32px rgba(0,0,0,0.1); min-height: 500px;">
                <h2 style="margin-bottom: 1.5rem; color: var(--text-dark);">My Orders</h2>
                <div id="orders-list">
                    <!-- Orders injected here -->
                </div>
            </div>
        </main>

        <!-- Right Panel (Cart) -->
        <aside class="right-panel">
            <div class="user-profile" style="display:flex; align-items:center; justify-content:flex-end; gap:1rem; margin-bottom:2rem;">
                <div class="user-badge" style="display:flex; align-items:center; gap:0.5rem; background:rgba(0,0,0,0.05); padding:0.5rem 1rem; border-radius:20px;">
                    <i class="fa-solid fa-user-circle" style="font-size:1.5rem; color:var(--primary-red);"></i>
                    <span id="user-name-display" style="font-weight:500;">Customer</span>
                </div>
            </div>

            <div class="cart-container" style="background:var(--bg-white); border-radius:16px; padding:1.5rem; border:1px solid var(--border-light); height:calc(100vh - 120px); display:flex; flex-direction:column; box-shadow: 0 5px 20px rgba(0,0,0,0.05);">
                <h3 style="margin-top:0; margin-bottom:1rem;">My Cart</h3>
                
                <div class="cart-items" id="cart-items" style="flex:1; overflow-y:auto; margin-bottom:1rem; display:flex; flex-direction:column; gap:1rem;">
                    <div style="text-align:center; padding:1rem; color:var(--text-muted)">Cart is empty</div>
                </div>

                <div class="cart-summary" style="border-top:1px solid var(--border-light); padding-top:1rem;">
                    <div class="summary-row" style="display:flex; justify-content:space-between; margin-bottom:0.5rem; color:var(--text-muted);">
                        <span>Subtotal</span>
                        <span id="cart-subtotal">Rs. 0.00</span>
                    </div>
                    <div class="summary-row total" style="display:flex; justify-content:space-between; margin-bottom:1rem; font-weight:700; font-size:1.1rem; color:var(--text-dark);">
                        <span>Total</span>
                        <span id="cart-total">Rs. 0.00</span>
                    </div>
                    <button id="btn-checkout-init" class="btn-checkout" onclick="initCheckout()" disabled>Checkout</button>
                </div>
            </div>
        </aside>
    </div>

    <!-- Product Details Modal -->
    <div class="modal" id="product-modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Product Details</h2>
                <button class="btn-close" onclick="closeModal('product-modal')">&times;</button>
            </div>
            <div id="modal-prod-img" style="height:200px; width:100%; background:rgba(0,0,0,0.05); border-radius:12px; margin-bottom:1rem; display:flex; align-items:center; justify-content:center; font-size:3rem;"></div>
            <h3 id="modal-prod-name" style="margin:0 0 0.5rem 0;">Name</h3>
            <div id="modal-prod-shop" style="font-size:0.85rem; color:var(--text-muted); margin-bottom:1rem;">Shop</div>
            <div id="modal-prod-price" style="font-size:1.25rem; font-weight:700; color:var(--primary-red); margin-bottom:1rem;">Rs. 0.00</div>
            <p id="modal-prod-desc" style="color:var(--text-muted); line-height:1.5; margin-bottom:1.5rem;"></p>
            <button id="modal-add-btn" class="btn-checkout">Add to Cart</button>
        </div>
    </div>

    <!-- Checkout Modal with Map -->
    <div class="modal" id="checkout-modal">
        <div class="modal-content" style="max-width:600px;">
            <div class="modal-header">
                <h2>Checkout</h2>
                <button class="btn-close" onclick="closeModal('checkout-modal')">&times;</button>
            </div>
            
            <label class="form-label">Delivery Location (Drag Pin)</label>
            <div id="checkout-map"></div>
            
            <div class="fee-calc">
                <div><span>Distance:</span> <strong id="calc-distance">0.00 km</strong></div>
                <div><span>Base Fee:</span> <strong>Rs. 120.00</strong></div>
                <div><span>Distance Fee:</span> <strong>Rs. 40/km</strong></div>
                <div><span>Est. Delivery Time:</span> <strong id="calc-time" style="color:var(--primary-red)">-- mins</strong></div>
            </div>

            <div class="form-group" style="margin-bottom:1rem;">
                <label class="form-label">Exact Delivery Address</label>
                <input type="text" id="checkout-address" class="form-control" style="width:100%;">
            </div>
            <div class="form-group" style="margin-bottom:1rem;">
                <label class="form-label">Special Instructions (Optional)</label>
                <textarea id="checkout-notes" class="form-control" style="width:100%; height:60px;"></textarea>
            </div>

            <div style="border-top:1px solid var(--border-light); padding-top:1rem; margin-top:1rem;">
                <div style="display:flex; justify-content:space-between; margin-bottom:0.5rem; color:var(--text-muted);">
                    <span>Items Subtotal</span>
                    <strong id="checkout-subtotal">Rs. 0.00</strong>
                </div>
                <div style="display:flex; justify-content:space-between; margin-bottom:0.5rem; color:var(--text-muted);">
                    <span>Delivery Fee</span>
                    <strong id="checkout-delivery">Rs. 0.00</strong>
                </div>
                <div style="display:flex; justify-content:space-between; font-size:1.25rem; font-weight:700; color:var(--text-dark); margin-bottom:1.5rem; margin-top:0.5rem;">
                    <span>Total Amount</span>
                    <strong id="checkout-total">Rs. 0.00</strong>
                </div>
            </div>

            <button id="btn-submit-order" class="btn-checkout" onclick="placeOrder()">Place Order</button>
            <p style="text-align:center; font-size:0.8rem; color:var(--text-muted); margin-top:1rem;">You will select a payment method once the shop accepts your order.</p>
        </div>
    </div>

    <!-- Payment Modal -->
    <div class="modal" id="payment-modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Select Payment Method</h2>
                <button class="btn-close" onclick="closeModal('payment-modal')">&times;</button>
            </div>
            
            <p style="color:var(--text-muted); margin-bottom:1.5rem;">Your order has been accepted! Please select how you would like to pay.</p>

            <div class="form-group" style="margin-bottom:1.5rem;">
                <label class="form-label">Payment Method</label>
                <select id="payment-method" class="form-control" style="width:100%;" onchange="toggleCardFields()">
                    <option value="CARD">Card Payment (Online)</option>
                    <option value="COD">Cash on Delivery</option>
                </select>
            </div>

            <div id="card-details-section" style="margin-bottom:1.5rem; background:#f8f9fa; padding:1rem; border-radius:8px; border:1px solid var(--border-light);">
                <label class="form-label">Cardholder Name</label>
                <input type="text" id="card-name" class="form-control" style="margin-bottom:1rem;" placeholder="e.g. John Doe">
                
                <label class="form-label">Card Number</label>
                <div style="position:relative; margin-bottom:1rem;">
                    <i class="fa-brands fa-cc-visa" style="position:absolute; left:12px; top:12px; color:#1a1f71; font-size:1.2rem;"></i>
                    <input type="text" id="card-number" class="form-control" style="padding-left:40px;" placeholder="0000 0000 0000 0000" maxlength="19">
                </div>
                
                <div style="display:flex; gap:1rem;">
                    <div style="flex:1;">
                        <label class="form-label">Expiry (MM/YY)</label>
                        <input type="text" id="card-expiry" class="form-control" placeholder="MM/YY" maxlength="5">
                    </div>
                    <div style="flex:1;">
                        <label class="form-label">CVC</label>
                        <input type="password" id="card-cvc" class="form-control" placeholder="123" maxlength="3">
                    </div>
                </div>
            </div>

            <button id="btn-submit-payment" class="btn-checkout" onclick="submitPayment()">Confirm Payment</button>
        </div>
    </div>

    <div id="toast-container" style="position:fixed; bottom:20px; right:20px; z-index:9999;"></div>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script>
        function openModal(id) { document.getElementById(id).classList.add('active'); }
        function closeModal(id) { document.getElementById(id).classList.remove('active'); }
    </script>
    <script>
        window.APP_CONTEXT_PATH = '${contextPath}';
        <c:if test="${not empty foodUser}">
        window.SERVER_FOOD_USER = {
            id: ${foodUser.id},
            userId: ${foodUser.id},
            fullName: '<c:out value="${foodUser.fullName}" />',
            email: '<c:out value="${foodUser.email}" />',
            role: '<c:out value="${foodUser.role.name()}" />'
        };
        </c:if>
    </script>
    <script src="${contextPath}/foodgrocery/js/api.js?v=2"></script>
    <script src="${contextPath}/foodgrocery/js/customer.js?v=4"></script>
</body>
</html>
