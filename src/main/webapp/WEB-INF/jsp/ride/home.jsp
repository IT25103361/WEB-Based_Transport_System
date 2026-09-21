<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>NEXT GO — Super App Dashboard</title>
  <!-- Bootstrap 5 -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
  <style>
    body {
      background: linear-gradient(rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.6)),
      url('https://images.unsplash.com/photo-1519501025264-65ba15a82390?q=80&w=1920&auto=format&fit=crop') no-repeat center center fixed;
      background-size: cover;
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }
    .feature-card {
      transition: transform 0.3s ease, box-shadow 0.3s ease;
      border: none;
      border-radius: 16px;
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(10px);
    }
    .feature-card:hover {
      transform: translateY(-7px);
      box-shadow: 0 15px 30px rgba(0,0,0,0.3);
    }
    .hero-banner {
      background: linear-gradient(135deg, rgba(13, 110, 253, 0.9), rgba(102, 16, 242, 0.9));
      color: white;
      border-radius: 20px;
      padding: 50px 40px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.3);
    }
    .glass-navbar {
      background: rgba(33, 37, 41, 0.9) !important;
      backdrop-filter: blur(10px);
    }
  </style>
</head>
<body>

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark glass-navbar shadow-sm sticky-top">
  <div class="container px-4">
    <a class="navbar-brand fw-bold" href="/ride/home">
      <i class="fa-solid fa-taxi text-warning me-2"></i> NEXT GO — Transport & Services Hub
    </a>
    <div class="d-flex align-items-center gap-3">
      <span class="navbar-text text-light small d-none d-md-block">
          <i class="fa-solid fa-circle-check text-success me-1"></i> System Online
      </span>
      <a href="/admin/login" class="btn btn-outline-warning btn-sm fw-bold">
        <i class="fa-solid fa-lock me-1"></i> Admin Portal
      </a>
    </div>
  </div>
</nav>

<div class="container py-5">
  <!-- Hero Banner -->
  <div class="hero-banner mb-5 text-center text-md-start">
    <div class="row align-items-center">
      <div class="col-lg-12">
        <span class="badge bg-warning text-dark mb-3 px-3 py-2 fw-bold rounded-pill shadow-sm">
            <i class="fa-solid fa-globe me-1"></i> Next-Gen Multi-Service Platform | 6-Member Group Project
        </span>
        <h1 class="fw-bold display-5 mb-3 text-white">Welcome to NEXT GO</h1>
        <p class="lead mb-0 col-lg-9 text-white-50">Your ultimate transport & logistics ecosystem. Access Ride Management and explore upcoming modules built collaboratively by our team!</p>
      </div>
    </div>
  </div>

  <!-- 6 Main Project Modules Grid -->
  <div class="d-flex align-items-center justify-content-between mb-4">
    <h4 class="fw-bold text-white mb-0"><i class="fa-solid fa-cubes me-2 text-warning"></i> NEXT GO Core Services (Group Modules)</h4>
    <span class="badge bg-light text-dark px-3 py-2 fw-bold rounded-pill">Total Modules: 6</span>
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
          <a href="/ride/ride-hub" class="btn btn-primary btn-sm w-100 fw-semibold mt-2 shadow-sm">
            <i class="fa-solid fa-arrow-right me-1"></i> Open Ride Portal
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
          <button class="btn btn-outline-success btn-sm w-100 fw-semibold mt-2" disabled>Member Module (In Progress)</button>
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
          <button class="btn btn-outline-warning btn-sm w-100 fw-semibold text-dark mt-2" disabled>Member Module (In Progress)</button>
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
          <button class="btn btn-outline-danger btn-sm w-100 fw-semibold mt-2" disabled>Member Module (In Progress)</button>
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
          <button class="btn btn-outline-info btn-sm w-100 fw-semibold mt-2" disabled>Member Module (In Progress)</button>
        </div>
      </div>
    </div>

    <!-- 6. Emergency Services -->
    <div class="col-md-4">
      <div class="card feature-card shadow h-100 border-top border-dark border-4">
        <div class="card-body p-4 text-center">
          <div class="bg-dark bg-opacity-10 text-dark rounded-circle d-inline-flex p-3 mb-3">
            <i class="fa-solid fa-truck-medical fa-2x"></i>
          </div>
          <h5 class="fw-bold text-dark">6. Emergency Services</h5>
          <p class="text-muted small">Quick dispatch for medical emergencies, towing, and safety support.</p>
          <button class="btn btn-outline-dark btn-sm w-100 fw-semibold mt-2" disabled>Member Module (In Progress)</button>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- Footer -->
<footer class="text-center py-4 text-white-50 small border-top border-secondary mt-5 bg-dark bg-opacity-75">
  <p class="mb-0">NEXT GO Transport System &copy; 2026 — Group Major Project (All 6 Members Integrated)</p>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>