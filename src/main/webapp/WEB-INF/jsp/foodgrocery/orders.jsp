<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Orders Hub — NEXT GO</title>
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 & FontAwesome -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <!-- Scoped Admin Stylesheet -->
    <link href="${contextPath}/foodgrocery/admin/css/admin-dashboard.css" rel="stylesheet">
    <!-- My Module CSS -->
    <link rel="stylesheet" href="${contextPath}/foodgrocery/css/style.css?v=3">
</head>
<body class="admin-body">

<div class="admin-dashboard-app">

    <!-- SIDEBAR (Dark Navy) -->
    <jsp:include page="sidebar.jsp" />

    <!-- MAIN CONTENT AREA -->
    <main class="admin-main-content">
        <!-- Top Bar -->
        <header class="admin-topbar">
            <div class="admin-topbar-left">
                <button type="button" class="admin-mobile-toggle" id="adminMobileToggle">
                    <i class="fa-solid fa-bars"></i>
                </button>
                <div>
                    <h1 class="admin-page-title">Customer Orders Dashboard</h1>
                </div>
            </div>
        </header>

        <!-- Main Dashboard Container -->
        <div class="admin-container">
            <div class="stats-grid" style="display:grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem;">
                <div class="stat-card bg-gradient-blue" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-primary); box-shadow:0 0 20px rgba(58, 76, 240, 0.4); text-align:center;">
                    <h3 class="text-blue" style="color:var(--clr-primary-light); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Total Orders</h3>
                    <div class="value" id="stat-total" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card bg-gradient-yellow" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-warning); box-shadow:0 0 20px rgba(255, 193, 7, 0.4); text-align:center;">
                    <h3 class="text-yellow" style="color:var(--clr-warning); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Pending</h3>
                    <div class="value" id="stat-pending" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card bg-gradient-cyan" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-info); box-shadow:0 0 20px rgba(13, 202, 240, 0.4); text-align:center;">
                    <h3 class="text-cyan" style="color:var(--clr-info); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Out for Delivery</h3>
                    <div class="value" id="stat-delivery" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card bg-gradient-green" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-success); box-shadow:0 0 20px rgba(32, 201, 151, 0.4); text-align:center;">
                    <h3 class="text-green" style="color:var(--clr-success); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Completed</h3>
                    <div class="value" id="stat-completed" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
            </div>

            <div class="table-container border-green" style="background:var(--bg-card); border-radius:12px; padding:2rem; border:2px solid var(--clr-success); box-shadow:0 0 20px rgba(32, 201, 151, 0.4);">
                <div class="section-header" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem;">
                    <h3 class="text-green" style="color:var(--clr-success); margin:0;">🧾 Order Management</h3>
                </div>
                
                <div style="display:flex; gap:0.5rem; margin-bottom:1rem; align-items:center; flex-wrap:wrap;">
                    <button class="btn btn-outline-light filter-chip active" data-filter="ALL">All Orders</button>
                    <button class="btn btn-outline-light filter-chip" data-filter="PENDING">Pending</button>
                    <button class="btn btn-outline-light filter-chip" data-filter="PREPARING">Preparing</button>
                    <button class="btn btn-outline-light filter-chip" data-filter="OUT_FOR_DELIVERY">Delivering</button>
                    <button class="btn btn-outline-light filter-chip" data-filter="DELIVERED">Completed</button>
                    <div style="flex-grow:1;"></div>
                    <input type="text" id="search-input" class="form-control bg-dark text-light border-secondary" style="width:250px;" placeholder="Search Order ID...">
                </div>

                <div id="orders-table-container">
                    <p class="text-muted">Loading orders...</p>
                </div>
            </div>
        </div>
    </main>
</div>

<!-- View Order Modal -->
<div class="modal-overlay" id="order-modal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.8); z-index:1050; align-items:center; justify-content:center;">
    <div class="modal-content p-4" style="background:var(--bg-dark); border-radius:12px; border:1px solid var(--border-color); width:100%; max-width:700px; max-height:90vh; overflow-y:auto;">
        <div class="d-flex justify-content-between mb-4">
            <h3 class="text-light m-0">Order Details</h3>
            <button class="btn-close btn-close-white" onclick="closeModal('order-modal')"></button>
        </div>
        <div id="order-modal-body"></div>
    </div>
</div>

<!-- Assign Delivery Modal -->
<div class="modal-overlay" id="assign-modal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.8); z-index:1060; align-items:center; justify-content:center;">
    <div class="modal-content p-4" style="background:var(--bg-dark); border-radius:12px; border:1px solid var(--border-color); width:100%; max-width:500px;">
        <div class="d-flex justify-content-between mb-4">
            <h3 class="text-light m-0">Assign Delivery Partner</h3>
            <button class="btn-close btn-close-white" onclick="closeModal('assign-modal')"></button>
        </div>
        <form id="assign-form">
            <div class="mb-4">
                <label class="form-label text-light">Select Partner</label>
                <select class="form-select bg-dark text-light border-secondary" id="assign-partner-select" required>
                    <option value="">-- Loading partners... --</option>
                </select>
            </div>
            <div class="d-flex gap-2">
                <button type="button" class="btn btn-outline-light w-50" onclick="closeModal('assign-modal')">Cancel</button>
                <button type="button" class="btn btn-primary w-50" id="btn-confirm-assign">Confirm Assignment</button>
            </div>
        </form>
    </div>
</div>

<div id="toast-container" style="position:fixed; bottom:20px; right:20px; z-index:1100;"></div>
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
<script src="${contextPath}/foodgrocery/admin/js/admin-dashboard.js"></script>
<script src="${contextPath}/foodgrocery/js/api.js"></script>
<script src="${contextPath}/foodgrocery/js/orders.js"></script>
</body>
</html>
