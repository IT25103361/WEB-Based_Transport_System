<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Driver Portal - NEXT GO</title>
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark bg-success shadow-sm">
    <div class="container-fluid px-4">
        <a class="navbar-brand fw-bold" href="/ride/home">
            <i class="fa-solid fa-id-badge text-warning me-2"></i> NEXT GO — Driver Portal
        </a>
        <div class="d-flex">
            <a href="/ride/ride-hub" class="btn btn-outline-light btn-sm me-2">
                <i class="fa-solid fa-arrows-split-up-and-left me-1"></i> Switch Role
            </a>
            <a href="/ride/home" class="btn btn-outline-light btn-sm">
                <i class="fa-solid fa-house me-1"></i> Home
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid py-4 px-4">
    <div class="row g-4">
        <!-- Left Column: Register Driver Profile & Registered Drivers List -->
        <div class="col-lg-4">
            <div class="card shadow-sm border-0 rounded-4 mb-4">
                <div class="card-header bg-success text-white py-3 rounded-top-4">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-user-plus me-2"></i> Register Driver Profile</h5>
                </div>
                <div class="card-body p-4">
                    <form action="/ride/driver/register" method="post">
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Driver Name</label>
                            <input type="text" name="driverName" class="form-control" placeholder="e.g. Sunil Perera" required />
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Vehicle Number</label>
                            <input type="text" name="vehicleNumber" class="form-control" placeholder="e.g. WP-ABC-1234" required />
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Vehicle Type</label>
                            <select name="vehicleType" class="form-select" required>
                                <option value="">Select Vehicle Type</option>
                                <option value="Car">Car</option>
                                <option value="Van">Van</option>
                                <option value="Bike">Bike</option>
                                <option value="Tuk">Tuk</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Phone Number</label>
                            <input type="text" name="phoneNumber" class="form-control" placeholder="0771234567" required />
                        </div>
                        <button type="submit" class="btn btn-success w-100 py-2 fw-bold shadow-sm">
                            <i class="fa-solid fa-plus me-1"></i> Register Profile
                        </button>
                    </form>
                </div>
            </div>

            <!-- Drivers Status Box with Toggle -->
            <div class="card shadow-sm border-0 rounded-4">
                <div class="card-header bg-dark text-white py-2 rounded-top-4 d-flex justify-content-between align-items-center">
                    <h6 class="mb-0 fw-bold"><i class="fa-solid fa-users me-2"></i> Drivers & Status</h6>
                    <span class="text-xs text-muted small text-white-50">Click badge to toggle</span>
                </div>
                <div class="card-body p-3">
                    <ul class="list-group list-group-flush small">
                        <c:forEach var="d" items="${drivers}">
                            <li class="list-group-item d-flex justify-content-between align-items-center px-0">
                                <div>
                                    <span class="fw-bold">${d.driverName}</span><br>
                                    <span class="text-muted" style="font-size: 0.75rem;">${d.vehicleType} (${d.vehicleNumber})</span>
                                </div>
                                <a href="/ride/driver/toggle/${d.id}" class="text-decoration-none" title="Click to toggle status">
                                    <span class="badge bg-${d.status == 'AVAILABLE' ? 'success' : 'secondary'}">
                                        ${d.status} <i class="fa-solid fa-rotate ms-1"></i>
                                    </span>
                                </a>
                            </li>
                        </c:forEach>
                        <c:if test="${empty drivers}">
                            <li class="list-group-item text-center text-muted border-0">No drivers registered yet.</li>
                        </c:if>
                    </ul>
                </div>
            </div>
        </div>

        <!-- Right Column: Live Ride Requests & Active Trips -->
        <div class="col-lg-8">
            <div class="card shadow-sm border-0 rounded-4">
                <div class="card-header bg-dark text-white py-3 rounded-top-4">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-taxi me-2"></i> Live Ride Requests & Active Trips</h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                            <tr>
                                <th class="ps-3">ID / Passenger</th>
                                <th>Route & Vehicle/Fare</th>
                                <th>Status / Driver</th>
                                <th class="text-end pe-3">Action / Accept Ride</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="b" items="${bookings}">
                                <tr>
                                    <td class="ps-3">
                                        <span class="fw-bold text-success">#${b.id}</span><br>
                                        <span class="small text-muted">${b.passengerName}</span>
                                    </td>
                                    <td>
                                        <div class="p-2 bg-white border rounded-3 shadow-sm mb-1" style="font-size: 0.85rem;">
                                            <div class="text-truncate mb-1" style="max-width: 280px;" title="${b.pickupLocation}">
                                                <i class="fa-solid fa-location-dot text-danger me-1"></i> <strong>From:</strong> ${b.pickupLocation}
                                            </div>
                                            <div class="text-truncate" style="max-width: 280px;" title="${b.dropLocation}">
                                                <i class="fa-solid fa-flag-checkered text-success me-1"></i> <strong>To:</strong> ${b.dropLocation}
                                            </div>
                                        </div>
                                        <div class="text-xs text-muted">
                                            <i class="fa-solid fa-car text-secondary me-1"></i> ${b.vehicleType} |
                                            <span class="fw-bold text-dark">LKR ${b.fare != null ? b.fare : '0'}</span>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge bg-${b.status == 'COMPLETED' ? 'success' : (b.status == 'ACCEPTED' ? 'primary' : 'warning text-dark')}">
                                                ${b.status}
                                        </span><br>
                                        <span class="text-xs text-muted small">
                                                ${b.driver != null ? b.driver.driverName : 'Waiting for Driver'}
                                        </span>
                                    </td>
                                    <td class="text-end pe-3">
                                        <c:choose>
                                            <c:when test="${b.status == 'PENDING'}">
                                                <form action="/ride/accept/${b.id}" method="post" class="d-inline-flex align-items-center gap-1">
                                                    <select name="driverId" class="form-select form-select-sm" style="width: 140px;" required>
                                                        <option value="">Select Yourself</option>
                                                        <c:forEach var="d" items="${drivers}">
                                                            <c:if test="${d.status == 'AVAILABLE'}">
                                                                <option value="${d.id}">${d.driverName} (${d.vehicleType})</option>
                                                            </c:if>
                                                        </c:forEach>
                                                    </select>
                                                    <button type="submit" class="btn btn-success btn-sm fw-bold">Accept</button>
                                                </form>
                                            </c:when>
                                            <c:when test="${b.status == 'ACCEPTED'}">
                                                <a href="/ride/complete/${b.id}" class="btn btn-outline-success btn-sm fw-bold">
                                                    <i class="fa-solid fa-check-double me-1"></i> Complete Ride
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="/ride/delete/${b.id}" class="btn btn-outline-danger btn-sm fw-bold">
                                                    <i class="fa-solid fa-trash me-1"></i> Delete
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty bookings}">
                                <tr>
                                    <td colspan="4" class="text-center py-4 text-muted">No ride requests available.</td>
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