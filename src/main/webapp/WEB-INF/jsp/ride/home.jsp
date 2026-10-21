<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>NEXT GO — Super App Dashboard</title>
  <!-- Google Fonts: Inter -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
  <!-- Bootstrap 5 -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
  <style>
    body {
      background: linear-gradient(rgba(15, 23, 42, 0.7), rgba(15, 23, 42, 0.8)),
      url('https://images.unsplash.com/photo-1519501025264-65ba15a82390?q=80&w=1920&auto=format&fit=crop') no-repeat center center fixed;
      background-size: cover;
      font-family: 'Inter', sans-serif;
      color: #f8fafc;
    }
    .feature-card {
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 24px;
      background: rgba(255, 255, 255, 0.05);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
    }
    .feature-card:hover {
      transform: translateY(-10px) scale(1.02);
      box-shadow: 0 20px 40px rgba(0,0,0,0.3);
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.2);
    }
    .feature-card h5 {
      color: #fff;
    }
    .hero-banner {
      background: linear-gradient(135deg, rgba(37, 99, 235, 0.8), rgba(124, 58, 237, 0.8));
      backdrop-filter: blur(10px);
      color: white;
      border-radius: 30px;
      padding: 60px 40px;
      box-shadow: 0 20px 50px rgba(0,0,0,0.25);
      border: 1px solid rgba(255, 255, 255, 0.2);
    }
    .glass-navbar {
      background: rgba(15, 23, 42, 0.75) !important;
      backdrop-filter: blur(15px);
      -webkit-backdrop-filter: blur(15px);
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }
    /* Premium Modal Styling */
    .modal-content.glass-modal {
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(20px);
      border-radius: 24px;
      border: 1px solid rgba(255,255,255,0.4);
      box-shadow: 0 25px 50px rgba(0,0,0,0.2);
      overflow: hidden;
    }
    .glass-modal .modal-header {
      background: linear-gradient(135deg, #2563eb, #4f46e5);
      border-bottom: none;
      padding: 24px 30px;
    }
    .glass-modal .modal-title {
      font-weight: 700;
      letter-spacing: 0.5px;
      color: white;
    }
    .glass-modal .modal-body {
      padding: 40px 30px 20px;
    }
    .glass-modal .form-control {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 12px 20px;
      font-size: 0.95rem;
      transition: all 0.3s ease;
      box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
    }
    .glass-modal .form-control:focus {
      background: #fff;
      border-color: #3b82f6;
      box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.15);
      outline: none;
    }
    .glass-modal .input-group-text {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-right: none;
      border-radius: 12px 0 0 12px;
      color: #64748b;
    }
    .glass-modal .form-control {
      border-left: none;
      border-radius: 0 12px 12px 0;
      padding-left: 0;
    }
    .glass-modal .form-label {
      color: #334155;
      font-weight: 600;
      margin-bottom: 8px;
    }
    .glass-modal .btn-primary {
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      border: none;
      border-radius: 12px;
      padding: 12px;
      font-weight: 600;
      transition: all 0.3s;
      box-shadow: 0 4px 15px rgba(37, 99, 235, 0.3);
    }
    .glass-modal .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(37, 99, 235, 0.4);
    }
    .glass-modal .btn-success {
      background: linear-gradient(135deg, #10b981, #059669);
      border: none;
      border-radius: 12px;
      padding: 12px;
      font-weight: 600;
      transition: all 0.3s;
      box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);
    }
    .glass-modal .btn-success:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(16, 185, 129, 0.4);
    }
    .glass-modal .btn-secondary {
      background: #f1f5f9;
      color: #475569;
      border: none;
      border-radius: 12px;
      padding: 12px 24px;
      font-weight: 600;
      transition: all 0.3s;
    }
    .glass-modal .btn-secondary:hover {
      background: #e2e8f0;
      color: #1e293b;
    }
    .modal-footer {
      border-top: none;
      padding: 0 30px 30px;
    }
  </style>

  <!-- NEXT GO Shared Theme -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nextgo-theme.css">
  <script src="${pageContext.request.contextPath}/js/nextgo-theme.js"></script>
</head>
<body class="home-super-app">

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark glass-navbar shadow-sm sticky-top">
  <div class="container px-4">
    <a class="navbar-brand fw-bold" href="/ride/home">
      <i class="fa-solid fa-taxi text-warning me-2"></i> NEXT GO — Transport & Services Hub
    </a>
    <div class="d-flex align-items-center gap-3">
      <button type="button" class="nextgo-theme-toggle" data-nextgo-theme-toggle title="Toggle light/dark theme" aria-label="Toggle light/dark theme">🌙</button>
      <c:choose>
        <c:when test="${user != null}">
          <span class="navbar-text text-light fw-bold">
            <i class="fa-solid fa-user-circle me-1"></i> Welcome, ${user.fullName}
          </span>
          <a href="/logout" class="btn btn-outline-danger btn-sm fw-bold">Logout</a>
        </c:when>
        <c:otherwise>
          <button type="button" class="btn btn-primary btn-sm fw-bold shadow-sm" data-bs-toggle="modal" data-bs-target="#loginModal">
            <i class="fa-solid fa-right-to-bracket me-1"></i> Login
          </button>
          <button type="button" class="btn btn-outline-light btn-sm fw-bold" data-bs-toggle="modal" data-bs-target="#registerModal">
            <i class="fa-solid fa-user-plus me-1"></i> Register
          </button>
        </c:otherwise>
      </c:choose>
      <div class="vr bg-light mx-1 opacity-50 d-none d-md-block" style="width: 2px;"></div>
      <span class="navbar-text text-light small d-none d-md-block">
          <i class="fa-solid fa-circle-check text-success me-1"></i> System Online
      </span>
    </div>
  </div>
</nav>

<div class="container py-5">
  <!-- Hero Banner -->
  <div class="hero-banner mb-5 text-center text-md-start">
    <div class="row align-items-center">
      <div class="col-lg-12">
        <h1 class="fw-bold display-5 mb-3 text-white">Welcome to NEXT GO</h1>
        <p class="lead mb-0 col-lg-9 text-white-50">Your ultimate transport & logistics ecosystem. Access Ride Management and explore upcoming modules built collaboratively by our team!</p>
      </div>
    </div>
  </div>

  <!-- 6 Main Project Modules Grid -->
  <div class="mb-4">
    <h4 class="fw-bold text-white mb-0"><i class="fa-solid fa-cubes me-2 text-warning"></i> NEXT GO Core Services</h4>
  </div>

  <div class="row g-4">
    <!-- 1. Ride Management System (Active) -->
    <div class="col-md-4">
      <div class="card feature-card shadow h-100 border-top border-primary border-4">
        <div class="card-body p-4 text-center">
          <div class="bg-primary bg-opacity-10 text-primary rounded-circle d-inline-flex p-3 mb-3">
            <i class="fa-solid fa-taxi fa-2x"></i>
          </div>
          <h5 class="fw-bold text-primary">1. Ride Management System</h5>
          <p class="text-muted small">Book rides with map lookup, choose vehicle types, manage drivers, and submit reviews.</p>
          <a href="${pageContext.request.contextPath}/ride/ride-hub" class="btn btn-primary btn-sm w-100 fw-semibold mt-2 shadow-sm">
            <i class="fa-solid fa-arrow-right me-1"></i> Open Ride Management
          </a>
        </div>
      </div>
    </div>

    <!-- 2. Courier Delivery Service -->
    <div class="col-md-4">
      <div class="card feature-card shadow h-100 border-top border-success border-4">
        <div class="card-body p-4 text-center">
          <div class="bg-success bg-opacity-10 text-success rounded-circle d-inline-flex p-3 mb-3">
            <i class="fa-solid fa-box-open fa-2x"></i>
          </div>
          <h5 class="fw-bold text-success">2. Courier Delivery Service</h5>
          <p class="text-muted small">Send packages, documents, and parcels safely across cities with live tracking.</p>
          <a href="${pageContext.request.contextPath}/courier" class="btn btn-success btn-sm w-100 fw-semibold mt-2 shadow-sm">
            <i class="fa-solid fa-arrow-right me-1"></i> Open Courier Service
          </a>
        </div>
      </div>
    </div>

    <!-- 3. School Student Transport System -->
    <div class="col-md-4">
      <div class="card feature-card shadow h-100 border-top border-warning border-4">
        <div class="card-body p-4 text-center">
          <div class="bg-warning bg-opacity-10 text-warning rounded-circle d-inline-flex p-3 mb-3">
            <i class="fa-solid fa-school-bus fa-2x"></i>
          </div>
          <h5 class="fw-bold text-dark">3. School Student Transport</h5>
          <p class="text-muted small">Reliable van and bus pooling system for school children with monthly packages.</p>
          <c:choose>
            <c:when test="${user != null}">
              <a href="${pageContext.request.contextPath}/school/index" class="btn btn-warning btn-sm w-100 fw-semibold text-dark mt-2 shadow-sm">
                <i class="fa-solid fa-arrow-right me-1"></i> Open School Portal
              </a>
            </c:when>
            <c:otherwise>
              <button class="btn btn-secondary btn-sm w-100 fw-semibold mt-2 shadow-sm" onclick="new bootstrap.Modal(document.getElementById('loginModal')).show()">
                <i class="fa-solid fa-lock me-1"></i> Login to Access
              </button>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>

    <!-- 4. Food and Grocery Management -->
    <div class="col-md-4">
      <div class="card feature-card shadow h-100 border-top border-danger border-4">
        <div class="card-body p-4 text-center">
          <div class="bg-danger bg-opacity-10 text-danger rounded-circle d-inline-flex p-3 mb-3">
            <i class="fa-solid fa-utensils fa-2x"></i>
          </div>
          <h5 class="fw-bold text-danger">4. Food & Grocery Management</h5>
          <p class="text-muted small">Order meals from local restaurants and daily essentials instantly.</p>
          <a href="${pageContext.request.contextPath}/food/customer" class="btn btn-outline-danger btn-sm w-100 fw-semibold mt-2">Open Food & Grocery</a>
        </div>
      </div>
    </div>

    <!-- 5. Vehicle Rental Management -->
    <div class="col-md-4">
      <div class="card feature-card shadow h-100 border-top border-info border-4">
        <div class="card-body p-4 text-center">
          <div class="bg-info bg-opacity-10 text-info rounded-circle d-inline-flex p-3 mb-3">
            <i class="fa-solid fa-car-keys fa-2x"></i>
          </div>
          <h5 class="fw-bold text-info">5. Vehicle Rental Management</h5>
          <p class="text-muted small">Rent self-drive cars, bikes, or luxury vehicles on daily or weekly basis.</p>
          <c:choose>
            <c:when test="${user != null}">
              <a href="/rental/index" class="btn btn-info btn-sm w-100 fw-semibold text-white mt-2 shadow-sm">
                <i class="fa-solid fa-arrow-right me-1"></i> Open Rental Portal
              </a>
            </c:when>
            <c:otherwise>
              <button class="btn btn-secondary btn-sm w-100 fw-semibold mt-2 shadow-sm" onclick="new bootstrap.Modal(document.getElementById('loginModal')).show()">
                <i class="fa-solid fa-lock me-1"></i> Login to Access
              </button>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>

    <!-- 6. Emergency Services -->
    <div class="col-md-4">
      <div class="card feature-card shadow h-100 border-top border-danger border-4">
        <div class="card-body p-4 text-center">
          <div class="bg-danger bg-opacity-10 text-danger rounded-circle d-inline-flex p-3 mb-3">
            <i class="fa-solid fa-truck-medical fa-2x"></i>
          </div>
          <h5 class="fw-bold text-dark">6. Emergency Services</h5>
          <p class="text-muted small">Quick dispatch for medical emergencies, towing, and safety support.</p>
          <a href="${pageContext.request.contextPath}/emergency" class="btn btn-danger btn-sm w-100 fw-semibold mt-2">
            <i class="fa-solid fa-truck-medical me-1"></i> Open Emergency Portal
          </a>
        </div>
      </div>
    </div>
  </div>
</div>


<!-- Login Modal -->
<div class="modal fade" id="loginModal" tabindex="-1" aria-labelledby="loginModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content glass-modal">
      <div class="modal-header">
        <h5 class="modal-title" id="loginModalLabel"><i class="fa-solid fa-right-to-bracket me-2"></i> User Login</h5>
        <button type="button" class="btn-close btn-close-white opacity-75 hover-opacity-100" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form action="${pageContext.request.contextPath}/login" method="post">
        <c:if test="${not empty param.redirect}">
          <input type="hidden" name="redirect" value="<c:out value='${param.redirect}' />" />
        </c:if>
        <div class="modal-body">
          <c:if test="${param.error == 'invalid'}">
            <div class="alert alert-danger py-2 small fw-bold rounded-3"><i class="fa-solid fa-triangle-exclamation me-1"></i> Invalid email or password!</div>
          </c:if>
          <div class="mb-4">
            <label class="form-label">Email Address</label>
            <div class="input-group">
              <span class="input-group-text"><i class="fa-solid fa-envelope"></i></span>
              <input type="email" name="email" class="form-control" required placeholder="example@email.com">
            </div>
          </div>
          <div class="mb-4">
            <label class="form-label">Password</label>
            <div class="input-group">
              <span class="input-group-text"><i class="fa-solid fa-lock"></i></span>
              <input type="password" name="password" class="form-control" required placeholder="••••••••">
            </div>
          </div>
        </div>
        <div class="modal-footer justify-content-between">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary px-5">Log In</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Register Modal -->
<div class="modal fade" id="registerModal" tabindex="-1" aria-labelledby="registerModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content glass-modal">
      <div class="modal-header" style="background: linear-gradient(135deg, #10b981, #059669);">
        <h5 class="modal-title" id="registerModalLabel"><i class="fa-solid fa-user-plus me-2"></i> Create Account</h5>
        <button type="button" class="btn-close btn-close-white opacity-75 hover-opacity-100" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form action="${pageContext.request.contextPath}/register" method="post">
        <c:if test="${not empty param.redirect}">
          <input type="hidden" name="redirect" value="<c:out value='${param.redirect}' />" />
        </c:if>
        <div class="modal-body">
          <c:if test="${param.regError == 'exists'}">
            <div class="alert alert-danger py-2 small fw-bold rounded-3"><i class="fa-solid fa-triangle-exclamation me-1"></i> Email is already registered!</div>
          </c:if>
          <div class="mb-4">
            <label class="form-label">Full Name</label>
            <div class="input-group">
              <span class="input-group-text"><i class="fa-solid fa-user"></i></span>
              <input type="text" name="fullName" class="form-control" required placeholder="John Doe">
            </div>
          </div>
          <div class="mb-4">
            <label class="form-label">Email Address</label>
            <div class="input-group">
              <span class="input-group-text"><i class="fa-solid fa-envelope"></i></span>
              <input type="email" name="email" class="form-control" required placeholder="example@email.com">
            </div>
          </div>
          <div class="mb-4">
            <label class="form-label">Password</label>
            <div class="input-group">
              <span class="input-group-text"><i class="fa-solid fa-lock"></i></span>
              <input type="password" name="password" class="form-control" required placeholder="••••••••">
            </div>
          </div>
        </div>
        <div class="modal-footer justify-content-between">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-success px-5">Register</button>
        </div>
      </form>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
  document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('loginRequired') === 'true' || urlParams.get('error') === 'invalid') {
      new bootstrap.Modal(document.getElementById('loginModal')).show();
    }
    if (urlParams.get('regError') === 'exists') {
      new bootstrap.Modal(document.getElementById('registerModal')).show();
    }
  });
</script>
</body>
</html>
