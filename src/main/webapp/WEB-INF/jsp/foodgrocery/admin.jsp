<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Operations Overview Dashboard — NEXT GO</title>
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 & FontAwesome -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <link href="${contextPath}/foodgrocery/admin/css/admin-dashboard.css?v=3" rel="stylesheet">
    <!-- Global Styles -->
    <link href="${contextPath}/foodgrocery/css/style.css?v=3" rel="stylesheet">
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
</head>
<body class="admin-body">

<div class="admin-dashboard-app">

    <!-- ==========================================
         SIDEBAR (Dark Navy)
         ========================================== -->
    <jsp:include page="sidebar.jsp" />

    <!-- ==========================================
         MAIN CONTENT AREA
         ========================================== -->
    <main class="admin-main-content">

        <!-- Top Bar -->
        <header class="admin-topbar">
            <div class="admin-topbar-left">
                <button type="button" class="admin-mobile-toggle" id="adminMobileToggle" aria-label="Toggle navigation">
                    <i class="fa-solid fa-bars"></i>
                </button>
                <div>
                    <h1 class="admin-page-title">Operations Overview</h1>
                    <div class="d-flex align-items-center gap-2 mt-1">
                        <span class="admin-timezone-badge">
                            <i class="fa-solid fa-clock text-info"></i>
                            <span>Timezone: <strong>Asia/Colombo (UTC+05:30)</strong></span>
                        </span>
                        <span class="text-muted small d-none d-md-inline" style="color:white !important;">
                            Last refreshed: <strong id="lastRefreshedTime">Just now</strong>
                        </span>
                    </div>
                </div>
            </div>
        </header>

        <!-- Main Dashboard Container -->
        <div class="admin-container">

            <!-- Overview Metric Cards -->
            <div class="row g-4 mb-4">
                <div class="col-sm-6 col-xl-3">
                    <div class="admin-kpi-card bg-gradient-blue border-primary" style="background: rgba(15, 23, 42, 0.7); border: 1px solid #3b82f6;">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-label text-primary">Total Food Shops</span>
                        </div>
                        <div class="admin-kpi-val text-white" id="stat-shops">0</div>
                    </div>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <div class="admin-kpi-card bg-gradient-yellow border-warning" style="background: rgba(15, 23, 42, 0.7); border: 1px solid #ffc107;">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-label text-warning">Customers</span>
                        </div>
                        <div class="admin-kpi-val text-white" id="stat-customers">0</div>
                    </div>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <div class="admin-kpi-card bg-gradient-cyan border-info" style="background: rgba(15, 23, 42, 0.7); border: 1px solid #0dcaf0;">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-label text-info">Delivery Partners</span>
                        </div>
                        <div class="admin-kpi-val text-white" id="stat-partners">0</div>
                    </div>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <div class="admin-kpi-card bg-gradient-green border-success" style="background: rgba(15, 23, 42, 0.7); border: 1px solid #198754;">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-label text-success">Total Orders</span>
                        </div>
                        <div class="admin-kpi-val text-white" id="stat-orders">0</div>
                    </div>
                </div>
            </div>

            <!-- Manage Users Table -->
            <div class="table-container" style="background:rgba(15, 23, 42, 0.85); backdrop-filter:blur(10px); border-radius:12px; padding:2rem; border:2px solid var(--clr-primary); box-shadow:0 0 20px rgba(58, 76, 240, 0.4);">
                <div class="section-header" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem;">
                    <h3 class="text-blue" style="color:var(--clr-primary-light); margin:0;">👥 System Users Directory</h3>
                    <button class="btn btn-primary" id="btn-add-user">+ Create User</button>
                </div>
                <div class="d-flex gap-2 mb-3">
                    <button class="btn btn-outline-light btn-sm filter-chip active" data-role="ALL">All Users</button>
                    <button class="btn btn-outline-light btn-sm filter-chip" data-role="CUSTOMER">Customers</button>
                    <button class="btn btn-outline-light btn-sm filter-chip" data-role="VENDOR_MANAGER">Vendor Managers</button>
                    <button class="btn btn-outline-light btn-sm filter-chip" data-role="DELIVERY_PARTNER">Delivery Partners</button>
                    <input type="text" id="search-input" class="form-control form-control-sm bg-dark text-light border-secondary ms-auto" style="width:250px" placeholder="Search...">
                </div>
                <div id="users-table-container">
                    <p class="text-muted">Loading...</p>
                </div>
            </div>

        </div>
    </main>
</div>

<!-- Add User Modal -->
<div class="modal" id="user-modal" tabindex="-1" style="display:none; background:rgba(0,0,0,0.8); position:fixed; inset:0; z-index:1050;">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content bg-dark text-light border-secondary">
      <div class="modal-header border-secondary">
        <h5 class="modal-title">Create New User</h5>
        <button type="button" class="btn-close btn-close-white" onclick="closeModal('user-modal')"></button>
      </div>
      <div class="modal-body">
        <form id="user-form">
          <input type="hidden" id="user-id">
          <div class="mb-3">
            <label>Full Name *</label>
            <input type="text" class="form-control bg-dark text-light border-secondary" id="user-name" required>
          </div>
          <div class="mb-3">
            <label>Email / Username *</label>
            <input type="text" class="form-control bg-dark text-light border-secondary" id="user-email" required>
          </div>
          <div class="mb-3">
            <label>Role *</label>
            <select class="form-select bg-dark text-light border-secondary" id="user-role" required>
                <option value="CUSTOMER">Customer</option>
                <option value="VENDOR_MANAGER">Vendor Manager</option>
                <option value="DELIVERY_PARTNER">Delivery Partner</option>
                <option value="ADMIN">System Admin</option>
            </select>
          </div>
          <div class="mb-3"><label>Phone *</label><input type="text" class="form-control bg-dark text-light border-secondary" id="user-phone" required></div>
          <div class="mb-3"><label>Gender</label><select class="form-select bg-dark text-light border-secondary" id="user-gender"><option value="MALE">Male</option><option value="FEMALE">Female</option></select></div>
          <div class="mb-3"><label>Address</label><input type="text" class="form-control bg-dark text-light border-secondary" id="user-address"></div>
          <div class="mb-3"><label>Postal Code *</label><input type="text" class="form-control bg-dark text-light border-secondary" id="user-postal-code" required></div>
          <div class="mb-3"><label>Password *</label>
            <input type="password" class="form-control bg-dark text-light border-secondary" id="user-password" required>
          </div>
        </form>
      </div>
      <div class="modal-footer border-secondary">
        <button type="button" class="btn btn-secondary" onclick="closeModal('user-modal')">Cancel</button>
        <button type="button" class="btn btn-primary" id="btn-save-user">Create User</button>
      </div>
    </div>
  </div>
</div>

<div id="toast-container" style="position:fixed; bottom:20px; right:20px; z-index:1100;"></div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
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
<script src="${contextPath}/foodgrocery/js/api.js?v=4"></script>
<script src="${contextPath}/foodgrocery/js/admin.js?v=3"></script>
<script>
    function closeModal(id) {
        document.getElementById(id).style.display = 'none';
    }
    document.getElementById('btn-add-user').addEventListener('click', () => {
        document.getElementById('user-form').reset();
        document.getElementById('user-id').value = '';
        document.getElementById('user-modal').style.display = 'block';
    });
</script>
</body>
</html>

