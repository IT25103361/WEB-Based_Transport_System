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
    <style>
        #map { height: 240px; border-radius: 10px; z-index: 1; }
        .vehicle-option {
            cursor: pointer;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            transition: all 0.2s ease;
        }
        .vehicle-option:hover, .vehicle-option.selected {
            border-color: #0d6efd;
            background-color: #f8f9fa;
        }
        .suggestions-box {
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            z-index: 9999;
            background: #fff;
            border: 1px solid #ced4da;
            border-top: none;
            max-height: 180px;
            overflow-y: auto;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            border-radius: 0 0 6px 6px;
        }
        .suggestion-item {
            padding: 8px 12px;
            font-size: 0.85rem;
            cursor: pointer;
            border-bottom: 1px solid #f1f1f1;
        }
        .suggestion-item:hover {
            background-color: #f8f9fa;
        }
    </style>
</head>
<body class="bg-light">

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
    <div class="container-fluid px-4">
        <a class="navbar-brand fw-bold" href="/ride/home">
            <i class="fa-solid fa-taxi text-warning me-2"></i> NEXT GO — Passenger Portal
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
        <!-- Left Column: Booking Form & Map -->
        <div class="col-lg-5">
            <div class="card shadow-sm border-0 rounded-4">
                <div class="card-header bg-primary text-white py-3 rounded-top-4">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-map-location-dot me-2"></i> Book Your Ride</h5>
                </div>
                <div class="card-body p-4">
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
                        </div>

                        <!-- Payment Method -->
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Payment Method</label>
                            <form:select path="paymentMethod" cssClass="form-select">
                                <form:option value="Cash" label="Cash"/>
                                <form:option value="Card" label="Card"/>
                            </form:select>
                        </div>

                        <button type="submit" class="btn btn-primary w-100 py-2 fw-bold shadow-sm">
                            <i class="fa-solid fa-paper-plane me-2"></i> Request Booking
                        </button>
                    </form:form>
                </div>
            </div>
        </div>

        <!-- Right Column: Active Rides & History -->
        <div class="col-lg-7">
            <div class="card shadow-sm border-0 rounded-4">
                <div class="card-header bg-dark text-white py-3 rounded-top-4">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-list-check me-2"></i> Active Rides & History</h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                            <tr>
                                <th class="ps-3">ID / Passenger</th>
                                <th>Route, Vehicle & Fare</th>
                                <th>Status / Driver</th>
                                <th class="text-end pe-3">Payment</th>
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
                                            <span class="fw-bold text-dark">LKR ${b.fare != null ? b.fare : '0'}</span>
                                        </div>
                                    </td>
                                    <td>
                                            <span class="badge bg-${b.status == 'COMPLETED' ? 'success' : (b.status == 'ASSIGNED' ? 'primary' : 'warning text-dark')}">
                                                    ${b.status}
                                            </span><br>
                                        <span class="text-xs text-muted small">
                                                ${b.driver != null ? b.driver.driverName : 'No Driver Assigned'}
                                        </span>
                                    </td>
                                    <td class="text-end pe-3">
                                        <span class="badge bg-secondary">${b.paymentMethod != null ? b.paymentMethod : 'Cash'}</span>
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
            if (pickupMarker) map.removeLayer(pickupMarker);
            pickupMarker = L.marker([lat, lon]).addTo(map).bindPopup("Pickup: " + name).openPopup();
            map.setView([lat, lon], 13);
        } else {
            destCoord = [lat, lon];
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
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>