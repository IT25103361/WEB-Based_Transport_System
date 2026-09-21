<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>School Transport Hub - NEXT GO</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <style>
        .role-card {
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            border: none;
            border-radius: 16px;
        }
        .role-card:hover {
            transform: translateY(-8px);
            box-shadow: 0 15px 30px rgba(0,0,0,0.12);
        }
        .icon-box {
            width: 80px;
            height: 80px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            margin: 0 auto 20px auto;
        }
    </style>
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
    <div class="container px-4">
        <a class="navbar-brand fw-bold" href="/">
            <i class="fa-solid fa-school-bus text-warning me-2"></i> NEXT GO — School Transport
        </a>
        <a href="/" class="btn btn-outline-light btn-sm">
            <i class="fa-solid fa-house me-1"></i> Main Hub
        </a>
    </div>
</nav>

<div class="container py-5">
    <div class="text-center mb-5">
        <span class="badge bg-warning text-dark px-3 py-2 rounded-pill fw-bold mb-2">
            <i class="fa-solid fa-bus-simple me-1"></i> Student Pooling System
        </span>
        <h2 class="fw-bold text-dark display-6">Select School Transport Portal</h2>
        <p class="text-muted col-lg-6 mx-auto">Choose your portal to manage monthly packages, driver routes, or live vehicle tracking for parents.</p>
    </div>

    <div class="row justify-content-center g-4">
        <!-- Student Portal Card -->
        <div class="col-md-4">
            <div class="card role-card shadow-sm h-100 bg-white border-top border-warning border-4">
                <div class="card-body p-4 text-center">
                    <div class="icon-box bg-warning bg-opacity-10 text-warning shadow-sm">
                        <i class="fa-solid fa-child-reaching fa-2x"></i>
                    </div>
                    <h4 class="fw-bold mb-3 text-dark">Student Portal</h4>
                    <p class="text-muted mb-4 small px-2">Register for school transport, choose van or bus, and track monthly attendance packages.</p>
                    <a href="/school-student" class="btn btn-warning btn-md w-100 fw-semibold py-2 text-dark shadow-sm">
                        <i class="fa-solid fa-arrow-right me-2"></i> Enter Student Portal
                    </a>
                </div>
            </div>
        </div>

        <!-- Driver Portal Card -->
        <div class="col-md-4">
            <div class="card role-card shadow-sm h-100 bg-white border-top border-success border-4">
                <div class="card-body p-4 text-center">
                    <div class="icon-box bg-success bg-opacity-10 text-success shadow-sm">
                        <i class="fa-solid fa-id-card-clip fa-2x"></i>
                    </div>
                    <h4 class="fw-bold mb-3 text-success">Driver Portal</h4>
                    <p class="text-muted mb-4 small px-2">Manage student passenger lists, view monthly subscription packages, and track route attendance logs.</p>
                    <a href="/school-driver" class="btn btn-success btn-md w-100 fw-semibold py-2 text-white shadow-sm">
                        <i class="fa-solid fa-arrow-right me-2"></i> Enter Driver Portal
                    </a>
                </div>
            </div>
        </div>

        <!-- Parent Tracking Portal Card (NEW) -->
        <div class="col-md-4">
            <div class="card role-card shadow-sm h-100 bg-white border-top border-info border-4">
                <div class="card-body p-4 text-center">
                    <div class="icon-box bg-info bg-opacity-10 text-info shadow-sm">
                        <i class="fa-solid fa-location-crosshairs fa-2x"></i>
                    </div>
                    <h4 class="fw-bold mb-3 text-info">Parent Live Tracking</h4>
                    <p class="text-muted mb-4 small px-2">Track the school van/bus in real-time, view student location updates, and monitor pick-up status.</p>
                    <a href="/school-parent" class="btn btn-info btn-md w-100 fw-semibold py-2 text-white shadow-sm">
                        <i class="fa-solid fa-arrow-right me-2"></i> Enter Parent Portal
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>