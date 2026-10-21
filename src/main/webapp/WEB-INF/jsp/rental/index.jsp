<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vehicle Rental — NEXT GO</title>
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- FontAwesome 6 -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <!-- Shared NextGo Theme -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nextgo-theme.css">
    <script src="${pageContext.request.contextPath}/js/nextgo-theme.js"></script>
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f1f5f9;
        }
        .premium-nav-rental {
            background: linear-gradient(135deg, #0f172a, #1e293b);
            border-bottom: 2px solid #06b6d4;
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
        .card-header-rental {
            background: linear-gradient(135deg, #06b6d4, #0284c7);
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
        .form-control, .form-select {
            border: 1px solid #cbd5e1;
            border-radius: 12px;
            padding: 12px 16px;
            background-color: #f8fafc;
            transition: all 0.2s;
        }
        .form-control:focus, .form-select:focus {
            background-color: #fff;
            border-color: #06b6d4;
            box-shadow: 0 0 0 4px rgba(6, 182, 212, 0.15);
        }
        .btn-rental {
            background: linear-gradient(135deg, #06b6d4, #0284c7);
            border: none;
            border-radius: 12px;
            color: #fff;
            font-weight: 600;
            transition: all 0.3s;
            box-shadow: 0 4px 15px rgba(6, 182, 212, 0.3);
        }
        .btn-rental:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(6, 182, 212, 0.4);
            color: #fff;
        }
        .btn-rental:disabled {
            background: #94a3b8;
            box-shadow: none;
            cursor: not-allowed;
            opacity: 0.65;
        }
        .vehicle-card {
            border: 2px solid #e2e8f0;
            border-radius: 16px;
            overflow: hidden;
            transition: all 0.3s ease;
        }
        .vehicle-card:hover {
            border-color: #06b6d4;
            box-shadow: 0 8px 25px rgba(6, 182, 212, 0.15);
        }
        .vehicle-card.selected-vehicle-card {
            border-color: #06b6d4;
            box-shadow: 0 0 0 4px rgba(6, 182, 212, 0.25);
            background-color: #f0fdfa;
        }
        .vehicle-img {
            width: 100%;
            height: 180px;
            object-fit: cover;
            background-color: #0f172a;
            border-bottom: 1px solid #e2e8f0;
        }
        .summary-thumb {
            width: 90px;
            height: 65px;
            object-fit: cover;
            border-radius: 10px;
            background-color: #0f172a;
        }
        .price-breakdown-card {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 16px;
        }
        .breakdown-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            font-size: 0.9rem;
        }
        .breakdown-total {
            border-top: 2px dashed #cbd5e1;
            padding-top: 10px;
            margin-top: 10px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-weight: 700;
            font-size: 1.15rem;
            color: #0284c7;
        }
        .filter-bar {
            background: #ffffff;
            border-radius: 16px;
            border: 1px solid #e2e8f0;
            padding: 18px 22px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.03);
        }
        .modal-content.glass-modal {
            border-radius: 20px;
            overflow: hidden;
            border: 1px solid rgba(0,0,0,0.1);
            box-shadow: 0 20px 40px rgba(0,0,0,0.2);
        }
    </style>
</head>
<body class="bg-light">

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark premium-nav-rental shadow-sm sticky-top">
    <div class="container-fluid px-4 py-1">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/ride/home">
            <i class="fa-solid fa-car-side text-info me-2"></i> NEXT GO <span class="fw-light opacity-75">| Vehicle Rental</span>
        </a>
        <div class="d-flex align-items-center">
            <button type="button" class="nextgo-theme-toggle me-3" data-nextgo-theme-toggle title="Toggle light/dark theme" aria-label="Toggle light/dark theme">🌙</button>
            <span class="text-white small me-3 d-none d-md-inline">
                <i class="fa-solid fa-user-circle me-1 text-info"></i> <c:out value="${sessionScope.loggedUser.fullName}"/>
            </span>
            <a href="${pageContext.request.contextPath}/rental/admin" class="btn btn-outline-light btn-sm me-2 rounded-pill px-3">
                <i class="fa-solid fa-user-shield me-1"></i> Admin Portal
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-warning btn-sm me-2 rounded-pill px-3">
                <i class="fa-solid fa-right-from-bracket me-1"></i> Logout
            </a>
            <a href="${pageContext.request.contextPath}/ride/home" class="btn btn-light btn-sm rounded-pill px-3 fw-semibold">
                <i class="fa-solid fa-house me-1"></i> Main Hub
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid py-5 px-4 px-lg-5">

    <!-- Flash Feedback Alerts -->
    <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger alert-dismissible fade show shadow-sm mb-4" role="alert">
            <i class="fa-solid fa-triangle-exclamation me-2"></i> <strong>Error:</strong> <c:out value="${errorMsg}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${not empty successMsg}">
        <div class="alert alert-success alert-dismissible fade show shadow-sm mb-4" role="alert">
            <i class="fa-solid fa-circle-check me-2"></i> <strong>Success:</strong> <c:out value="${successMsg}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <!-- Date-Based Fleet Filter Bar -->
    <div class="filter-bar mb-4">
        <form action="${pageContext.request.contextPath}/rental/index" method="get" class="row g-3 align-items-end">
            <div class="col-md-4">
                <label class="form-label small fw-bold text-muted mb-1">
                    <i class="fa-solid fa-calendar-days text-info me-1"></i> Filter By Start Date
                </label>
                <input type="date" name="filterStart" id="filterStart" class="form-control form-control-sm"
                       value="${filterStart}" min="${LocalDate.now()}" />
            </div>
            <div class="col-md-4">
                <label class="form-label small fw-bold text-muted mb-1">
                    <i class="fa-solid fa-calendar-check text-info me-1"></i> Filter By Return Date
                </label>
                <input type="date" name="filterEnd" id="filterEnd" class="form-control form-control-sm"
                       value="${filterEnd}" min="${LocalDate.now()}" />
            </div>
            <div class="col-md-4 d-flex gap-2">
                <button type="submit" class="btn btn-rental btn-sm flex-grow-1 fw-bold">
                    <i class="fa-solid fa-filter me-1"></i> Check Fleet Availability
                </button>
                <c:if test="${not empty filterStart || not empty filterEnd}">
                    <a href="${pageContext.request.contextPath}/rental/index" class="btn btn-outline-secondary btn-sm" title="Clear Date Filter">
                        <i class="fa-solid fa-rotate-left"></i> Clear
                    </a>
                </c:if>
            </div>
        </form>
    </div>

    <div class="row g-5">
        
        <!-- Left Column: Available Vehicles -->
        <div class="col-lg-7">
            <div class="card premium-card h-100">
                <div class="card-header card-header-rental text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-car me-2"></i> Rental Vehicle Fleet</h5>
                    <span class="badge bg-white text-dark fw-bold px-3 py-2 rounded-pill">${vehicles.size()} Vehicles</span>
                </div>
                <div class="card-body p-4">
                    <c:if test="${not empty filterStart && not empty filterEnd}">
                        <div class="alert alert-info py-2 small mb-4">
                            <i class="fa-solid fa-circle-info me-1"></i> Showing availability for:
                            <strong>${filterStart}</strong> to <strong>${filterEnd}</strong>.
                        </div>
                    </c:if>

                    <div class="row g-4">
                        <c:forEach var="v" items="${vehicles}">
                            <c:set var="isAvail" value="${availabilityMap[v.id]}" />
                            <div class="col-md-6">
                                <div class="vehicle-card h-100 d-flex flex-column bg-white ${retainedVehicleId == v.id ? 'selected-vehicle-card' : ''}" id="vcard-${v.id}">
                                    <img src="${v.resolveImageUrl(pageContext.request.contextPath)}"
                                         class="vehicle-img"
                                         id="vimg-${v.id}"
                                         alt="Photo of <c:out value="${v.name}"/> (<c:out value="${v.type}"/>)"
                                         onerror="if(this.src.indexOf('/rental/images/') === -1){this.onerror=null;this.src='${pageContext.request.contextPath}/rental/images/vehicle-fallback.svg';}">
                                    
                                    <div class="p-3 d-flex flex-column flex-grow-1">
                                        <div class="d-flex justify-content-between align-items-start mb-2">
                                            <h6 class="fw-bold mb-0" id="vname-${v.id}"><c:out value="${v.name}"/></h6>
                                            <span class="badge bg-primary" id="vtype-${v.id}"><c:out value="${v.type}"/></span>
                                        </div>
                                        <div class="text-muted small mb-3">
                                            Daily Rate: <strong class="text-dark">LKR <fmt:formatNumber value="${v.dailyRate}" pattern="#,##0.00"/></strong>
                                        </div>
                                        
                                        <div class="mt-auto text-center">
                                            <c:choose>
                                                <c:when test="${v.status == 'MAINTENANCE' || v.status == 'INACTIVE'}">
                                                    <span class="badge bg-secondary mb-2 w-100 py-2">
                                                        <i class="fa-solid fa-wrench me-1"></i> Under Maintenance
                                                    </span>
                                                    <button class="btn btn-outline-secondary btn-sm w-100 fw-bold" disabled>Unavailable</button>
                                                </c:when>
                                                <c:when test="${isAvail}">
                                                    <span class="badge bg-success mb-2 w-100 py-2">
                                                        <i class="fa-solid fa-check-circle me-1"></i> Available
                                                    </span>
                                                    <button type="button" class="btn btn-outline-info btn-sm w-100 fw-bold"
                                                            onclick="selectVehicle(${v.id}, '${v.name}', ${v.dailyRate}, '${v.type}', document.getElementById('vimg-${v.id}').src)">
                                                        <i class="fa-solid fa-hand-pointer me-1"></i> Select for Booking
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-warning text-dark mb-2 w-100 py-2" title="Reserved for selected dates or currently rented">
                                                        <i class="fa-solid fa-calendar-xmark me-1"></i> Reserved on Dates
                                                    </span>
                                                    <button type="button" class="btn btn-outline-secondary btn-sm w-100 fw-bold"
                                                            onclick="selectVehicle(${v.id}, '${v.name}', ${v.dailyRate}, '${v.type}', document.getElementById('vimg-${v.id}').src)">
                                                        <i class="fa-solid fa-calendar me-1"></i> View / Change Dates
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty vehicles}">
                            <div class="col-12 text-center text-muted py-5">
                                <i class="fa-solid fa-car fa-3x mb-3 text-secondary opacity-50"></i>
                                <p>No vehicles found in fleet.</p>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <!-- Right Column: Booking Form & Selected Vehicle Summary -->
        <div class="col-lg-5">
            <div class="card premium-card mb-4">
                <div class="card-header card-header-dark text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-calendar-check me-2 text-info"></i> Book a Vehicle</h5>
                    <span class="badge bg-info text-dark small fw-bold">Step 1 of 1</span>
                </div>
                <div class="card-body p-4 p-md-5">
                    
                    <!-- Feature 5: Selected Vehicle Summary -->
                    <div id="noVehicleSelectedPrompt" class="p-3 bg-light border rounded-3 mb-4 text-center text-muted small">
                        <i class="fa-solid fa-hand-pointer fa-2x mb-2 d-block text-info opacity-75"></i>
                        Select a vehicle from the fleet on the left to view instant quote and verify date availability.
                    </div>

                    <div id="selectedVehicleSummary" class="card border border-info bg-light p-3 mb-4 d-none shadow-sm">
                        <div class="d-flex align-items-center gap-3">
                            <img id="summaryVehicleImg" src="${pageContext.request.contextPath}/rental/images/vehicle-fallback.svg"
                                 class="summary-thumb border" alt="Selected Vehicle Preview">
                            <div class="flex-grow-1">
                                <div class="d-flex justify-content-between align-items-start">
                                    <h6 class="fw-bold mb-0 text-dark" id="summaryVehicleName">Selected Vehicle</h6>
                                    <span class="badge bg-primary" id="summaryVehicleType">Car</span>
                                </div>
                                <div class="text-muted small mt-1">
                                    Rate: <strong class="text-dark">LKR <span id="summaryDailyRate">0.00</span></strong> / day
                                </div>
                                <div class="small text-muted mt-1" id="summarySelectedDates">
                                    <i class="fa-solid fa-calendar-days me-1"></i> Dates: <span class="fw-semibold text-dark">Please select dates</span>
                                </div>
                            </div>
                        </div>
                        <div class="mt-2 pt-2 border-top" id="summaryAvailabilityBadge">
                            <span class="badge bg-secondary"><i class="fa-solid fa-circle-question me-1"></i> Awaiting Dates</span>
                        </div>
                    </div>

                    <!-- Booking Form -->
                    <form action="${pageContext.request.contextPath}/rental/book" method="post" id="rentalBookingForm" onsubmit="return validateAndSubmitBooking(event);">
                        <input type="hidden" name="vehicleId" id="vehicleIdInput" value="${retainedVehicleId}" required />
                        
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Customer Name</label>
                            <input type="text" name="customerName" id="customerName" class="form-control"
                                   value="${sessionScope.loggedUser.fullName}" placeholder="e.g. John Doe" required />
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted">Contact Phone Number</label>
                            <input type="text" name="customerPhone" id="customerPhone" class="form-control"
                                   value="${not empty retainedPhone ? retainedPhone : '0771234567'}" placeholder="0771234567" required />
                        </div>
                        
                        <div class="row g-3 mb-3">
                            <div class="col-6">
                                <label class="form-label fw-semibold small text-muted">
                                    <i class="fa-solid fa-calendar-day text-info me-1"></i> Start Date
                                </label>
                                <input type="date" name="startDate" id="startDate" class="form-control"
                                       value="${not empty retainedStartDate ? retainedStartDate : (not empty filterStart ? filterStart : LocalDate.now())}"
                                       required min="${LocalDate.now()}" onchange="onDateChanged()" />
                            </div>
                            <div class="col-6">
                                <label class="form-label fw-semibold small text-muted">
                                    <i class="fa-solid fa-calendar-check text-info me-1"></i> Return Date
                                </label>
                                <input type="date" name="endDate" id="endDate" class="form-control"
                                       value="${not empty retainedEndDate ? retainedEndDate : (not empty filterEnd ? filterEnd : LocalDate.now())}"
                                       required min="${LocalDate.now()}" onchange="onDateChanged()" />
                            </div>
                        </div>

                        <!-- Booked ranges calendar helper note -->
                        <div id="vehicleBookedDatesNotice" class="alert alert-warning py-2 small d-none mb-3">
                            <i class="fa-solid fa-calendar-xmark me-1"></i> <strong>Existing reservations:</strong>
                            <span id="vehicleBookedDatesList"></span>
                        </div>

                        <!-- Feature 2: Clear Price Breakdown -->
                        <div class="price-breakdown-card mb-4" id="priceBreakdownCard">
                            <h6 class="fw-bold small text-muted mb-2 text-uppercase letter-spacing-1">
                                <i class="fa-solid fa-file-invoice-dollar me-1 text-info"></i> Price Breakdown (LKR)
                            </h6>
                            <div class="breakdown-row">
                                <span class="text-muted">Daily Rental Rate:</span>
                                <span class="fw-semibold">LKR <span id="quoteDailyRate">0.00</span></span>
                            </div>
                            <div class="breakdown-row">
                                <span class="text-muted">Billable Days:</span>
                                <span class="fw-semibold"><span id="quoteDays">0</span> Day(s)</span>
                            </div>
                            <div class="text-xs text-muted mb-2 ps-1" id="quoteExplanation">
                                <i class="fa-solid fa-circle-info me-1 text-secondary"></i> Day-count rule: 24-hour cycle from pickup to return date (minimum 1 day).
                            </div>
                            <div class="breakdown-row">
                                <span class="text-muted">Rental Subtotal:</span>
                                <span class="fw-semibold">LKR <span id="quoteSubtotal">0.00</span></span>
                            </div>
                            <div class="breakdown-row text-muted small">
                                <span>Refundable Security Deposit:</span>
                                <span>LKR 0.00</span>
                            </div>
                            <div class="breakdown-total">
                                <span>Final Total (LKR):</span>
                                <span>LKR <span id="quoteTotal">0.00</span></span>
                            </div>
                        </div>

                        <!-- Submit Confirmation Button -->
                        <button type="submit" class="btn btn-rental w-100 py-3 text-uppercase letter-spacing-1 fw-bold shadow-sm"
                                id="confirmBookingBtn" disabled>
                            <i class="fa-solid fa-paper-plane me-2"></i> Confirm Booking
                        </button>
                    </form>
                </div>
            </div>
            
            <!-- Feature 3: My Rental Bookings Summary with "Extend My Rental" -->
            <div class="card premium-card">
                <div class="card-body p-4">
                    <h6 class="fw-bold mb-3"><i class="fa-solid fa-clock-rotate-left me-2 text-info"></i> My Rental Bookings</h6>
                    <ul class="list-group list-group-flush small">
                        <c:forEach var="b" items="${bookings}">
                            <li class="list-group-item px-0 py-3 border-bottom">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong class="text-dark"><c:out value="${b.vehicle.name}"/></strong>
                                    <span class="badge bg-${b.status == 'CONFIRMED' ? 'success' : (b.status == 'COMPLETED' ? 'secondary' : (b.status == 'CANCELLED' ? 'danger' : 'warning text-dark'))}">
                                        <c:out value="${b.status}"/>
                                    </span>
                                </div>
                                <div class="text-muted text-xs">
                                    <i class="fa-solid fa-calendar me-1"></i> <c:out value="${b.startDate}"/> to <c:out value="${b.endDate}"/>
                                    (${b.days} Days) | Total: <strong class="text-dark">LKR <fmt:formatNumber value="${b.totalCost}" pattern="#,##0.00"/></strong>
                                </div>
                                <c:if test="${not empty b.extendedFromDate}">
                                    <div class="text-success text-xs mt-1">
                                        <i class="fa-solid fa-clock-rotate-left me-1"></i> Extended from ${b.extendedFromDate} (+LKR <fmt:formatNumber value="${b.extensionCost}" pattern="#,##0.00"/>)
                                    </div>
                                </c:if>
                                
                                <div class="mt-2 d-flex gap-2 justify-content-end align-items-center">
                                    <c:if test="${b.status == 'PENDING' || b.status == 'CONFIRMED'}">
                                        <button type="button" class="btn btn-outline-primary btn-sm py-1 px-2" style="font-size: 0.75rem;"
                                                onclick="openExtendModal(${b.id}, '${b.vehicle.name}', '${b.endDate}', ${b.dailyRate != null ? b.dailyRate : b.vehicle.dailyRate})">
                                            <i class="fa-solid fa-calendar-plus me-1"></i> Extend
                                        </button>
                                        <a href="${pageContext.request.contextPath}/rental/booking/status/${b.id}?status=CANCELLED&src=index"
                                           class="btn btn-outline-danger btn-sm py-1 px-2" style="font-size: 0.75rem;"
                                           onclick="return confirm('Cancel this booking? Dates will be released immediately.');">
                                            <i class="fa-solid fa-xmark me-1"></i> Cancel
                                        </a>
                                    </c:if>
                                </div>
                            </li>
                        </c:forEach>
                        <c:if test="${empty bookings}">
                            <li class="list-group-item text-center text-muted py-4 border-0">
                                You have no rental bookings yet. Select a vehicle above to reserve!
                            </li>
                        </c:if>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal: Feature 3 - Extend My Rental -->
<div class="modal fade" id="extendRentalModal" tabindex="-1" aria-labelledby="extendRentalModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content glass-modal">
            <div class="modal-header bg-dark text-white">
                <h5 class="modal-title fw-bold" id="extendRentalModalLabel">
                    <i class="fa-solid fa-calendar-plus text-info me-2"></i> Extend My Rental
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="extendForm" method="post" action="">
                <div class="modal-body p-4">
                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-muted">Vehicle</label>
                        <input type="text" id="extendVehicleName" class="form-control fw-bold" readonly />
                    </div>
                    <div class="row g-3 mb-3">
                        <div class="col-6">
                            <label class="form-label small fw-semibold text-muted">Current Return Date</label>
                            <input type="date" id="extendCurrentEndDate" class="form-control" readonly />
                        </div>
                        <div class="col-6">
                            <label class="form-label small fw-semibold text-muted text-primary">New Return Date</label>
                            <input type="date" name="newEndDate" id="extendNewEndDate" class="form-control border-primary"
                                   required onchange="calculateExtensionQuote()" />
                        </div>
                    </div>

                    <!-- Extension Feedback -->
                    <div id="extensionAvailabilityAlert" class="alert py-2 small d-none mb-3"></div>

                    <!-- Extension Quote Breakdown -->
                    <div class="price-breakdown-card mb-3" id="extendQuoteCard">
                        <div class="breakdown-row">
                            <span class="text-muted">Additional Days:</span>
                            <span class="fw-semibold">+<span id="extDays">0</span> Day(s)</span>
                        </div>
                        <div class="breakdown-row">
                            <span class="text-muted">Additional Charge:</span>
                            <span class="fw-semibold text-success">+LKR <span id="extCost">0.00</span></span>
                        </div>
                        <div class="breakdown-total">
                            <span>Revised Booking Total:</span>
                            <span>LKR <span id="extNewTotal">0.00</span></span>
                        </div>
                    </div>
                </div>
                <div class="modal-footer bg-light justify-content-between">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-rental btn-sm fw-bold px-4" id="confirmExtendBtn" disabled>
                        <i class="fa-solid fa-check me-1"></i> Confirm Extension
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    let selectedVehicle = {
        id: null,
        name: '',
        dailyRate: 0,
        type: '',
        imgSrc: ''
    };
    let isCurrentQuoteAvailable = false;
    let extendBookingId = null;

    function selectVehicle(id, name, dailyRate, type, imgSrc) {
        selectedVehicle.id = id;
        selectedVehicle.name = name;
        selectedVehicle.dailyRate = dailyRate;
        selectedVehicle.type = type;
        selectedVehicle.imgSrc = imgSrc;

        document.getElementById('vehicleIdInput').value = id;

        // Highlight selected card and remove highlight from others
        document.querySelectorAll('.vehicle-card').forEach(el => el.classList.remove('selected-vehicle-card'));
        const activeCard = document.getElementById('vcard-' + id);
        if (activeCard) activeCard.classList.add('selected-vehicle-card');

        // Update Feature 5: Selected Vehicle Summary
        document.getElementById('noVehicleSelectedPrompt').classList.add('d-none');
        const summary = document.getElementById('selectedVehicleSummary');
        summary.classList.remove('d-none');
        document.getElementById('summaryVehicleName').innerText = name;
        document.getElementById('summaryVehicleType').innerText = type;
        document.getElementById('summaryDailyRate').innerText = parseFloat(dailyRate).toLocaleString('en-US', { minimumFractionDigits: 2 });
        document.getElementById('summaryVehicleImg').src = imgSrc;

        // Load booked dates for this vehicle
        loadBookedDates(id);

        // Recalculate quote
        onDateChanged();
    }

    function loadBookedDates(vehicleId) {
        fetch('${pageContext.request.contextPath}/rental/api/vehicle/' + vehicleId + '/booked-dates')
            .then(res => res.json())
            .then(ranges => {
                const notice = document.getElementById('vehicleBookedDatesNotice');
                const list = document.getElementById('vehicleBookedDatesList');
                if (ranges && ranges.length > 0) {
                    const text = ranges.map(r => r.start + ' to ' + r.end).join(', ');
                    list.innerText = text;
                    notice.classList.remove('d-none');
                } else {
                    notice.classList.add('d-none');
                }
            }).catch(() => {
                document.getElementById('vehicleBookedDatesNotice').classList.add('d-none');
            });
    }

    function onDateChanged() {
        const start = document.getElementById('startDate').value;
        const end = document.getElementById('endDate').value;
        const confirmBtn = document.getElementById('confirmBookingBtn');
        const badge = document.getElementById('summaryAvailabilityBadge');
        const datesLabel = document.getElementById('summarySelectedDates');

        if (!selectedVehicle.id) {
            confirmBtn.disabled = true;
            return;
        }

        if (!start || !end) {
            badge.innerHTML = '<span class="badge bg-secondary"><i class="fa-solid fa-circle-question me-1"></i> Awaiting Dates</span>';
            confirmBtn.disabled = true;
            return;
        }

        datesLabel.innerHTML = '<i class="fa-solid fa-calendar-days me-1"></i> Dates: <span class="fw-semibold text-dark">' + start + ' to ' + end + '</span>';

        if (end < start) {
            badge.innerHTML = '<span class="badge bg-danger"><i class="fa-solid fa-triangle-exclamation me-1"></i> Return date cannot be before start date</span>';
            confirmBtn.disabled = true;
            isCurrentQuoteAvailable = false;
            return;
        }

        // Fetch authoritative server quote and availability
        fetch('${pageContext.request.contextPath}/rental/api/quote?vehicleId=' + selectedVehicle.id + '&startDate=' + start + '&endDate=' + end)
            .then(res => res.json())
            .then(quote => {
                if (quote.available) {
                    badge.innerHTML = '<span class="badge bg-success py-2 px-3"><i class="fa-solid fa-check-circle me-1"></i> Available for selected dates (' + quote.billableDays + ' day' + (quote.billableDays === 1 ? '' : 's') + ')</span>';
                    confirmBtn.disabled = false;
                    isCurrentQuoteAvailable = true;
                } else {
                    badge.innerHTML = '<span class="badge bg-danger py-2 px-3"><i class="fa-solid fa-calendar-xmark me-1"></i> ' + (quote.availabilityMessage || 'Unavailable on selected dates') + '</span>';
                    confirmBtn.disabled = true;
                    isCurrentQuoteAvailable = false;
                }

                // Update Price Breakdown (Feature 2)
                document.getElementById('quoteDailyRate').innerText = quote.formattedDailyRate || '0.00';
                document.getElementById('quoteDays').innerText = quote.billableDays || '0';
                document.getElementById('quoteSubtotal').innerText = quote.formattedSubtotal || '0.00';
                document.getElementById('quoteTotal').innerText = quote.formattedTotal || '0.00';
                if (quote.dayCountExplanation) {
                    document.getElementById('quoteExplanation').innerHTML = '<i class="fa-solid fa-circle-info me-1 text-secondary"></i> ' + quote.dayCountExplanation;
                }
            })
            .catch(err => {
                console.error("Quote fetch error:", err);
                confirmBtn.disabled = true;
            });
    }

    function validateAndSubmitBooking(e) {
        if (!selectedVehicle.id) {
            alert("Please select a vehicle from the fleet.");
            e.preventDefault();
            return false;
        }
        if (!isCurrentQuoteAvailable) {
            alert("The vehicle is not available for the selected dates. Please adjust your dates.");
            e.preventDefault();
            return false;
        }
        const btn = document.getElementById('confirmBookingBtn');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span> Reserving...';
        return true;
    }

    // Modal: Feature 3 - Extend My Rental
    function openExtendModal(id, vehicleName, endDate, dailyRate) {
        extendBookingId = id;
        document.getElementById('extendVehicleName').value = vehicleName;
        document.getElementById('extendCurrentEndDate').value = endDate;
        
        const nextDay = new Date(endDate);
        nextDay.setDate(nextDay.getDate() + 1);
        const nextDayStr = nextDay.toISOString().split('T')[0];
        
        const newEndInput = document.getElementById('extendNewEndDate');
        newEndInput.min = nextDayStr;
        newEndInput.value = nextDayStr;

        document.getElementById('extendForm').action = '${pageContext.request.contextPath}/rental/booking/' + id + '/extend';
        
        calculateExtensionQuote();
        new bootstrap.Modal(document.getElementById('extendRentalModal')).show();
    }

    function calculateExtensionQuote() {
        const newEnd = document.getElementById('extendNewEndDate').value;
        const alertBox = document.getElementById('extensionAvailabilityAlert');
        const confirmBtn = document.getElementById('confirmExtendBtn');

        if (!newEnd) return;

        fetch('${pageContext.request.contextPath}/rental/api/booking/' + extendBookingId + '/extend-preview?newEndDate=' + newEnd)
            .then(res => res.json())
            .then(data => {
                alertBox.classList.remove('d-none', 'alert-success', 'alert-danger');
                if (data.available) {
                    alertBox.classList.add('alert-success');
                    alertBox.innerHTML = '<i class="fa-solid fa-check-circle me-1"></i> ' + data.message;
                    confirmBtn.disabled = false;
                } else {
                    alertBox.classList.add('alert-danger');
                    alertBox.innerHTML = '<i class="fa-solid fa-triangle-exclamation me-1"></i> ' + data.message;
                    confirmBtn.disabled = true;
                }

                document.getElementById('extDays').innerText = data.additionalDays;
                document.getElementById('extCost').innerText = data.formattedAdditionalCost;
                document.getElementById('extNewTotal').innerText = data.formattedNewTotalCost;
            })
            .catch(err => {
                console.error("Extension preview error:", err);
                confirmBtn.disabled = true;
            });
    }

    // Initialize with retained vehicle if form was submitted with conflict
    document.addEventListener("DOMContentLoaded", function() {
        const retainedId = '${retainedVehicleId}';
        if (retainedId && retainedId.trim() !== '') {
            const vnameEl = document.getElementById('vname-' + retainedId);
            const vimgEl = document.getElementById('vimg-' + retainedId);
            const vtypeEl = document.getElementById('vtype-' + retainedId);
            if (vnameEl && vimgEl && vtypeEl) {
                selectVehicle(parseInt(retainedId), vnameEl.innerText, 0, vtypeEl.innerText, vimgEl.src);
            }
        }
    });
</script>
</body>
</html>
