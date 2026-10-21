<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<aside class="admin-sidebar" aria-label="Admin Navigation">
    <div class="admin-sidebar-header">
        <a href="${contextPath}/food/admin" class="admin-brand">
            <div class="admin-brand-icon">
                <i class="fa-solid fa-chart-line"></i>
            </div>
            <div>
                <div class="admin-brand-text">NEXT GO</div>
                <span class="admin-brand-badge">Food Module</span>
            </div>
        </a>
    </div>

    <nav class="admin-sidebar-menu" id="main-sidebar-menu">
        <div class="admin-menu-header" data-roles="ADMIN">Overview</div>
        <a href="${contextPath}/food/admin" class="admin-nav-item" data-path="/admin" data-roles="ADMIN">
            <i class="fa-solid fa-table-columns"></i>
            <span>All Modules Overview</span>
        </a>

        <div class="admin-menu-header">Food & Grocery Submodules</div>
        <a href="${contextPath}/food/shops" class="admin-nav-item" data-path="/shops" data-roles="ADMIN,VENDOR_MANAGER">
            <i class="fa-solid fa-store text-danger"></i>
            <span>Manage Shops</span>
        </a>
        <a href="${contextPath}/food/products" class="admin-nav-item" data-path="/products" data-roles="ADMIN,VENDOR_MANAGER">
            <i class="fa-solid fa-box-open text-warning"></i>
            <span>Manage Products</span>
        </a>
        <a href="${contextPath}/food/stock" class="admin-nav-item" data-path="/stock" data-roles="ADMIN,VENDOR_MANAGER">
            <i class="fa-solid fa-cubes text-info"></i>
            <span>Stock & Inventory</span>
        </a>
        <a href="${contextPath}/food/orders" class="admin-nav-item" data-path="/orders" data-roles="ADMIN,VENDOR_MANAGER">
            <i class="fa-solid fa-receipt text-success"></i>
            <span>View Orders</span>
        </a>
        <a href="${contextPath}/food/deliveries" class="admin-nav-item" data-path="/deliveries" data-roles="ADMIN,VENDOR_MANAGER,DELIVERY_PARTNER">
            <i class="fa-solid fa-motorcycle text-primary"></i>
            <span>Deliveries</span>
        </a>
    </nav>

    <div class="admin-sidebar-footer">
        <div class="admin-profile-pill">
            <div class="admin-avatar" id="sidebar-avatar">A</div>
            <div class="admin-profile-info">
                <div class="admin-profile-name" id="sidebar-name">Administrator</div>
                <div class="admin-profile-role" id="sidebar-role"><i class="fa-solid fa-user-shield me-1"></i>ADMINISTRATOR</div>
            </div>
        </div>
        <div class="d-flex gap-2">
            <a href="${contextPath}/ride/home" class="btn btn-outline-light btn-sm w-50 py-1" style="font-size: 0.76rem;" title="Go to Main Hub">
                <i class="fa-solid fa-house me-1"></i> Main Hub
            </a>
            <button onclick="logout()" class="btn btn-outline-danger btn-sm w-50 py-1" style="font-size: 0.76rem;" title="Sign out of Admin">
                <i class="fa-solid fa-right-from-bracket me-1"></i> Sign Out
            </button>
        </div>
    </div>
</aside>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        const path = window.location.pathname;
        const links = document.querySelectorAll('#main-sidebar-menu .admin-nav-item');
        links.forEach(link => {
            link.classList.remove('active');
            if (path.includes(link.getAttribute('data-path'))) {
                link.classList.add('active');
            }
        });
        
        // Ensure role-based items are hidden immediately
        try {
            const userStr = sessionStorage.getItem('fg_user') || localStorage.getItem('fg_user');
            if (userStr) {
                const user = JSON.parse(userStr);
                if (user.role && user.role !== 'ADMIN') {
                    document.querySelectorAll('[data-roles="ADMIN"]').forEach(el => {
                        el.style.display = 'none';
                        el.remove();
                    });
                }
            }
        } catch(e) { console.error(e); }
    });
</script>
