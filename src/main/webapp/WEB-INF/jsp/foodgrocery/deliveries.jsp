<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Deliveries - NEXT GO</title>
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
    <jsp:include page="sidebar.jsp" />

    <main class="admin-main-content">
        <header class="admin-topbar">
            <div class="admin-topbar-left">
                <button type="button" class="admin-mobile-toggle" id="adminMobileToggle">
                    <i class="fa-solid fa-bars"></i>
                </button>
                <div>
                    <h1 class="admin-page-title" id="page-title">Deliveries Management</h1>
                </div>
            </div>
            <div class="admin-topbar-right">
                <button class="btn btn-outline-light" id="btn-refresh"><i class="fa-solid fa-rotate-right"></i> Refresh</button>
            </div>
        </header>

        <div class="admin-container">
            <div class="stats-grid" style="display:grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem;">
                <div class="stat-card bg-gradient-blue" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-primary); box-shadow:0 0 20px rgba(58, 76, 240, 0.4); text-align:center;">
                    <h3 class="text-blue" style="color:var(--clr-primary-light); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Total Deliveries</h3>
                    <div class="value" id="stat-total" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card bg-gradient-yellow" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-warning); box-shadow:0 0 20px rgba(255, 193, 7, 0.4); text-align:center;">
                    <h3 class="text-yellow" style="color:var(--clr-warning); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Pending</h3>
                    <div class="value" id="stat-pending" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card bg-gradient-cyan" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-info); box-shadow:0 0 20px rgba(13, 202, 240, 0.4); text-align:center;">
                    <h3 class="text-cyan" style="color:var(--clr-info); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">In Transit</h3>
                    <div class="value" id="stat-transit" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card bg-gradient-green" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-success); box-shadow:0 0 20px rgba(32, 201, 151, 0.4); text-align:center;">
                    <h3 class="text-green" style="color:var(--clr-success); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Delivered</h3>
                    <div class="value" id="stat-delivered" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
            </div>

            <div class="table-container border-primary" style="background:var(--bg-card); border-radius:12px; padding:2rem; border:2px solid var(--clr-primary); box-shadow:0 0 20px rgba(58, 76, 240, 0.2);">
                <div class="section-header" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem;">
                    <h3 class="text-primary" style="color:var(--clr-primary); margin:0;">🚚 Active Deliveries</h3>
                </div>
                
                <div style="display:flex; gap:0.5rem; margin-bottom:1rem; align-items:center; flex-wrap:wrap;">
                    <button class="btn btn-outline-light filter-chip active" data-status="ALL">All</button>
                    <button class="btn btn-outline-light filter-chip" data-status="PENDING">Pending</button>
                    <button class="btn btn-outline-light filter-chip" data-status="ASSIGNED">Assigned</button>
                    <button class="btn btn-outline-light filter-chip" data-status="PICKED_UP">Picked Up</button>
                    <button class="btn btn-outline-light filter-chip" data-status="ON_THE_WAY">On the Way</button>
                    <button class="btn btn-outline-light filter-chip" data-status="DELIVERED">Delivered</button>
                    <div style="flex-grow:1;"></div>
                    <input type="text" id="search-input" class="form-control bg-dark text-light border-secondary" style="width:250px;" placeholder="Search...">
                </div>

                <div id="deliveries-table-container">
                    <p class="text-muted">Loading deliveries...</p>
                </div>
            </div>
        </div>
    </main>
</div>

<!-- Assign Partner Modal -->
<div class="modal-overlay" id="assign-modal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.8); z-index:1050; align-items:center; justify-content:center;">
    <div class="modal-content p-4" style="background:var(--bg-dark); border-radius:12px; border:1px solid var(--border-color); width:100%; max-width:500px;">
        <div class="d-flex justify-content-between mb-4">
            <h3 class="text-light m-0">Assign Delivery Partner</h3>
            <button class="btn-close btn-close-white" onclick="closeModal('assign-modal')"></button>
        </div>
        <form id="assign-form">
            <input type="hidden" id="assign-delivery-id">
            <div class="mb-4">
                <label class="form-label text-light">Select Partner</label>
                <select class="form-select bg-dark text-light border-secondary" id="assign-partner" required>
                    <option value="">-- Select Partner --</option>
                </select>
            </div>
            <div class="d-flex gap-2">
                <button type="button" class="btn btn-outline-light w-50" onclick="closeModal('assign-modal')">Cancel</button>
                <button type="button" class="btn btn-primary w-50" onclick="confirmAssign()">Confirm Assignment</button>
            </div>
        </form>
    </div>
</div>

<!-- Update Status Modal -->
<div class="modal-overlay" id="delivery-status-modal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.8); z-index:1060; align-items:center; justify-content:center;">
    <div class="modal-content p-4" style="background:var(--bg-dark); border-radius:12px; border:1px solid var(--border-color); width:100%; max-width:500px;">
        <div class="d-flex justify-content-between mb-4">
            <h3 class="text-light m-0">Update Delivery Status</h3>
            <button class="btn-close btn-close-white" onclick="closeModal('delivery-status-modal')"></button>
        </div>
        <form id="delivery-status-form">
            <input type="hidden" id="delivery-update-id">
            <div class="mb-4">
                <label class="form-label text-light">Status</label>
                <select class="form-select bg-dark text-light border-secondary" id="delivery-new-status" required>
                    <option value="PENDING">Pending</option>
                    <option value="ASSIGNED">Assigned</option>
                    <option value="PICKED_UP">Picked Up</option>
                    <option value="ON_THE_WAY">On the Way</option>
                    <option value="DELIVERED">Delivered</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>
            </div>
            <div class="mb-4">
                <label class="form-label text-light">Current Location (Optional)</label>
                <input type="text" class="form-control bg-dark text-light border-secondary" id="delivery-location" placeholder="e.g. Near Main Street">
            </div>
            <div class="d-flex gap-2">
                <button type="button" class="btn btn-outline-light w-50" onclick="closeModal('delivery-status-modal')">Cancel</button>
                <button type="button" class="btn btn-primary w-50" onclick="confirmDeliveryStatus()">Update Status</button>
            </div>
        </form>
    </div>
</div>

<!-- View Detail Modal -->
<div class="modal-overlay" id="delivery-view-modal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.8); z-index:1070; align-items:center; justify-content:center;">
    <div class="modal-content p-4" style="background:var(--bg-dark); border-radius:12px; border:1px solid var(--border-color); width:100%; max-width:600px;">
        <div class="d-flex justify-content-between mb-4">
            <h3 class="text-light m-0" id="delivery-view-title">Delivery Details</h3>
            <button class="btn-close btn-close-white" onclick="closeModal('delivery-view-modal')"></button>
        </div>
        <div id="delivery-view-body" class="text-light">
            <!-- Details will be injected here -->
        </div>
    </div>
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
<script src="${contextPath}/foodgrocery/js/deliveries.js"></script>
<script>
    document.getElementById('page-title').innerText = window.location.pathname.substring(1).toUpperCase() + ' Management';
</script>
</body>
</html>
