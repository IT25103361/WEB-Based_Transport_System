<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Rental Admin Portal - NEXT GO</title>
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
            font-family: 'Inter', sans-serif;
            background-color: #f1f5f9;
        }
        .premium-nav-admin {
            background: linear-gradient(135deg, #1e293b, #0f172a);
            border-bottom: 2px solid #8b5cf6;
        }
        .premium-card {
            background: #ffffff;
            border: none;
            border-radius: 20px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.05);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }
        .premium-card:hover {
            box-shadow: 0 15px 40px rgba(0,0,0,0.08);
            transform: translateY(-2px);
        }
        .card-header-admin {
            background: linear-gradient(135deg, #8b5cf6, #6d28d9);
            border-radius: 20px 20px 0 0 !important;
            padding: 20px;
            border-bottom: none;
        }
        .card-header-dark {
            background: linear-gradient(135deg, #334155, #1e293b);
            border-radius: 20px 20px 0 0 !important;
            padding: 20px;
            border-bottom: none;
        }
        .form-control, .form-select {
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 12px 16px;
            background-color: #f8fafc;
            transition: all 0.2s;
        }
        .form-control:focus, .form-select:focus {
            background-color: #fff;
            border-color: #8b5cf6;
            box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.1);
        }
        .btn-admin {
            background: linear-gradient(135deg, #8b5cf6, #6d28d9);
            border: none;
            border-radius: 12px;
            color: #fff;
            font-weight: 600;
            transition: all 0.3s;
            box-shadow: 0 4px 15px rgba(139, 92, 246, 0.3);
        }
        .btn-admin:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(139, 92, 246, 0.4);
            color: #fff;
        }
        .premium-table { border-collapse: separate; border-spacing: 0 10px; }
        .premium-table thead th { border: none; background: transparent; color: #64748b; font-weight: 600; text-transform: uppercase; font-size: 0.8rem; letter-spacing: 0.5px; padding-bottom: 10px; }
        .premium-table tbody tr { background: #fff; box-shadow: 0 2px 10px rgba(0,0,0,0.02); transition: transform 0.2s; }
        .premium-table tbody tr:hover { transform: scale(1.01); box-shadow: 0 5px 15px rgba(0,0,0,0.05); }
        .premium-table td { border: none; padding: 16px; vertical-align: middle; }
        .premium-table td:first-child { border-radius: 12px 0 0 12px; }
        .premium-table td:last-child { border-radius: 0 12px 12px 0; }
        .vehicle-thumb {
            width: 50px; height: 50px; object-fit: cover; border-radius: 8px;
        }
    </style>
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark premium-nav-admin shadow-sm">
    <div class="container-fluid px-4 py-1">
        <a class="navbar-brand fw-bold" href="/rental/index">
            <i class="fa-solid fa-user-shield text-warning me-2"></i> NEXT GO <span class="fw-light opacity-75">| Rental Admin Portal</span>
        </a>
        <div class="d-flex align-items-center">
            <button type="button" class="nextgo-theme-toggle me-3" data-nextgo-theme-toggle title="Toggle light/dark theme" aria-label="Toggle light/dark theme">🌙</button>
            <a href="/rental/index" class="btn btn-outline-light btn-sm me-2 rounded-pill px-3">
                <i class="fa-solid fa-arrows-split-up-and-left me-1"></i> Switch Role
            </a>
            <a href="/ride/home" class="btn btn-light btn-sm rounded-pill px-3 fw-semibold">
                <i class="fa-solid fa-house me-1"></i> Home
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid py-5 px-4 px-lg-5">
    <div class="row g-5">
        
        <!-- Left Column: Add Vehicle -->
        <div class="col-lg-4">
            <div class="card premium-card mb-4">
                <div class="card-header card-header-admin text-white">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-plus-circle me-2"></i> Add New Vehicle</h5>
                </div>
                <div class="card-body p-4 p-md-5">
                    <form action="/rental/vehicle/add" method="post">
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Vehicle Name / Model</label>
                            <input type="text" name="name" class="form-control" placeholder="e.g. Toyota Aqua 2018" required />
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Vehicle Type</label>
                            <select name="type" class="form-select" required>
                                <option value="">Select Type</option>
                                <option value="Car">Car</option>
                                <option value="Van">Van</option>
                                <option value="Bike">Bike</option>
                                <option value="Luxury">Luxury</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Daily Rate (LKR)</label>
                            <input type="number" step="0.01" name="dailyRate" class="form-control" placeholder="e.g. 5000" required />
                        </div>
                        <div class="mb-4">
                            <label class="form-label fw-semibold small text-muted">Image URL (Optional)</label>
                            <input type="url" name="imageUrl" class="form-control" placeholder="https://..." />
                        </div>
                        <button type="submit" class="btn btn-admin w-100 py-3 text-uppercase letter-spacing-1">
                            <i class="fa-solid fa-save me-2"></i> Save Vehicle
                        </button>
                    </form>
                </div>
            </div>

            <!-- Vehicles List -->
            <div class="card premium-card">
                <div class="card-body p-4">
                    <h6 class="fw-bold mb-3"><i class="fa-solid fa-list me-2"></i> Fleet Management</h6>
                    <ul class="list-group list-group-flush small">
                        <c:forEach var="v" items="${vehicles}">
                            <li class="list-group-item px-0 py-2 border-bottom">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div class="d-flex align-items-center">
                                        <img src="${v.resolveImageUrl(pageContext.request.contextPath)}" class="vehicle-thumb me-3" alt="${v.name}" onerror="if (!this.dataset.tried) { this.dataset.tried = 'true'; this.src='${pageContext.request.contextPath}/rental/images/vehicle-fallback.svg'; }">
                                        <div>
                                            <strong class="d-block">${v.name}</strong>
                                            <span class="text-muted text-xs">${v.type} - LKR ${v.dailyRate}/day</span>
                                        </div>
                                    </div>
                                    <div class="text-end">
                                        <span class="badge bg-${v.status == 'AVAILABLE' ? 'success' : 'danger'} mb-1">${v.status}</span><br>
                                        <a href="/rental/vehicle/delete/${v.id}" class="text-danger" onclick="return confirm('Delete this vehicle?');"><i class="fa-solid fa-trash"></i></a>
                                    </div>
                                </div>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </div>

        <!-- Right Column: Booking Management -->
        <div class="col-lg-8">
            <div class="card premium-card h-100">
                <div class="card-header card-header-dark text-white">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-calendar-check me-2"></i> Manage Rental Bookings</h5>
                </div>
                <div class="card-body p-4 bg-light rounded-bottom-4">
                    <div class="table-responsive">
                        <table class="table premium-table align-middle w-100">
                            <thead>
                            <tr>
                                <th class="ps-3">ID / Customer</th>
                                <th>Vehicle & Dates</th>
                                <th>Cost</th>
                                <th>Status</th>
                                <th class="text-end pe-3">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="b" items="${bookings}">
                                <tr>
                                    <td class="ps-3">
                                        <span class="fw-bold text-info">#${b.id}</span><br>
                                        <span class="small fw-semibold text-dark">${b.customerName}</span><br>
                                        <span class="text-muted text-xs"><i class="fa-solid fa-phone me-1"></i>${b.customerPhone}</span>
                                    </td>
                                    <td>
                                        <div class="fw-bold text-dark small">${b.vehicle.name}</div>
                                        <div class="text-muted text-xs mt-1">
                                            <i class="fa-solid fa-calendar me-1"></i> ${b.startDate} to ${b.endDate}<br>
                                            <span class="badge bg-light text-dark border mt-1">${b.days} Days</span>
                                            <c:if test="${not empty b.extendedFromDate}">
                                                <span class="badge bg-info text-dark border mt-1" title="Extended from ${b.extendedFromDate}"><i class="fa-solid fa-clock-rotate-left me-1"></i>Extended</span>
                                            </c:if>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="fw-bold text-success">LKR ${b.totalCost}</span>
                                    </td>
                                    <td>
                                        <span class="badge bg-${b.status == 'CONFIRMED' ? 'success' : (b.status == 'COMPLETED' ? 'secondary' : (b.status == 'CANCELLED' ? 'danger' : 'warning text-dark'))}">
                                            ${b.status}
                                        </span>
                                    </td>
                                    <td class="text-end pe-3">
                                        <c:choose>
                                            <c:when test="${b.status == 'PENDING'}">
                                                <a href="/rental/booking/status/${b.id}?status=CONFIRMED" class="btn btn-success btn-sm px-2 py-1 mb-1 fw-bold" title="Confirm Booking"><i class="fa-solid fa-check"></i></a>
                                                <a href="/rental/booking/status/${b.id}?status=CANCELLED" class="btn btn-outline-danger btn-sm px-2 py-1 mb-1 fw-bold" title="Cancel Booking"><i class="fa-solid fa-xmark"></i></a>
                                            </c:when>
                                            <c:when test="${b.status == 'CONFIRMED'}">
                                                <a href="/rental/booking/status/${b.id}?status=COMPLETED" class="btn btn-outline-primary btn-sm px-2 py-1 fw-bold" title="Mark as Completed"><i class="fa-solid fa-check-double"></i> Complete</a>
                                            </c:when>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty bookings}">
                                <tr>
                                    <td colspan="5" class="text-center py-5 text-muted">No rental bookings found.</td>
                                </tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
