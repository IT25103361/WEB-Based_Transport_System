<%@ page contentType=text/html;charset=UTF-8 language=java %>
<%@ taglib prefix=c uri=jakarta.tags.core %>
<c:set var=contextPath value=" />
<!DOCTYPE html>
<html lang=en>
<head>
 <meta charset=UTF-8>
 <meta name=viewport content=width=device-width, initial-scale=1.0>
 <title>Checkout — NEXT GO Food & Grocery</title>
 <link rel=stylesheet href=/foodgrocery/css/style.css?v=3>
 <link href=https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap rel=stylesheet>
 <style>
 .page-wrapper { padding: 2rem; max-width: 1200px; margin: 0 auto; }
 .data-card { background: var(--bg-card); padding: 2rem; border-radius: 12px; border: 1px solid var(--border-color); }
 </style>
</head>
<body>
 <nav class=navbar>
 <div class=nav-brand>NEXT GO — <span class=text-blue>Food Checkout</span></div>
 <div class=nav-actions>
 <a href=/ride/home class=btn btn-outline border-blue text-blue>Hub</a>
 <a href=javascript:void(0) onclick=logout() class=btn btn-outline border-red text-red>Logout</a>
 </div>
 </nav>
 <div class=page-wrapper>
 <div class=section-header>
 <h2 id=page-title>Order Checkout</h2>
 </div>
 <div class=data-card border-blue id=main-content>
 <div id=summaryItems class=mb-3></div>
 <div class=d-flex justify-content-between mb-2>
 <span>Subtotal:</span>
 <span id=sum-subtotal>Rs. 0.00</span>
 </div>
 <div class=d-flex justify-content-between mb-2>
 <span>Delivery:</span>
 <span id=sum-delivery>Rs. 0.00</span>
 </div>
 <div class=d-flex justify-content-between mb-3 fw-bold>
 <span>Total:</span>
 <span id=sum-total>Rs. 0.00</span>
 </div>
 <div class=mb-3>
 <label class=form-label text-light>Delivery Address</label>
 <input type=text id=chk-address class=form-control bg-dark text-light border-secondary placeholder=Enter delivery address />
 </div>
 <div class=mb-3>
 <label class=form-label text-light>Special Instructions</label>
 <input type=text id=chk-instructions class=form-control bg-dark text-light border-secondary placeholder=Optional notes />
 </div>
 <button id=btn-place-order class=btn btn-primary w-100>Place Order</button>
 </div>
 </div>
 <div id=toast-container style=position:fixed; bottom:20px; right:20px; z-index:1100;></div>
 <script>
 window.APP_CONTEXT_PATH = '';
 <c:if test=>
 window.SERVER_FOOD_USER = {
 id: ,
 userId: ,
 fullName: '<c:out value= />',
 email: '<c:out value= />',
 role: '<c:out value= />'
 };
 </c:if>
 </script>
 <script src=/foodgrocery/js/api.js></script>
 <script src=/foodgrocery/js/checkout.js></script>
</body>
</html>
