<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Stock Management - NEXT GO</title>
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
                    <h1 class="admin-page-title" id="page-title">Stock Management</h1>
                </div>
            </div>
        </header>

        <div class="admin-container">
            <!-- Stats -->
            <div class="stats-grid" style="display:grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem;">
                <div class="stat-card" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-primary); box-shadow:0 0 20px rgba(58, 76, 240, 0.4); text-align:center;">
                    <h3 style="color:#93c5fd; margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Total Items</h3>
                    <div class="value" id="stat-total" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-warning); box-shadow:0 0 20px rgba(255, 193, 7, 0.4); text-align:center;">
                    <h3 style="color:#fcd34d; margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Low Stock</h3>
                    <div class="value" id="stat-low" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-success); box-shadow:0 0 20px rgba(32, 201, 151, 0.4); text-align:center;">
                    <h3 style="color:#6ee7b7; margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">In Stock</h3>
                    <div class="value" id="stat-ok" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-info); box-shadow:0 0 20px rgba(13, 202, 240, 0.4); text-align:center;">
                    <h3 style="color:#67e8f9; margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Shops</h3>
                    <div class="value" id="stat-shops" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
            </div>

            <!-- Low Stock Alert -->
            <div id="low-stock-alert" class="alert alert-warning" style="display:none; background:rgba(255,193,7,0.1); border:1px solid var(--clr-warning); color:#fcd34d; border-radius:12px;">
                <strong><i class="fa-solid fa-triangle-exclamation"></i> Action Required:</strong> 
                <span id="low-stock-count">0</span> items are running low on stock! 
                <div id="low-stock-list" style="margin-top:8px; font-size:0.85rem;"></div>
            </div>

            <div class="table-container" style="background:var(--bg-card); border-radius:12px; padding:2rem; border:2px solid var(--clr-primary); box-shadow:0 0 20px rgba(58, 76, 240, 0.4);">
                <div class="section-header" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem;">
                    <h3 style="color:#93c5fd; margin:0; letter-spacing:1px;">📦 Inventory Directory</h3>
                    <div>
                        <button class="btn btn-warning" id="btn-low-stock">View Low Stock</button>
                        <button class="btn btn-primary" id="btn-add-stock">+ Add Stock Entry</button>
                    </div>
                </div>

                <div style="display:flex; gap:0.5rem; margin-bottom:1rem; align-items:center;">
                    <button class="btn btn-outline-light filter-chip active" data-filter="ALL">All Items</button>
                    <button class="btn btn-outline-light filter-chip" data-filter="LOW">Low Stock</button>
                    <button class="btn btn-outline-light filter-chip" data-filter="EMPTY">Empty</button>
                    <select id="filter-shop" class="form-select" style="width:200px; background: rgba(0,0,0,0.4); border: 1px solid rgba(255,255,255,0.2); color: white;">
                        <option value="">All Shops</option>
                    </select>
                    <div style="flex-grow:1;"></div>
                    <input type="text" id="search-input" class="form-control" style="width:250px; background: rgba(0,0,0,0.4); border: 1px solid rgba(255,255,255,0.2); color: white;" placeholder="Search stock...">
                </div>

                <div id="stock-table-container">
                    <p class="text-light">Loading stock...</p>
                </div>
            </div>
        </div>
    </main>
</div>

<!-- Stock Modal -->
<div class="modal-overlay" id="stock-modal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.8); z-index:1050; align-items:center; justify-content:center;">
    <div class="modal-content p-4" style="background:var(--bg-dark); border-radius:12px; border:1px solid var(--border-color); width:100%; max-width:600px; max-height:90vh; overflow-y:auto;">
        <div class="d-flex justify-content-between mb-4">
            <h3 id="modal-title" class="text-light m-0">Add Stock</h3>
            <button class="btn-close btn-close-white" onclick="closeModal('stock-modal')"></button>
        </div>
        <form id="stock-form">
            <input type="hidden" id="stock-id">
            <div class="row mb-3">
                <div class="col-6">
                    <label class="form-label text-light">Product *</label>
                    <select class="form-select bg-dark text-light border-secondary" id="stock-product" required>
                        <option value="">Select Product...</option>
                    </select>
                </div>
                <div class="col-6">
                    <label class="form-label text-light">Shop</label>
                    <select class="form-select bg-dark text-light border-secondary" id="stock-shop">
                        <option value="">General/Central</option>
                    </select>
                </div>
            </div>
            <div class="row mb-3">
                <div class="col-6">
                    <label class="form-label text-light">Quantity *</label>
                    <input type="number" class="form-control bg-dark text-light border-secondary" id="stock-quantity" required min="0">
                </div>
                <div class="col-6">
                    <label class="form-label text-light">Min Stock Level *</label>
                    <input type="number" class="form-control bg-dark text-light border-secondary" id="stock-min" required min="1">
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label text-light">Unit (e.g. kg, pcs, boxes)</label>
                <input type="text" class="form-control bg-dark text-light border-secondary" id="stock-unit">
            </div>
            <div class="mb-3">
                <label class="form-label text-light">Notes</label>
                <textarea class="form-control bg-dark text-light border-secondary" id="stock-notes" rows="2"></textarea>
            </div>
            <div class="d-flex gap-2 mt-4">
                <button type="button" class="btn btn-outline-light w-50" onclick="closeModal('stock-modal')">Cancel</button>
                <button type="button" class="btn btn-primary w-50" id="btn-save-stock">Save Stock</button>
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
<script src="${contextPath}/foodgrocery/js/api.js?v=2"></script>
<script src="${contextPath}/foodgrocery/js/stock.js?v=2"></script>
</body>
</html>
