<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ride Management Hub - NEXT GO</title>
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <style>
        .role-card {
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            border: none;
            border-radius: 16px;
            overflow: hidden;
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

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
    <div class="container px-4">
        <a class="navbar-brand fw-bold" href="/ride/home">
            <i class="fa-solid fa-taxi text-warning me-2"></i> NEXT GO — Ride Management
        </a>
        <a href="/ride/home" class="btn btn-outline-light btn-sm">
            <i class="fa-solid fa-house me-1"></i> Main Hub
        </a>
    </div>
</nav>

<div class="container py-5">
    <div class="text-center mb-5">
        <span class="badge bg-primary bg-opacity-10 text-primary px-3 py-2 rounded-pill fw-bold mb-2">
            <i class="fa-solid fa-compass me-1"></i> Module Selection
        </span>
        <h2 class="fw-bold text-dark display-6">Select Your Ride Portal</h2>
        <p class="text-muted col-lg-6 mx-auto">Choose whether you want to book a ride as a Passenger or view assigned rides as a Driver.</p>
    </div>

    <div class="row justify-content-center g-4">
        <!-- Option 1: Passenger Portal -->
        <div class="col-md-5">
            <div class="card role-card shadow-sm h-100 bg-white border-top border-primary border-4">
                <div class="card-body p-5 text-center">
                    <div class="icon-box bg-primary bg-opacity-10 text-primary shadow-sm">
                        <i class="fa-solid fa-user-tag fa-2x"></i>
                    </div>
                    <h3 class="fw-bold mb-3 text-primary">Passenger Portal</h3>
                    <p class="text-muted mb-4 small px-2">Request rides with pickup and drop locations, auto-assign drivers, and view booking history.</p>
                    <a href="/ride/passenger" class="btn btn-primary btn-lg w-100 fw-semibold py-2 shadow-sm">
                        <i class="fa-solid fa-arrow-right me-2"></i> Enter as Passenger
                    </a>
                </div>
            </div>
        </div>

        <!-- Option 2: Driver Portal -->
        <div class="col-md-5">
            <div class="card role-card shadow-sm h-100 bg-white border-top border-success border-4">
                <div class="card-body p-5 text-center">
                    <div class="icon-box bg-success bg-opacity-10 text-success shadow-sm">
                        <i class="fa-solid fa-id-badge fa-2x"></i>
                    </div>
                    <h3 class="fw-bold mb-3 text-success">Driver Portal</h3>
                    <p class="text-muted mb-4 small px-2">View automatically assigned rides, track trip statuses, and mark completed jobs.</p>
                    <a href="/ride/driver" class="btn btn-success btn-lg w-100 fw-semibold py-2 text-white shadow-sm">
                        <i class="fa-solid fa-arrow-right me-2"></i> Enter as Driver
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Footer -->
<footer class="text-center py-4 text-muted small border-top mt-5">
    <p class="mb-0">NEXT GO Transport System &copy; 2026 — Ride Management Module</p>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>