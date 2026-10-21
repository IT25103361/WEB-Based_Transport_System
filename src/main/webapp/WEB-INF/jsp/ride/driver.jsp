<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Driver Dashboard — NEXT GO</title>
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- FontAwesome 6 -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- NEXT GO Shared Theme -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nextgo-theme.css">
    <script src="${pageContext.request.contextPath}/js/nextgo-theme.js"></script>
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f1f5f9;
        }
        /* Navbar */
        .premium-nav-driver {
            background: linear-gradient(135deg, #064e3b, #0f766e);
            border-bottom: 2px solid #10b981;
        }
        /* Cards */
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
        .card-header-success {
            background: linear-gradient(135deg, #10b981, #059669);
            border-radius: 20px 20px 0 0 !important;
            padding: 20px;
            border-bottom: none;
        }
        .card-header-dark {
            background: linear-gradient(135deg, #1e293b, #334155);
            border-radius: 20px 20px 0 0 !important;
            padding: 20px;
            border-bottom: none;
        }
        /* Tables */
        .premium-table { border-collapse: separate; border-spacing: 0 10px; }
        .premium-table thead th {
            border: none;
            background: transparent;
            color: #64748b;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.8rem;
            letter-spacing: 0.5px;
            padding-bottom: 10px;
        }
        .premium-table tbody tr {
            background: #fff;
            box-shadow: 0 2px 10px rgba(0,0,0,0.02);
            transition: transform 0.2s;
        }
        .premium-table tbody tr:hover {
            transform: scale(1.005);
            box-shadow: 0 5px 15px rgba(0,0,0,0.05);
        }
        .premium-table td { border: none; padding: 16px; vertical-align: middle; }
        .premium-table td:first-child { border-radius: 12px 0 0 12px; }
        .premium-table td:last-child { border-radius: 0 12px 12px 0; }

        .trip-route-box {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 10px 15px;
            transition: all 0.3s ease;
        }
        .trip-route-box:hover {
            border-color: #cbd5e1;
            box-shadow: 0 4px 12px rgba(0,0,0,0.03);
        }
        .btn-status-toggle {
            transition: all 0.3s ease;
        }
    </style>
</head>
<body class="bg-light">

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark premium-nav-driver shadow-sm sticky-top">
    <div class="container-fluid px-4 py-1">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/ride/home">
            <i class="fa-solid fa-id-badge text-warning me-2"></i> NEXT GO <span class="fw-light opacity-75">| Driver Portal</span>
        </a>
        <div class="d-flex align-items-center">
            <button type="button" class="nextgo-theme-toggle me-3" data-nextgo-theme-toggle title="Toggle light/dark theme" aria-label="Toggle light/dark theme">🌙</button>
            <span class="text-white small me-3 d-none d-md-inline">
                <i class="fa-solid fa-circle-user me-1 text-warning"></i> <c:out value="${driver.driverName}"/>
                <span class="badge bg-${driver.status == 'AVAILABLE' ? 'success' : 'secondary'} ms-1"><c:out value="${driver.status}"/></span>
            </span>
            <a href="${pageContext.request.contextPath}/ride/ride-hub" class="btn btn-outline-light btn-sm me-2 rounded-pill px-3">
                <i class="fa-solid fa-arrows-split-up-and-left me-1"></i> Switch Role
            </a>
            <a href="${pageContext.request.contextPath}/ride/driver/logout" class="btn btn-outline-warning btn-sm me-2 rounded-pill px-3">
                <i class="fa-solid fa-right-from-bracket me-1"></i> Logout
            </a>
            <a href="${pageContext.request.contextPath}/ride/home" class="btn btn-light btn-sm rounded-pill px-3 fw-semibold">
                <i class="fa-solid fa-house me-1"></i> Home
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid py-5 px-4 px-lg-5">
    <!-- Feedback Messages -->
    <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
            <i class="fa-solid fa-triangle-exclamation me-2"></i> <strong>Error:</strong> <c:out value="${errorMsg}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${not empty successMsg}">
        <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
            <i class="fa-solid fa-circle-check me-2"></i> <strong>Success:</strong> <c:out value="${successMsg}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <div class="row g-4">
        <!-- Left Column: Authenticated Driver's Profile & Own Availability -->
        <div class="col-lg-4">
            <div class="card premium-card mb-4">
                <div class="card-header card-header-success text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-id-card me-2"></i> My Driver Profile</h5>
                    <span class="badge bg-white text-dark small fw-bold">ID #${driver.id}</span>
                </div>
                <div class="card-body p-4">
                    <div class="text-center mb-4">
                        <div class="bg-success bg-opacity-10 text-success rounded-circle d-inline-flex p-3 mb-2">
                            <i class="fa-solid fa-user-tie fa-3x"></i>
                        </div>
                        <h4 class="fw-bold mb-1"><c:out value="${driver.driverName}"/></h4>
                        <span class="text-muted small"><i class="fa-solid fa-envelope me-1"></i> <c:out value="${sessionScope.loggedUser.email}"/></span>
                    </div>

                    <div class="list-group list-group-flush mb-4">
                        <div class="list-group-item d-flex justify-content-between align-items-center px-0">
                            <span class="text-muted small"><i class="fa-solid fa-phone me-2 text-secondary"></i> Phone Number</span>
                            <span class="fw-semibold"><c:out value="${driver.phoneNumber}"/></span>
                        </div>
                        <div class="list-group-item d-flex justify-content-between align-items-center px-0">
                            <span class="text-muted small"><i class="fa-solid fa-car me-2 text-secondary"></i> Vehicle Type</span>
                            <span class="badge bg-primary px-3 py-2"><c:out value="${driver.vehicleType}"/></span>
                        </div>
                        <div class="list-group-item d-flex justify-content-between align-items-center px-0">
                            <span class="text-muted small"><i class="fa-solid fa-hashtag me-2 text-secondary"></i> Vehicle Number</span>
                            <span class="fw-bold text-dark"><c:out value="${driver.vehicleNumber}"/></span>
                        </div>
                        <div class="list-group-item d-flex justify-content-between align-items-center px-0">
                            <span class="text-muted small"><i class="fa-solid fa-signal me-2 text-secondary"></i> Current Status</span>
                            <span class="badge bg-${driver.status == 'AVAILABLE' ? 'success' : 'secondary'} fs-6 px-3 py-2">
                                <c:out value="${driver.status}"/>
                            </span>
                        </div>
                    </div>

                    <!-- Driver's Own Availability Toggle -->
                    <form action="${pageContext.request.contextPath}/ride/driver/toggle-status" method="post">
                        <button type="submit" class="btn ${driver.status == 'AVAILABLE' ? 'btn-outline-secondary' : 'btn-success'} w-100 py-3 fw-bold btn-status-toggle shadow-sm">
                            <c:choose>
                                <c:when test="${driver.status == 'AVAILABLE'}">
                                    <i class="fa-solid fa-pause me-2"></i> Set Status to BUSY (Pause Requests)
                                </c:when>
                                <c:otherwise>
                                    <i class="fa-solid fa-play me-2"></i> Set Status to AVAILABLE (Go Online)
                                </c:otherwise>
                            </c:choose>
                        </button>
                    </form>
                    <p class="text-muted small mt-2 mb-0 text-center">
                        <i class="fa-solid fa-shield-halved text-success me-1"></i> You can only accept rides when your status is AVAILABLE.
                    </p>
                </div>
            </div>
        </div>

        <!-- Right Column: Available Ride Requests (Pending) & My Assigned Rides -->
        <div class="col-lg-8">
            <!-- 1. Available Ride Requests (Eligible Unassigned Pending Bookings) -->
            <div class="card premium-card mb-4">
                <div class="card-header card-header-dark text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-bell me-2 text-warning"></i> Available Ride Requests</h5>
                    <span class="badge bg-warning text-dark fw-bold">${pendingBookings.size()} Available</span>
                </div>
                <div class="card-body p-4 bg-light rounded-bottom-4">
                    <div class="table-responsive">
                        <table class="table premium-table align-middle w-100">
                            <thead>
                            <tr>
                                <th class="ps-3">ID / Passenger</th>
                                <th>Route & Vehicle/Fare</th>
                                <th>Status</th>
                                <th class="text-end pe-3">Action</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="b" items="${pendingBookings}">
                                <tr>
                                    <td class="ps-3">
                                        <span class="fw-bold text-success">#${b.id}</span><br>
                                        <span class="small text-muted"><c:out value="${b.passengerName}"/></span>
                                    </td>
                                    <td>
                                        <div class="trip-route-box mb-2" style="font-size: 0.85rem;">
                                            <div class="text-truncate mb-1" style="max-width: 280px;" title="${b.pickupLocation}">
                                                <i class="fa-solid fa-location-dot text-danger me-2"></i> <strong>From:</strong> <c:out value="${b.pickupLocation}"/>
                                            </div>
                                            <div class="text-truncate" style="max-width: 280px;" title="${b.dropLocation}">
                                                <i class="fa-solid fa-flag-checkered text-success me-2"></i> <strong>To:</strong> <c:out value="${b.dropLocation}"/>
                                            </div>
                                        </div>
                                        <div class="text-xs text-muted">
                                            <i class="fa-solid fa-car text-secondary me-1"></i> <c:out value="${b.vehicleType}"/> |
                                            <span class="fw-bold text-dark">LKR <c:out value="${b.fare != null ? b.fare : '0'}"/></span>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge bg-warning text-dark">PENDING</span><br>
                                        <span class="text-xs text-muted small">Awaiting driver</span>
                                    </td>
                                    <td class="text-end pe-3">
                                        <form action="${pageContext.request.contextPath}/ride/accept/${b.id}" method="post" class="d-inline">
                                            <button type="submit" class="btn btn-success btn-sm fw-bold px-3 shadow-sm"
                                                    ${driver.status != 'AVAILABLE' ? 'disabled title="Set your status to AVAILABLE to accept rides"' : ''}>
                                                <i class="fa-solid fa-check me-1"></i> Accept Ride
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty pendingBookings}">
                                <tr>
                                    <td colspan="4" class="text-center py-4 text-muted">
                                        <i class="fa-solid fa-clipboard-check fa-2x mb-2 d-block text-secondary opacity-50"></i>
                                        No unassigned ride requests available at this moment.
                                    </td>
                                </tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- 2. My Assigned Trips & Ride History -->
            <div class="card premium-card">
                <div class="card-header card-header-dark text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-taxi me-2 text-info"></i> My Assigned Rides & History</h5>
                    <span class="badge bg-info text-white fw-bold">${myTrips.size()} Total Rides</span>
                </div>
                <div class="card-body p-4 bg-light rounded-bottom-4">
                    <div class="table-responsive">
                        <table class="table premium-table align-middle w-100">
                            <thead>
                            <tr>
                                <th class="ps-3">ID / Passenger</th>
                                <th>Route & Vehicle/Fare</th>
                                <th>Trip Status</th>
                                <th class="text-end pe-3">Actions & Live Tracking</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="b" items="${myTrips}">
                                <tr>
                                    <td class="ps-3">
                                        <span class="fw-bold text-success">#${b.id}</span><br>
                                        <span class="small text-muted"><c:out value="${b.passengerName}"/></span>
                                    </td>
                                    <td>
                                        <div class="trip-route-box mb-2" style="font-size: 0.85rem;">
                                            <div class="text-truncate mb-1" style="max-width: 280px;" title="${b.pickupLocation}">
                                                <i class="fa-solid fa-location-dot text-danger me-2"></i> <strong>From:</strong> <c:out value="${b.pickupLocation}"/>
                                            </div>
                                            <div class="text-truncate" style="max-width: 280px;" title="${b.dropLocation}">
                                                <i class="fa-solid fa-flag-checkered text-success me-2"></i> <strong>To:</strong> <c:out value="${b.dropLocation}"/>
                                            </div>
                                        </div>
                                        <div class="text-xs text-muted">
                                            <i class="fa-solid fa-car text-secondary me-1"></i> <c:out value="${b.vehicleType}"/> |
                                            <span class="fw-bold text-dark">LKR <c:out value="${b.fare != null ? b.fare : '0'}"/></span>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge bg-${b.status == 'COMPLETED' || b.status == 'PAID' ? 'success' : (b.status == 'ACCEPTED' ? 'primary' : (b.status == 'IN_PROGRESS' ? 'warning text-dark' : 'secondary'))}">
                                            <c:out value="${b.status}"/>
                                        </span>
                                    </td>
                                    <td class="text-end pe-3">
                                        <c:choose>
                                            <c:when test="${b.status == 'ACCEPTED' || b.status == 'ARRIVED' || b.status == 'IN_PROGRESS'}">
                                                <div class="d-flex flex-column gap-1 align-items-end" style="min-width: 170px;">
                                                    <c:if test="${b.status == 'ACCEPTED'}">
                                                        <button onclick="updateStatus(${b.id}, 'ARRIVED')" class="btn btn-primary btn-sm fw-bold w-100">
                                                            <i class="fa-solid fa-car-side me-1"></i> I Have Arrived
                                                        </button>
                                                    </c:if>
                                                    <c:if test="${b.status == 'ARRIVED'}">
                                                        <button onclick="updateStatus(${b.id}, 'IN_PROGRESS')" class="btn btn-warning btn-sm fw-bold w-100 text-dark">
                                                            <i class="fa-solid fa-play me-1"></i> Start Trip
                                                        </button>
                                                    </c:if>
                                                    <c:if test="${b.status == 'IN_PROGRESS'}">
                                                        <a href="${pageContext.request.contextPath}/ride/complete/${b.id}" class="btn btn-success btn-sm fw-bold w-100">
                                                            <i class="fa-solid fa-check-double me-1"></i> Complete Trip
                                                        </a>
                                                    </c:if>
                                                    <div class="form-check form-switch mt-2 mb-1 ms-auto">
                                                        <input class="form-check-input" type="checkbox" id="demoMode-${b.id}">
                                                        <label class="form-check-label small text-muted" for="demoMode-${b.id}">Demo Simulation</label>
                                                    </div>
                                                    <button id="trackBtn-${b.id}" onclick="toggleTracking(${b.id})" class="btn btn-info btn-sm fw-bold w-100 text-white mt-1">
                                                        <i class="fa-solid fa-location-arrow me-1"></i> Start Tracking
                                                    </button>
                                                </div>
                                            </c:when>
                                            <c:when test="${b.status == 'COMPLETED'}">
                                                <span class="badge bg-success bg-opacity-10 text-success border border-success p-2 small">
                                                    <i class="fa-solid fa-clock-rotate-left me-1"></i> Awaiting Passenger Payment
                                                </span>
                                            </c:when>
                                            <c:when test="${b.status == 'PAID'}">
                                                <span class="badge bg-success p-2 small">
                                                    <i class="fa-solid fa-circle-check me-1"></i> Paid in Full
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/ride/delete/${b.id}" class="btn btn-outline-danger btn-sm fw-bold">
                                                    <i class="fa-solid fa-trash me-1"></i> Delete
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty myTrips}">
                                <tr>
                                    <td colspan="4" class="text-center py-4 text-muted">
                                        <i class="fa-solid fa-car-tunnel fa-2x mb-2 d-block text-secondary opacity-50"></i>
                                        You have no assigned rides yet. Accept a pending request above to begin!
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
<script>
    function updateStatus(bookingId, status) {
        fetch('${pageContext.request.contextPath}/ride/api/booking/' + bookingId + '/status', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: status })
        }).then(res => {
            if (res.ok) {
                location.reload();
            } else {
                alert("Failed to update status. Please make sure you are authorized for this ride.");
            }
        }).catch(err => {
            console.error("Error updating status:", err);
            alert("Network error updating status.");
        });
    }

    let activeWatchId = null;
    let trackingBookingId = null;
    let demoInterval = null;
    let demoLat = 6.9271;
    let demoLng = 79.8612;

    function toggleTracking(bookingId) {
        const btn = document.getElementById('trackBtn-' + bookingId);
        const demoCheckbox = document.getElementById('demoMode-' + bookingId);
        const isDemo = demoCheckbox ? demoCheckbox.checked : false;

        if (activeWatchId !== null || demoInterval !== null) {
            // Stop tracking
            if (activeWatchId !== null) navigator.geolocation.clearWatch(activeWatchId);
            if (demoInterval !== null) clearInterval(demoInterval);
            activeWatchId = null;
            demoInterval = null;
            trackingBookingId = null;
            btn.innerHTML = '<i class="fa-solid fa-location-arrow me-1"></i> Start Tracking';
            btn.classList.replace('btn-danger', 'btn-info');
            return;
        }

        // Start tracking
        btn.innerHTML = '<i class="fa-solid fa-stop me-1"></i> Stop Tracking' + (isDemo ? ' [DEMO]' : '');
        btn.classList.replace('btn-info', 'btn-danger');
        trackingBookingId = bookingId;

        if (isDemo) {
            console.log("Starting Demo Mode Tracking");
            demoInterval = setInterval(() => {
                demoLat += 0.0005;
                demoLng += 0.0005;
                sendLocation(demoLat, demoLng);
            }, 3000);
        } else {
            if (!navigator.geolocation) {
                alert("Geolocation is not supported by your browser.");
                toggleTracking(bookingId); // Revert UI
                return;
            }

            activeWatchId = navigator.geolocation.watchPosition(
                position => sendLocation(position.coords.latitude, position.coords.longitude),
                err => {
                    console.error("Location error:", err);
                    if (err.code === 1) alert("Please allow location access to share your location.");
                },
                { enableHighAccuracy: true, maximumAge: 0, timeout: 5000 }
            );
        }
    }

    function sendLocation(lat, lng) {
        fetch('${pageContext.request.contextPath}/ride/api/location/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ lat: lat, lng: lng })
        }).catch(err => console.error("Error sending location", err));
    }
</script>
</body>
</html>