<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Products Hub — NEXT GO</title>
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
                    <h1 class="admin-page-title">Manage Products Catalog</h1>
                </div>
            </div>
        </header>

        <!-- Main Dashboard Container -->
        <div class="admin-container">
            <div class="stats-grid" style="display:grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem;">
                <div class="stat-card bg-gradient-blue" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-primary); box-shadow:0 0 20px rgba(58, 76, 240, 0.4); text-align:center;">
                    <h3 class="text-blue" style="color:var(--clr-primary-light); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Total Products</h3>
                    <div class="value" id="stat-total" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card bg-gradient-green" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-success); box-shadow:0 0 20px rgba(32, 201, 151, 0.4); text-align:center;">
                    <h3 class="text-green" style="color:var(--clr-success); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Available</h3>
                    <div class="value" id="stat-available" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card bg-gradient-yellow" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-warning); box-shadow:0 0 20px rgba(255, 193, 7, 0.4); text-align:center;">
                    <h3 class="text-yellow" style="color:var(--clr-warning); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Popular</h3>
                    <div class="value" id="stat-popular" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
                <div class="stat-card bg-gradient-red" style="background:var(--bg-card); padding:1.5rem; border-radius:12px; border:2px solid var(--clr-danger); box-shadow:0 0 20px rgba(252, 75, 108, 0.4); text-align:center;">
                    <h3 class="text-red" style="color:var(--clr-danger); margin-bottom:0.5rem; font-size:0.9rem; text-transform:uppercase;">Out of Stock</h3>
                    <div class="value" id="stat-unavailable" style="font-size:2.5rem; font-weight:bold; color:white;">—</div>
                </div>
            </div>

            <div class="table-container border-cyan" style="background:var(--bg-card); border-radius:12px; padding:2rem; border:2px solid var(--clr-info); box-shadow:0 0 20px rgba(13, 202, 240, 0.4);">
                <div class="section-header" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem;">
                    <h3 class="text-cyan" style="color:var(--clr-info); margin:0;">📦 Inventory Catalog</h3>
                    <button class="btn btn-primary" id="btn-add-product">+ Add Product</button>
                </div>
                
                <div style="display:flex; gap:0.5rem; margin-bottom:1rem; align-items:center; flex-wrap:wrap;">
                    <button class="btn btn-outline-light filter-chip active" data-filter="ALL">All Items</button>
                    <button class="btn btn-outline-light filter-chip" data-filter="AVAILABLE">Available</button>
                    <button class="btn btn-outline-light filter-chip" data-filter="UNAVAILABLE">Unavailable</button>
                    <div style="flex-grow:1;"></div>
                    <select id="filter-category" class="form-select bg-dark text-light border-secondary" style="width:auto;"><option value="">All Categories</option></select>
                    <select id="view-toggle" class="form-select bg-dark text-light border-secondary" style="width:auto;"><option value="table">Table</option><option value="grid">Grid</option></select>
                    <input type="text" id="search-input" class="form-control bg-dark text-light border-secondary" style="width:250px;" placeholder="Search products...">
                </div>

                <div id="products-container">
                    <p class="text-muted">Loading products...</p>
                </div>
            </div>
        </div>
    </main>
</div>

<!-- Product Modal -->
<div class="modal-overlay" id="product-modal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.8); z-index:1050; align-items:center; justify-content:center;">
    <div class="modal-content p-4" style="background:var(--bg-dark); border-radius:12px; border:1px solid var(--border-color); width:100%; max-width:600px; max-height:90vh; overflow-y:auto;">
        <div class="d-flex justify-content-between mb-4">
            <h3 id="modal-title" class="text-light m-0">Add Product</h3>
            <button class="btn-close btn-close-white" onclick="closeModal('product-modal')"></button>
        </div>
        <form id="product-form">
            <input type="hidden" id="product-id">
            <div class="row mb-3">
                <div class="col-6">
                    <label class="form-label text-light">Product Name *</label>
                    <input type="text" class="form-control bg-dark text-light border-secondary" id="product-name" required>
                </div>
                <div class="col-6">
                    <label class="form-label text-light">Category *</label>
                    <input type="text" class="form-control bg-dark text-light border-secondary" id="product-category" required>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label text-light">Description</label>
                <textarea class="form-control bg-dark text-light border-secondary" id="product-desc" rows="2"></textarea>
            </div>
            <div class="row mb-3">
                <div class="col-6">
                    <label class="form-label text-light">Price (LKR) *</label>
                    <input type="number" class="form-control bg-dark text-light border-secondary" id="product-price" required min="0" step="0.01">
                </div>
                <div class="col-6">
                    <label class="form-label text-light">Stock *</label>
                    <input type="number" class="form-control bg-dark text-light border-secondary" id="product-stock" required min="0">
                </div>
            </div>
            <div class="row mb-3">
                <div class="col-12">
                    <label class="form-label text-light">Shop *</label>
                    <select class="form-select bg-dark text-light border-secondary" id="product-shop" required><option value="">-- Select Shop --</option></select>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label text-light">Image URL or Upload</label>
                <div class="input-group">
                    <input type="text" class="form-control bg-dark text-light border-secondary" id="product-image" placeholder="https://...">
                    <label class="btn btn-secondary mb-0" for="product-image-file" style="cursor:pointer; display:flex; align-items:center;">📂</label>
                    <input type="file" id="product-image-file" style="display:none" accept="image/*" onchange="uploadImageFile(this, 'product-image')">
                </div>
            </div>
            <div class="d-flex gap-3 mb-4">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="product-available" checked>
                    <label class="form-check-label text-light">Available</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="product-popular">
                    <label class="form-check-label text-light">Popular</label>
                </div>
            </div>
            <div class="d-flex gap-2">
                <button type="button" class="btn btn-outline-light w-50" onclick="closeModal('product-modal')">Cancel</button>
                <button type="button" class="btn btn-primary w-50" id="btn-save-product">Save Product</button>
            </div>
        </form>
    </div>
</div>

<!-- View Product Modal -->
<div class="modal-overlay" id="view-modal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.8); z-index:1050; align-items:center; justify-content:center;">
    <div class="modal-content p-4" style="background:var(--bg-dark); border-radius:12px; border:1px solid var(--border-color); width:100%; max-width:500px;">
        <div class="d-flex justify-content-between mb-3">
            <h3 class="text-light m-0">Product Details</h3>
            <button class="btn-close btn-close-white" onclick="closeModal('view-modal')"></button>
        </div>
        <div class="text-center mb-4">
            <img id="view-image" src="" alt="Product Image" style="width:100%; max-height:250px; object-fit:cover; border-radius:12px; display:none;">
            <div id="view-image-placeholder" style="width:100%; height:250px; background:var(--clr-surface-3); border-radius:12px; display:flex; align-items:center; justify-content:center; font-size:1.2rem; color:#ccc;">No Image Available</div>
        </div>
        <h4 id="view-name" class="text-light mb-1"></h4>
        <div class="d-flex gap-2 mb-3">
            <span id="view-category" class="tag"></span>
            <span id="view-price" class="tag" style="color:var(--clr-primary); font-weight:bold;"></span>
        </div>
        <p id="view-desc" style="color: #ccc; font-size:0.9rem; line-height:1.5;"></p>
        <hr style="border-color:var(--border-color);">
        <div class="d-flex justify-content-between mb-2">
            <span style="color: #ccc;">Stock:</span>
            <span id="view-stock" class="text-light fw-bold"></span>
        </div>
        <div class="d-flex justify-content-between mb-2">
            <span style="color: #ccc;">Shop:</span>
            <span id="view-shop" class="text-light fw-bold"></span>
        </div>
        <div class="d-flex justify-content-between">
            <span style="color: #ccc;">Status:</span>
            <span id="view-status"></span>
        </div>
        <div class="mt-4">
            <button class="btn btn-outline-light w-100" onclick="closeModal('view-modal')">Close</button>
        </div>
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
<script src="${contextPath}/foodgrocery/js/api.js?v=10"></script>
<script src="${contextPath}/foodgrocery/js/products.js?v=10"></script>
</body>
</html>
