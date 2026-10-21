<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vendor Manager - NEXT GO</title>
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
                    <h1 class="admin-page-title" id="page-title">Management Dashboard</h1>
                </div>
            </div>
        </header>

        <div class="admin-container">
            <div class="data-card border-blue" id="main-content" style="background:rgba(15, 23, 42, 0.85); backdrop-filter:blur(10px); padding:2rem; border-radius:12px; border:2px solid var(--clr-primary); box-shadow:0 0 20px rgba(58, 76, 240, 0.4);">
                <h3 class="text-white mb-3">Service Dashboard</h3>
                <p class="text-muted">Loading module data...</p>
            </div>
        </div>
    </main>
</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
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
<script src="${contextPath}/foodgrocery/js/vendor_manager.js"></script>
<script>
    document.getElementById('page-title').innerText = window.location.pathname.substring(1).toUpperCase().replace(/_/g, ' ') + ' Management';
</script>
</body>
</html>
