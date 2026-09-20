<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cab & Ride Booking System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
    <div class="container-fluid px-4">
        <a class="navbar-brand fw-bold" href="#">
            <i class="fa-solid fa-taxi text-warning me-2"></i> NextGo Ride Booking System
        </a>
        <span class="text-white-50 small"><i class="fa-solid fa-circle text-success me-1"></i> Port: 8081</span>
    </div>
</nav>

<div class="container py-4">
    <div class="row g-4">
        <!-- Left Side: Forms (Register Driver & Book Ride) -->
        <div class="col-lg-4">

            <!-- 1. Register Driver Form -->
            <div class="card shadow-sm border-0 mb-4">
                <div class="card-header bg-primary text-white py-3">
                    <h5 class="mb-0"><i class="fa-solid fa-user-tie me-2"></i> Register Driver</h5>
                </div>
                <div class="card-body p-3">
                    <form action="/add-driver" method="post">
                        <div class="mb-2">
                            <label class="form-label small fw-semibold text-muted">Driver Name</label>
                            <input type="text" name="name" class="form-control form-control-sm" placeholder="e.g. Amal Perera" required />
                        </div>
                        <div class="mb-2">
                            <label class="form-label small fw-semibold text-muted">Vehicle Number</label>
                            <input type="text" name="vehicleNumber" class="form-control form-control-sm" placeholder="e.g. WP-CA-1234" required />
                        </div>
                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-muted">Phone</label>
                            <input type="text" name="phone" class="form-control form-control-sm" placeholder="0712345678" required />
                        </div>
                        <button type="submit" class="btn btn-primary btn-sm w-100 fw-semibold">
                            <i class="fa-solid fa-plus me-1"></i> Save Driver
                        </button>
                    </form>
                </div>
            </div>

            <!-- 2. Book Ride Form -->
            <div class="card shadow-sm border-0">
                <div class="card-header bg-success text-white py-3">
                    <h5 class="mb-0"><i class="fa-solid fa-car-side me-2"></i> Book a Ride</h5>
                </div>
                <div class="card-body p-3">
                    <form action="/book" method="post">
                        <div class="mb-2">
                            <label class="form-label small fw-semibold text-muted">Passenger Name</label>
                            <input type="text" name="passengerName" class="form-control form-control-sm" placeholder="Enter name" required />
                        </div>
                        <div class="mb-2">
                            <label class="form-label small fw-semibold text-muted">Pickup Location</label>
                            <input type="text" name="pickupLocation" class="form-control form-control-sm" placeholder="Pickup spot" required />
                        </div>
                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-muted">Drop Location</label>
                            <input type="text" name="dropLocation" class="form-control form-control-sm" placeholder="Destination" required />
                        </div>
                        <button type="submit" class="btn btn-success btn-sm w-100 fw-semibold">
                            <i class="fa-solid fa-paper-plane me-1"></i> Request Ride
                        </button>
                    </form>
                </div>
            </div>

        </div>

        <!-- Right Side: Booking List Table -->
        <div class="col-lg-8">
            <div class="card shadow-sm border-0">
                <div class="card-header bg-dark text-white py-3">
                    <h5 class="mb-0"><i class="fa-solid fa-list-check me-2"></i> Ride Requests & Driver Assignments</h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                            <tr>
                                <th class="ps-3">ID</th>
                                <th>Passenger</th>
                                <th>Route</th>
                                <th>Status / Driver</th>
                                <th class="text-center">Action</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="b" items="${bookings}">
                                <tr>
                                    <td class="ps-3 fw-bold text-muted">#${b.id}</td>
                                    <td>
                                        <div class="fw-semibold">${b.passengerName}</div>
                                    </td>
                                    <td>
                                        <small class="text-danger"><i class="fa-solid fa-location-dot"></i> ${b.pickupLocation}</small><br>
                                        <small class="text-success"><i class="fa-solid fa-flag-checkered"></i> ${b.dropLocation}</small>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${b.status == 'APPROVED'}">
                                                <span class="badge bg-success mb-1">APPROVED</span><br>
                                                <small class="text-muted"><i class="fa-solid fa-user-shield"></i> ${b.driver.name} (${b.driver.vehicleNumber})</small>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-warning text-dark">PENDING</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <c:choose>
                                            <c:when test="${b.status == 'PENDING'}">
                                                <!-- Form for Driver to Approve -->
                                                <form action="/approve/${b.id}" method="post" class="d-inline-flex align-items-center gap-1">
                                                    <select name="driverId" class="form-select form-select-sm" style="width: 130px;" required>
                                                        <option value="">Select Driver</option>
                                                        <c:forEach var="d" items="${drivers}">
                                                            <option value="${d.id}">${d.name}</option>
                                                        </c:forEach>
                                                    </select>
                                                    <button type="submit" class="btn btn-sm btn-success px-2 py-1" title="Approve">
                                                        <i class="fa-solid fa-check"></i>
                                                    </button>
                                                </form>
                                            </c:when>
                                        </c:choose>
                                        <a href="/cancel/${b.id}" class="btn btn-sm btn-outline-danger px-2 py-1 ms-1" onclick="return confirm('Cancel this ride?');" title="Cancel">
                                            <i class="fa-solid fa-xmark"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty bookings}">
                                <tr>
                                    <td colspan="5" class="text-center py-5 text-muted">
                                        No bookings available right now. Register a driver and book a ride!
                                    </td>
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