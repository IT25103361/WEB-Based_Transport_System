<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Portal Login — NEXT GO Operations</title>
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <!-- Shared NextGo Theme -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nextgo-theme.css">
    <script src="${pageContext.request.contextPath}/js/nextgo-theme.js"></script>
    <style>
        body {
            background: linear-gradient(135deg, #0b1120 0%, #0f172a 50%, #1e293b 100%);
            min-height: 100vh;
            font-family: 'Inter', sans-serif;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            color: #f8fafc;
        }
        .admin-login-card {
            background: rgba(15, 23, 42, 0.85);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 24px;
            box-shadow: 0 25px 60px rgba(0, 0, 0, 0.4);
            width: 100%;
            max-width: 440px;
            overflow: hidden;
        }
        .admin-login-header {
            padding: 36px 36px 20px;
            text-align: center;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
        }
        .admin-login-icon {
            width: 60px;
            height: 60px;
            background: linear-gradient(135deg, #06b6d4, #0284c7);
            border-radius: 16px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.6rem;
            margin-bottom: 16px;
            box-shadow: 0 8px 24px rgba(6, 182, 212, 0.35);
        }
        .admin-login-body {
            padding: 32px 36px 36px;
        }
        .form-label {
            font-size: 0.82rem;
            font-weight: 600;
            color: #94a3b8;
            margin-bottom: 6px;
        }
        .form-control {
            background: rgba(30, 41, 59, 0.7);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 12px;
            padding: 12px 16px;
            color: #ffffff;
            font-size: 0.95rem;
            transition: all 0.2s;
        }
        .form-control:focus {
            background: rgba(30, 41, 59, 0.95);
            border-color: #06b6d4;
            color: #ffffff;
            box-shadow: 0 0 0 4px rgba(6, 182, 212, 0.2);
            outline: none;
        }
        .btn-admin-submit {
            background: linear-gradient(135deg, #06b6d4, #0284c7);
            border: none;
            border-radius: 12px;
            padding: 14px;
            font-weight: 600;
            color: #ffffff;
            font-size: 0.95rem;
            transition: all 0.3s;
            box-shadow: 0 4px 16px rgba(6, 182, 212, 0.3);
            width: 100%;
        }
        .btn-admin-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba(6, 182, 212, 0.4);
            color: #ffffff;
        }
        .admin-notice {
            background: rgba(6, 182, 212, 0.1);
            border: 1px solid rgba(6, 182, 212, 0.2);
            border-radius: 12px;
            padding: 12px;
            font-size: 0.76rem;
            color: #bae6fd;
            margin-top: 20px;
            line-height: 1.4;
        }
    </style>
</head>
<body>
<button type="button" class="nextgo-theme-float" data-nextgo-theme-toggle title="Toggle light/dark theme" aria-label="Toggle light/dark theme">🌙</button>

<div class="admin-login-card">
    <div class="admin-login-header">
        <div class="admin-login-icon">
            <i class="fa-solid fa-shield-halved"></i>
        </div>
        <h4 class="fw-bold mb-1 text-white">Operations Portal</h4>
        <p class="text-white-50 small mb-0">NEXT GO Management & Oversight System</p>
    </div>

    <div class="admin-login-body">
        <c:if test="${not empty errorMsg}">
            <div class="alert alert-danger py-2 px-3 small rounded-3 mb-3 d-flex align-items-center gap-2">
                <i class="fa-solid fa-circle-exclamation"></i>
                <div><c:out value="${errorMsg}"/></div>
            </div>
        </c:if>

        <c:if test="${param.error == 'auth_required'}">
            <div class="alert alert-warning py-2 px-3 small rounded-3 mb-3 d-flex align-items-center gap-2 text-dark">
                <i class="fa-solid fa-lock"></i>
                <div>Administrative privileges required to access the dashboard.</div>
            </div>
        </c:if>

        <c:if test="${param.logout == 'success'}">
            <div class="alert alert-info py-2 px-3 small rounded-3 mb-3 d-flex align-items-center gap-2 text-dark">
                <i class="fa-solid fa-circle-check"></i>
                <div>You have been safely signed out of the Operations Portal.</div>
            </div>
        </c:if>

        <form action="/admin/login" method="post">
            <div class="mb-3">
                <label class="form-label" for="adminEmail">Administrator Email</label>
                <div class="input-group">
                    <span class="input-group-text bg-transparent border-end-0 text-white-50"><i class="fa-solid fa-envelope"></i></span>
                    <input type="email" id="adminEmail" name="email" class="form-control border-start-0 ps-0" placeholder="admin@nextgo.com" required autofocus>
                </div>
            </div>

            <div class="mb-4">
                <label class="form-label" for="adminPassword">Security Password</label>
                <div class="input-group">
                    <span class="input-group-text bg-transparent border-end-0 text-white-50"><i class="fa-solid fa-key"></i></span>
                    <input type="password" id="adminPassword" name="password" class="form-control border-start-0 ps-0" placeholder="••••••••" required>
                </div>
            </div>

            <button type="submit" class="btn btn-admin-submit">
                <i class="fa-solid fa-right-to-bracket me-2"></i> Access Operations Dashboard
            </button>
        </form>

        <div class="admin-notice">
            <i class="fa-solid fa-circle-info me-1 text-info"></i>
            <strong>Security Notice:</strong> Standard passenger and driver accounts do not possess administrative access. If you are an authorized staff member, ensure your account has been designated with the <code>ADMIN</code> role.
        </div>

        <div class="text-center mt-4">
            <a href="/ride/home" class="text-white-50 text-decoration-none small hover-light">
                <i class="fa-solid fa-arrow-left me-1"></i> Return to NEXT GO Main Hub
            </a>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
