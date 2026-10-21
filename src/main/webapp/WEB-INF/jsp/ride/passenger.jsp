<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Passenger Portal - NEXT GO</title>
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <!-- Leaflet Map CSS -->
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
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
        .premium-nav {
            background: linear-gradient(135deg, #0f172a, #1e293b);
            border-bottom: 2px solid #3b82f6;
        }
        /* Cards & Glassmorphism */
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
        .card-header-primary {
            background: linear-gradient(135deg, #3b82f6, #2563eb);
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
        /* Inputs & Buttons */
        .form-control, .form-select {
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 12px 16px;
            background-color: #f8fafc;
            transition: all 0.2s;
        }
        .form-control:focus, .form-select:focus {
            background-color: #fff;
            border-color: #3b82f6;
            box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
        }
        .btn-premium {
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            border: none;
            border-radius: 12px;
            color: #fff;
            font-weight: 600;
            transition: all 0.3s;
            box-shadow: 0 4px 15px rgba(37, 99, 235, 0.3);
        }
        .btn-premium:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(37, 99, 235, 0.4);
            color: #fff;
        }
        /* Vehicle Options */
        .vehicle-option {
            cursor: pointer;
            border: 2px solid transparent;
            background: #f8fafc;
            border-radius: 16px;
            transition: all 0.3s ease;
            box-shadow: 0 2px 8px rgba(0,0,0,0.02);
        }
        .vehicle-option:hover {
            background: #fff;
            transform: translateY(-3px);
            box-shadow: 0 8px 20px rgba(0,0,0,0.06);
        }
        .vehicle-option.selected {
            border-color: #3b82f6;
            background: #eff6ff;
            box-shadow: 0 8px 20px rgba(59, 130, 246, 0.15);
        }
        /* Map */
        #map { height: 260px; border-radius: 16px; z-index: 1; border: 2px solid #e2e8f0; }
        /* Suggestions */
        .suggestions-box {
            position: absolute;
            top: 100%; left: 0; right: 0; z-index: 9999;
            background: rgba(255, 255, 255, 0.98);
            backdrop-filter: blur(10px);
            border: 1px solid #e2e8f0;
            border-top: none;
            max-height: 200px;
            overflow-y: auto;
            box-shadow: 0 10px 25px rgba(0,0,0,0.1);
            border-radius: 0 0 12px 12px;
        }
        .suggestion-item {
            padding: 12px 16px; font-size: 0.9rem; cursor: pointer;
            border-bottom: 1px solid #f1f5f9; transition: background 0.2s;
        }
        .suggestion-item:hover { background-color: #f1f5f9; }
        
        /* Table Styles */
        .premium-table { border-collapse: separate; border-spacing: 0 10px; }
        .premium-table thead th { border: none; background: transparent; color: #64748b; font-weight: 600; text-transform: uppercase; font-size: 0.8rem; letter-spacing: 0.5px; padding-bottom: 10px; }
        .premium-table tbody tr { background: #fff; box-shadow: 0 2px 10px rgba(0,0,0,0.02); transition: transform 0.2s; }
        .premium-table tbody tr:hover { transform: scale(1.01); box-shadow: 0 5px 15px rgba(0,0,0,0.05); }
        .premium-table td { border: none; padding: 16px; vertical-align: middle; }
        .premium-table td:first-child { border-radius: 12px 0 0 12px; }
        .premium-table td:last-child { border-radius: 0 12px 12px 0; }
    </style>
</head>
<body class="bg-light">

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark premium-nav shadow-sm">
    <div class="container-fluid px-4 py-1">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/ride/home">
            <i class="fa-solid fa-taxi text-warning me-2"></i> NEXT GO <span class="fw-light opacity-75">| Passenger Portal</span>
        </a>
        <div class="d-flex align-items-center">
            <button type="button" class="nextgo-theme-toggle me-3" data-nextgo-theme-toggle title="Toggle light/dark theme" aria-label="Toggle light/dark theme">🌙</button>
            <span class="text-white-50 small me-3 d-none d-md-inline">
                <i class="fa-solid fa-user-circle me-1 text-info"></i> <c:out value="${sessionScope.loggedUser.fullName}"/>
            </span>
            <a href="${pageContext.request.contextPath}/ride/ride-hub" class="btn btn-outline-light btn-sm me-2 rounded-pill px-3">
                <i class="fa-solid fa-arrows-split-up-and-left me-1"></i> Switch Role
            </a>
            <a href="${pageContext.request.contextPath}/ride/logout" class="btn btn-outline-warning btn-sm me-2 rounded-pill px-3">
                <i class="fa-solid fa-right-from-bracket me-1"></i> Logout
            </a>
            <a href="${pageContext.request.contextPath}/ride/home" class="btn btn-light btn-sm rounded-pill px-3 fw-semibold">
                <i class="fa-solid fa-house me-1"></i> Home
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid py-5 px-4 px-lg-5">
    <div class="row g-5">
        <!-- Left Column: Booking Form & Map -->
        <div class="col-lg-5">
            <div class="card premium-card">
                <div class="card-header card-header-primary text-white">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-map-location-dot me-2"></i> Book Your Ride</h5>
                </div>
                <div class="card-body p-4 p-md-5">
                    <form:form action="/ride/book" modelAttribute="booking" method="post" id="bookingForm">
                        <!-- Passenger Name -->
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Passenger Name</label>
                            <form:input path="passengerName" cssClass="form-control" placeholder="e.g. Dinuja" required="true" />
                        </div>

                        <!-- Pickup Location (Auto-Suggest) -->
                        <div class="mb-3 position-relative">
                            <label class="form-label fw-semibold small text-muted">Pickup Location</label>
                            <form:input path="pickupLocation" id="pickup" cssClass="form-control" placeholder="Type pickup location..." autocomplete="off" oninput="onTypeSearch('pickup')" required="true" />
                            <div id="pickup-suggestions" class="suggestions-box d-none"></div>
                        </div>

                        <!-- Drop Location (Auto-Suggest) -->
                        <div class="mb-3 position-relative">
                            <label class="form-label fw-semibold small text-muted">Drop Location</label>
                            <form:input path="dropLocation" id="destination" cssClass="form-control" placeholder="Type destination..." autocomplete="off" oninput="onTypeSearch('destination')" required="true" />
                            <div id="destination-suggestions" class="suggestions-box d-none"></div>
                        </div>

                        <!-- Leaflet Map -->
                        <div class="mb-3">
                            <div id="map"></div>
                            <div class="text-center mt-1 text-muted small" id="distanceInfo">Road Distance: 0.00 km</div>
                        </div>

                        <!-- Vehicle Selection & Exact Per-KM Rates -->
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Select Vehicle & Fare</label>
                            <div class="row g-2">
                                <div class="col-3 text-center">
                                    <div class="vehicle-option p-2 selected" onclick="selectVehicle('Bike', fareBike)">
                                        <i class="fa-solid fa-motorcycle fa-xl text-primary mb-1"></i>
                                        <div class="fw-bold" style="font-size: 0.75rem;">Bike</div>
                                        <div class="text-muted" style="font-size: 0.7rem;" id="fare-Bike-text">LKR 0</div>
                                    </div>
                                </div>
                                <div class="col-3 text-center">
                                    <div class="vehicle-option p-2" onclick="selectVehicle('Three-Wheeler', fareWheel)">
                                        <i class="fa-solid fa-taxi fa-xl text-warning mb-1"></i>
                                        <div class="fw-bold" style="font-size: 0.75rem;">Wheel</div>
                                        <div class="text-muted" style="font-size: 0.7rem;" id="fare-Wheel-text">LKR 0</div>
                                    </div>
                                </div>
                                <div class="col-3 text-center">
                                    <div class="vehicle-option p-2" onclick="selectVehicle('Car', fareCar)">
                                        <i class="fa-solid fa-car fa-xl text-success mb-1"></i>
                                        <div class="fw-bold" style="font-size: 0.75rem;">Car</div>
                                        <div class="text-muted" style="font-size: 0.7rem;" id="fare-Car-text">LKR 0</div>
                                    </div>
                                </div>
                                <div class="col-3 text-center">
                                    <div class="vehicle-option p-2" onclick="selectVehicle('Van', fareVan)">
                                        <i class="fa-solid fa-shuttle-van fa-xl text-danger mb-1"></i>
                                        <div class="fw-bold" style="font-size: 0.75rem;">Van</div>
                                        <div class="text-muted" style="font-size: 0.7rem;" id="fare-Van-text">LKR 0</div>
                                    </div>
                                </div>
                            </div>
                            <!-- Hidden Fields -->
                            <form:hidden path="vehicleType" id="vehicleType" value="Bike" />
                            <form:hidden path="fare" id="fareValue" value="0" />
                            <form:hidden path="pickupLat" id="pickupLat" />
                            <form:hidden path="pickupLng" id="pickupLng" />
                            <form:hidden path="dropLat" id="dropLat" />
                            <form:hidden path="dropLng" id="dropLng" />
                        </div>

                        <!-- Payment Method -->
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Payment Method</label>
                            <form:select path="paymentMethod" cssClass="form-select">
                                <form:option value="Cash" label="Cash"/>
                                <form:option value="Card" label="Card"/>
                            </form:select>
                        </div>

                        <button type="submit" class="btn btn-premium w-100 py-3 mt-2 text-uppercase letter-spacing-1">
                            <i class="fa-solid fa-paper-plane me-2"></i> Request Booking
                        </button>
                    </form:form>
                </div>
            </div>
        </div>

        <!-- Right Column: Active Rides & History -->
        <div class="col-lg-7">
            <div class="card premium-card h-100">
                <div class="card-header card-header-dark text-white">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-list-check me-2"></i> Active Rides & History</h5>
                </div>
                <div class="card-body p-4 bg-light rounded-bottom-4">
                    <div class="table-responsive">
                        <table class="table premium-table align-middle w-100">
                            <thead>
                            <tr>
                                <th class="ps-3">ID / Passenger</th>
                                <th>Route, Vehicle & Fare</th>
                                <th>Status / Driver</th>
                                <th class="text-end pe-3">Action / Payment</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="b" items="${bookings}">
                                <tr>
                                    <td class="ps-3">
                                        <span class="fw-bold text-primary">#${b.id}</span><br>
                                        <span class="text-muted small">${b.passengerName}</span>
                                    </td>
                                    <td>
                                        <div class="small"><i class="fa-solid fa-location-dot text-danger me-1"></i> ${b.pickupLocation}</div>
                                        <div class="small"><i class="fa-solid fa-flag-checkered text-success me-1"></i> ${b.dropLocation}</div>
                                        <div class="text-xs text-muted mt-1">
                                            <i class="fa-solid fa-car text-secondary me-1"></i> ${b.vehicleType} |
                                            <c:choose>
                                                <c:when test="${b.status == 'PAID'}">
                                                    <span class="fw-bold text-secondary text-decoration-line-through">LKR ${b.fare}</span>
                                                    <span class="fw-bold text-success ms-1">LKR ${b.finalFare != null ? b.finalFare : b.fare}</span>
                                                    <c:if test="${b.appliedOffer != null && b.appliedOffer != 'NONE'}">
                                                        <span class="badge bg-warning text-dark ms-1" style="font-size: 0.65rem;"><i class="fa-solid fa-tag me-1"></i>${b.appliedOffer}</span>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="fw-bold text-dark">LKR ${b.fare != null ? b.fare : '0'}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge bg-${b.status == 'COMPLETED' ? 'success' : (b.status == 'ACCEPTED' ? 'primary' : (b.status == 'CANCELLED' ? 'danger' : 'warning text-dark'))}">
                                                ${b.status}
                                        </span><br>
                                        <span class="text-xs text-muted small">
                                                ${b.driver != null ? b.driver.driverName : 'No Driver Assigned'}
                                        </span>
                                    </td>
                                    <td class="text-end pe-3">
                                        <c:choose>
                                            <c:when test="${b.status == 'PENDING' || b.status == 'ACCEPTED' || b.status == 'ARRIVED' || b.status == 'IN_PROGRESS'}">
                                                <div class="d-flex flex-column gap-1">
                                                <c:if test="${b.status == 'PENDING' || b.status == 'ACCEPTED'}">
                                                    <a href="/ride/cancel/${b.id}" class="btn btn-outline-danger btn-sm fw-bold w-100" onclick="return confirm('Are you sure you want to cancel this ride?');">
                                                        <i class="fa-solid fa-ban me-1"></i> Cancel
                                                    </a>
                                                </c:if>
                                                <c:if test="${b.status == 'ACCEPTED' || b.status == 'ARRIVED' || b.status == 'IN_PROGRESS'}">
                                                    <button onclick="openTrackingModal(${b.id})" class="btn btn-info btn-sm fw-bold text-white w-100">
                                                        <i class="fa-solid fa-location-crosshairs me-1"></i> Track Ride
                                                    </button>
                                                </c:if>
                                                </div>
                                            </c:when>
                                            <c:when test="${b.status == 'COMPLETED'}">
                                                <a href="/ride/payment/${b.id}" class="btn btn-success btn-sm fw-bold mb-1">
                                                    <i class="fa-solid fa-credit-card me-1"></i> Pay Now
                                                </a><br>
                                            </c:when>
                                        </c:choose>
                                        <span class="badge bg-secondary">${b.paymentMethod != null ? b.paymentMethod : 'Cash'}</span>
                                        <c:if test="${b.status == 'PAID'}">
                                            <span class="badge bg-success mt-1">Paid</span>
                                            <c:choose>
                                                <c:when test="${b.rating != null}">
                                                    <div class="small text-warning mt-1" title="${b.reviewComment}">
                                                        <c:forEach begin="1" end="${b.rating}">★</c:forEach><c:forEach begin="${b.rating + 1}" end="5">☆</c:forEach>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="button" class="btn btn-outline-primary btn-sm mt-1 w-100" style="font-size: 0.72rem; padding: 2px 6px;" onclick="openReviewModal(${b.id})">
                                                        <i class="fa-regular fa-star me-1"></i> Review
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty bookings}">
                                <tr>
                                    <td colspan="4" class="text-center py-4 text-muted">No bookings found yet.</td>
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

<!-- Live Tracking Modal -->
<div class="modal fade" id="trackingModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content premium-card">
            <div class="modal-header card-header-primary text-white">
                <h5 class="modal-title fw-bold"><i class="fa-solid fa-location-arrow me-2"></i> Live Ride Tracking</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close" onclick="closeTrackingModal()"></button>
            </div>
            <div class="modal-body p-4 bg-light">
                <div class="row mb-3">
                    <div class="col-md-6">
                        <div class="fw-bold mb-1">Driver Details:</div>
                        <div id="track-driverName" class="text-primary fw-bold">Waiting for driver...</div>
                        <div id="track-vehicleInfo" class="text-muted small"></div>
                        <div id="track-driverPhone" class="small mt-1"></div>
                    </div>
                    <div class="col-md-6 text-md-end">
                        <div class="fw-bold mb-1">Ride Status:</div>
                        <span id="track-rideStatus" class="badge bg-secondary mb-1">...</span>
                        <div id="track-statusText" class="small fw-bold mt-1 text-danger">Waiting for driver location...</div>
                    </div>
                </div>
                <div id="track-map" style="height: 350px; border-radius: 12px; border: 2px solid #e2e8f0; z-index: 1;"></div>
            </div>
        </div>
    </div>
</div>

<!-- Review Modal -->
<div class="modal fade" id="reviewModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content premium-card">
            <div class="modal-header card-header-primary text-white">
                <h5 class="modal-title fw-bold"><i class="fa-solid fa-star me-2"></i> Rate & Review Ride</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="reviewForm" method="post" action="">
                <div class="modal-body p-4">
                    <div class="mb-3">
                        <label class="form-label fw-semibold small text-muted">Rating</label>
                        <select name="rating" class="form-select" required>
                            <option value="5">⭐⭐⭐⭐⭐ (5 - Excellent)</option>
                            <option value="4">⭐⭐⭐⭐ (4 - Very Good)</option>
                            <option value="3">⭐⭐⭐ (3 - Good)</option>
                            <option value="2">⭐⭐ (2 - Fair)</option>
                            <option value="1">⭐ (1 - Poor)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold small text-muted">Comments / Feedback</label>
                        <textarea name="reviewComment" class="form-control" rows="3" placeholder="Share your experience..."></textarea>
                    </div>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-premium rounded-pill px-4">Submit Review</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Leaflet & OpenStreetMap Nominatim Auto-Suggest & Routing Script -->
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<script>
    var map = L.map('map').setView([6.9271, 79.8612], 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    let pickupCoord = null;
    let destCoord = null;
    let pickupMarker = null;
    let destMarker = null;
    let routeLayer = null;

    let roadDistanceKm = 0;
    let selectedVehicleType = 'Bike';

    let fareBike = 0;
    let fareWheel = 0;
    let fareCar = 0;
    let fareVan = 0;

    let typingTimer;

    function onTypeSearch(type) {
        clearTimeout(typingTimer);
        let inputField = document.getElementById(type === 'pickup' ? 'pickup' : 'destination');
        let query = inputField.value.trim();
        let suggestionBox = document.getElementById(type === 'pickup' ? 'pickup-suggestions' : 'destination-suggestions');

        if (query.length < 3) {
            suggestionBox.innerHTML = '';
            suggestionBox.classList.add('d-none');
            return;
        }

        typingTimer = setTimeout(function() {
            let url = 'https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(query) + '&countrycodes=lk&limit=5';

            fetch(url)
                .then(response => response.json())
                .then(data => {
                    suggestionBox.innerHTML = '';
                    if (data && data.length > 0) {
                        suggestionBox.classList.remove('d-none');
                        data.forEach(item => {
                            let div = document.createElement('div');
                            div.className = 'suggestion-item';
                            div.innerText = item.display_name;
                            div.onclick = function() {
                                inputField.value = item.display_name;
                                suggestionBox.classList.add('d-none');
                                setLocationAndRoute(type, parseFloat(item.lat), parseFloat(item.lon), item.display_name);
                            };
                            suggestionBox.appendChild(div);
                        });
                    } else {
                        suggestionBox.classList.add('d-none');
                    }
                })
                .catch(err => console.error("Nominatim error:", err));
        }, 300);
    }

    function setLocationAndRoute(type, lat, lon, name) {
        if (type === 'pickup') {
            pickupCoord = [lat, lon];
            document.getElementById('pickupLat').value = lat;
            document.getElementById('pickupLng').value = lon;
            if (pickupMarker) map.removeLayer(pickupMarker);
            pickupMarker = L.marker([lat, lon]).addTo(map).bindPopup("Pickup: " + name).openPopup();
            map.setView([lat, lon], 13);
        } else {
            destCoord = [lat, lon];
            document.getElementById('dropLat').value = lat;
            document.getElementById('dropLng').value = lon;
            if (destMarker) map.removeLayer(destMarker);
            destMarker = L.marker([lat, lon], {icon: L.icon({
                    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.ico',
                    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
                    iconSize: [25, 41], iconAnchor: [12, 41]
                })}).addTo(map).bindPopup("Drop: " + name).openPopup();
        }

        if (pickupCoord && destCoord) {
            fetchOSRMRoute();
        }
    }

    function fetchOSRMRoute() {
        let osrmUrl = 'https://router.project-osrm.org/route/v1/driving/' +
            pickupCoord[1] + ',' + pickupCoord[0] + ';' +
            destCoord[1] + ',' + destCoord[0] + '?overview=full&geometries=geojson';

        fetch(osrmUrl)
            .then(response => response.json())
            .then(data => {
                if (data.routes && data.routes.length > 0) {
                    let route = data.routes[0];
                    roadDistanceKm = route.distance / 1000;

                    document.getElementById('distanceInfo').innerText = "Road Distance: " + roadDistanceKm.toFixed(2) + " km";

                    if (routeLayer) map.removeLayer(routeLayer);
                    routeLayer = L.geoJSON(route.geometry, {
                        style: { color: '#0d6efd', weight: 5, opacity: 0.8 }
                    }).addTo(map);

                    map.fitBounds(routeLayer.getBounds(), {padding: [50, 50]});

                    calculateExactFares();
                }
            })
            .catch(err => console.error("OSRM error:", err));
    }

    function calculateExactFares() {
        fareBike  = Math.round(roadDistanceKm * 60);
        fareWheel = Math.round(roadDistanceKm * 120);
        fareCar   = Math.round(roadDistanceKm * 170);
        fareVan   = Math.round(roadDistanceKm * 210);

        if (roadDistanceKm < 1) {
            fareBike = 60;
            fareWheel = 120;
            fareCar = 170;
            fareVan = 210;
        }

        document.getElementById('fare-Bike-text').innerText = "LKR " + fareBike;
        document.getElementById('fare-Wheel-text').innerText = "LKR " + fareWheel;
        document.getElementById('fare-Car-text').innerText = "LKR " + fareCar;
        document.getElementById('fare-Van-text').innerText = "LKR " + fareVan;

        let activeFare = fareBike;
        if (selectedVehicleType === 'Three-Wheeler') activeFare = fareWheel;
        else if (selectedVehicleType === 'Car') activeFare = fareCar;
        else if (selectedVehicleType === 'Van') activeFare = fareVan;

        document.getElementById('fareValue').value = activeFare;
    }

    function selectVehicle(type, amount) {
        selectedVehicleType = type;
        document.getElementById('vehicleType').value = type;
        document.getElementById('fareValue').value = amount;

        document.querySelectorAll('.vehicle-option').forEach(el => el.classList.remove('selected'));
        event.currentTarget.classList.add('selected');
    }

    document.addEventListener('click', function(e) {
        if (!e.target.closest('#pickup') && !e.target.closest('#pickup-suggestions')) {
            document.getElementById('pickup-suggestions').classList.add('d-none');
        }
        if (!e.target.closest('#destination') && !e.target.closest('#destination-suggestions')) {
            document.getElementById('destination-suggestions').classList.add('d-none');
        }
    });

    // Tracking Logic
    let trackingInterval = null;
    let trackMap = null;
    let trackDriverMarker = null;
    let trackPickupMarker = null;
    let trackDropMarker = null;
    let trackRouteLayer = null;

    function initTrackingMap() {
        if (!trackMap) {
            trackMap = L.map('track-map').setView([6.9271, 79.8612], 13);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(trackMap);
        }
    }

    function openTrackingModal(bookingId) {
        const modal = new bootstrap.Modal(document.getElementById('trackingModal'));
        modal.show();
        setTimeout(initTrackingMap, 500); // Wait for modal to render
        pollTracking(bookingId);
        trackingInterval = setInterval(() => pollTracking(bookingId), 4000);
    }

    function closeTrackingModal() {
        if (trackingInterval) clearInterval(trackingInterval);
    }

    function pollTracking(bookingId) {
        fetch('/ride/api/tracking/' + bookingId)
            .then(res => {
                if (res.status === 403 || res.status === 401) {
                    closeTrackingModal();
                    alert("Access denied to tracking for this booking.");
                    return null;
                }
                return res.json();
            })
            .then(data => {
                if (data) updateTrackingUI(data);
            })
            .catch(err => console.error("Error fetching tracking data", err));
    }

    function openReviewModal(bookingId) {
        document.getElementById('reviewForm').action = '/ride/review/' + bookingId;
        new bootstrap.Modal(document.getElementById('reviewModal')).show();
    }

    // Refresh if page restored from bfcache on account switch
    window.addEventListener('pageshow', function(event) {
        if (event.persisted) {
            window.location.reload();
        }
    });

    function updateTrackingUI(data) {
        if (!trackMap) return;

        // UI Updates
        document.getElementById('track-driverName').innerText = data.driverName || 'Waiting...';
        document.getElementById('track-vehicleInfo').innerText = data.vehicleNumber ? `${data.vehicleType} (${data.vehicleNumber})` : '';
        document.getElementById('track-driverPhone').innerHTML = data.driverPhone ? `<i class="fa-solid fa-phone me-1"></i> ${data.driverPhone}` : '';
        
        let statusBadge = document.getElementById('track-rideStatus');
        statusBadge.innerText = data.bookingStatus;
        statusBadge.className = 'badge ' + (data.bookingStatus === 'ACCEPTED' ? 'bg-primary' : (data.bookingStatus === 'ARRIVED' ? 'bg-warning text-dark' : 'bg-success'));

        // Markers
        if (data.pickupLat && data.pickupLng && !trackPickupMarker) {
            trackPickupMarker = L.marker([data.pickupLat, data.pickupLng]).addTo(trackMap).bindPopup("Pickup");
        }
        if (data.dropLat && data.dropLng && !trackDropMarker) {
            trackDropMarker = L.marker([data.dropLat, data.dropLng], {icon: L.icon({
                iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.ico',
                iconSize: [25, 41], iconAnchor: [12, 41]
            })}).addTo(trackMap).bindPopup("Drop");
        }

        if (data.driverLat && data.driverLng) {
            let latlng = [data.driverLat, data.driverLng];
            
            if (!trackDriverMarker) {
                let carIcon = L.icon({
                    iconUrl: 'https://cdn-icons-png.flaticon.com/512/3204/3204933.png',
                    iconSize: [32, 32], iconAnchor: [16, 16]
                });
                trackDriverMarker = L.marker(latlng, {icon: carIcon}).addTo(trackMap).bindPopup("Driver Location");
                trackMap.setView(latlng, 15);
            } else {
                trackDriverMarker.setLatLng(latlng);
            }
            
            let lastUpdate = new Date(data.lastLocationUpdate);
            let diffSecs = (new Date() - lastUpdate) / 1000;
            if (diffSecs > 30) {
                document.getElementById('track-statusText').innerText = "Location outdated (" + Math.round(diffSecs) + "s ago)";
            } else {
                document.getElementById('track-statusText').innerText = "Live Location Active";
                document.getElementById('track-statusText').className = "small fw-bold mt-1 text-success";
            }
        } else {
            document.getElementById('track-statusText').innerText = "Waiting for driver location...";
            if (trackPickupMarker) trackMap.setView(trackPickupMarker.getLatLng(), 14);
        }
    }
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>