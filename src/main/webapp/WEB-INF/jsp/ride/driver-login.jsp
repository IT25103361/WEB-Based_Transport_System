<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Driver Login & Register — NEXT GO</title>
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- FontAwesome 6 -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <!-- NEXT GO Shared Theme -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nextgo-theme.css">
    <script src="${pageContext.request.contextPath}/js/nextgo-theme.js"></script>
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background: linear-gradient(rgba(6, 78, 59, 0.75), rgba(15, 23, 42, 0.85)),
                        url('https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?q=80&w=1920&auto=format&fit=crop') no-repeat center center fixed;
            background-size: cover;
            min-height: 100vh;
            color: #f8fafc;
        }
        .glass-navbar {
            background: rgba(6, 78, 59, 0.85) !important;
            backdrop-filter: blur(15px);
            border-bottom: 2px solid #10b981;
        }
        .auth-card {
            background: rgba(255, 255, 255, 0.96);
            backdrop-filter: blur(20px);
            border: 1px solid rgba(255, 255, 255, 0.4);
            border-radius: 24px;
            box-shadow: 0 25px 50px rgba(0, 0, 0, 0.4);
            overflow: hidden;
            color: #1e293b;
        }
        .auth-header {
            background: linear-gradient(135deg, #059669, #047857);
            padding: 30px;
            color: white;
            text-align: center;
        }
        .nav-tabs .nav-link {
            border: none;
            color: #64748b;
            font-weight: 600;
            padding: 14px 20px;
            border-bottom: 3px solid transparent;
            border-radius: 0;
            transition: all 0.2s ease;
        }
        .nav-tabs .nav-link.active {
            color: #059669;
            background: transparent;
            border-bottom: 3px solid #059669;
        }
        .form-control, .form-select {
            border-radius: 12px;
            padding: 12px 16px;
            border: 1px solid #cbd5e1;
            background-color: #f8fafc;
            font-size: 0.95rem;
            transition: all 0.2s;
        }
        .form-control:focus, .form-select:focus {
            background-color: #fff;
            border-color: #10b981;
            box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.15);
        }
        .input-group-text {
            border-radius: 12px 0 0 12px;
            border: 1px solid #cbd5e1;
            border-right: none;
            background-color: #f8fafc;
            color: #64748b;
        }
        .input-group .form-control, .input-group .form-select {
            border-left: none;
            border-radius: 0 12px 12px 0;
        }
        .btn-driver-primary {
            background: linear-gradient(135deg, #059669, #047857);
            border: none;
            border-radius: 12px;
            padding: 12px 20px;
            color: white;
            font-weight: 600;
            box-shadow: 0 4px 15px rgba(5, 150, 105, 0.35);
            transition: all 0.3s ease;
        }
        .btn-driver-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(5, 150, 105, 0.45);
            color: white;
        }
    </style>
</head>
<body>

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark glass-navbar shadow-sm sticky-top">
    <div class="container px-4">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/ride/home">
            <i class="fa-solid fa-id-badge text-warning me-2"></i> NEXT GO <span class="fw-light opacity-75">| Driver Portal</span>
        </a>
        <div class="d-flex align-items-center gap-2">
            <button type="button" class="nextgo-theme-toggle" data-nextgo-theme-toggle title="Toggle light/dark theme" aria-label="Toggle light/dark theme">🌙</button>
            <a href="${pageContext.request.contextPath}/ride/ride-hub" class="btn btn-outline-light btn-sm rounded-pill px-3">
                <i class="fa-solid fa-arrow-left me-1"></i> Portals
            </a>
            <a href="${pageContext.request.contextPath}/ride/home" class="btn btn-outline-light btn-sm rounded-pill px-3">
                <i class="fa-solid fa-house me-1"></i> Hub
            </a>
        </div>
    </div>
</nav>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="card auth-card">
                <div class="auth-header">
                    <div class="bg-white bg-opacity-20 text-white rounded-circle d-inline-flex p-3 mb-2 shadow-sm">
                        <i class="fa-solid fa-id-card fa-2x"></i>
                    </div>
                    <h3 class="fw-bold mb-1">Driver Portal</h3>
                    <p class="mb-0 text-white-50 small">Sign in to your linked profile or register as a NEXT GO driver</p>
                </div>

                <!-- Tabs -->
                <ul class="nav nav-tabs nav-fill" id="authTabs" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active" id="login-tab" data-bs-toggle="tab" data-bs-target="#loginPane" type="button" role="tab">
                            <i class="fa-solid fa-right-to-bracket me-1"></i> Driver Sign In
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="register-tab" data-bs-toggle="tab" data-bs-target="#registerPane" type="button" role="tab">
                            <i class="fa-solid fa-user-plus me-1"></i> Driver Registration
                        </button>
                    </li>
                </ul>

                <div class="card-body p-4 p-md-5">
                    <!-- Error / Success Alerts -->
                    <c:if test="${not empty errorMsg}">
                        <div class="alert alert-danger py-2 small fw-bold rounded-3 mb-4">
                            <i class="fa-solid fa-triangle-exclamation me-1"></i> <c:out value="${errorMsg}"/>
                        </div>
                    </c:if>
                    <c:if test="${not empty successMsg}">
                        <div class="alert alert-success py-2 small fw-bold rounded-3 mb-4">
                            <i class="fa-solid fa-circle-check me-1"></i> <c:out value="${successMsg}"/>
                        </div>
                    </c:if>

                    <div class="tab-content" id="authTabContent">
                        <!-- Login Pane -->
                        <div class="tab-pane fade show active" id="loginPane" role="tabpanel">
                            <form action="${pageContext.request.contextPath}/ride/driver/login" method="post">
                                <div class="mb-3">
                                    <label class="form-label small fw-semibold text-muted">Driver Email Address</label>
                                    <div class="input-group">
                                        <span class="input-group-text"><i class="fa-solid fa-envelope"></i></span>
                                        <input type="email" name="email" class="form-control" required placeholder="driver@email.com" autofocus>
                                    </div>
                                </div>
                                <div class="mb-4">
                                    <label class="form-label small fw-semibold text-muted">Password</label>
                                    <div class="input-group">
                                        <span class="input-group-text"><i class="fa-solid fa-lock"></i></span>
                                        <input type="password" name="password" class="form-control" required placeholder="••••••••">
                                    </div>
                                </div>
                                <button type="submit" class="btn btn-driver-primary w-100 py-3">
                                    <i class="fa-solid fa-right-to-bracket me-2"></i> Enter Driver Dashboard
                                </button>
                            </form>
                        </div>

                        <!-- Register Pane -->
                        <div class="tab-pane fade" id="registerPane" role="tabpanel">
                            <form action="${pageContext.request.contextPath}/ride/driver/register" method="post">
                                <div class="row g-3">
                                    <div class="col-12">
                                        <label class="form-label small fw-semibold text-muted">Full Name</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fa-solid fa-user"></i></span>
                                            <input type="text" name="fullName" class="form-control" required placeholder="e.g. Sunil Perera">
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label small fw-semibold text-muted">Email Address</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fa-solid fa-envelope"></i></span>
                                            <input type="email" name="email" class="form-control" required placeholder="driver@email.com">
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label small fw-semibold text-muted">Password</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fa-solid fa-lock"></i></span>
                                            <input type="password" name="password" class="form-control" required placeholder="••••••••">
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label small fw-semibold text-muted">Phone Number</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fa-solid fa-phone"></i></span>
                                            <input type="text" name="phoneNumber" class="form-control" required placeholder="0771234567">
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label small fw-semibold text-muted">Vehicle Number</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fa-solid fa-hashtag"></i></span>
                                            <input type="text" name="vehicleNumber" class="form-control" required placeholder="WP-CAD-5678">
                                        </div>
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label small fw-semibold text-muted">Vehicle Type</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fa-solid fa-car-side"></i></span>
                                            <select name="vehicleType" class="form-select" required>
                                                <option value="">Select vehicle type...</option>
                                                <option value="Car">Car</option>
                                                <option value="Van">Van</option>
                                                <option value="Bike">Bike</option>
                                                <option value="Tuk">Tuk (Three-Wheeler)</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                                <button type="submit" class="btn btn-driver-primary w-100 py-3 mt-4">
                                    <i class="fa-solid fa-user-plus me-2"></i> Register Driver Account & Profile
                                </button>
                            </form>
                        </div>
                    </div>
                </div>

                <div class="bg-light p-3 text-center border-top small text-muted">
                    <i class="fa-solid fa-link text-success me-1"></i> Each driver account is atomically linked to exactly one verified driver profile.
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", function() {
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('tab') === 'register') {
            const regTab = document.getElementById('register-tab');
            if (regTab) {
                bootstrap.Tab.getOrCreateInstance(regTab).show();
            }
        }
    });
</script>
</body>
</html>
