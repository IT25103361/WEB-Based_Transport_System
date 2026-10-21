<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment & Offers - NEXT GO</title>
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <!-- NEXT GO Shared Theme -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nextgo-theme.css">
    <script src="${pageContext.request.contextPath}/js/nextgo-theme.js"></script>
    <style>
        .offer-card {
            border: 2px solid #e9ecef;
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .offer-card:hover {
            border-color: #0d6efd;
            background-color: #f8f9fa;
        }
        .offer-card.selected {
            border-color: #198754;
            background-color: #e8f5e9;
        }
    </style>
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
    <div class="container-fluid px-4 d-flex justify-content-between align-items-center">
        <a class="navbar-brand fw-bold" href="/ride/passenger">
            <i class="fa-solid fa-arrow-left me-2"></i> Back to Passenger Portal
        </a>
        <button type="button" class="nextgo-theme-toggle" data-nextgo-theme-toggle title="Toggle light/dark theme" aria-label="Toggle light/dark theme">🌙</button>
    </div>
</nav>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow-sm border-0 rounded-4">
                <div class="card-header bg-primary text-white py-3 rounded-top-4">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-credit-card me-2"></i> Payment Summary & Offers</h5>
                </div>
                <div class="card-body p-4">
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <h6 class="text-muted fw-bold mb-2">Ride Details</h6>
                            <p class="mb-1"><i class="fa-solid fa-location-dot text-danger me-2"></i> <strong>From:</strong> ${booking.pickupLocation}</p>
                            <p class="mb-1"><i class="fa-solid fa-flag-checkered text-success me-2"></i> <strong>To:</strong> ${booking.dropLocation}</p>
                            <p class="mb-0"><i class="fa-solid fa-car text-primary me-2"></i> <strong>Vehicle:</strong> ${booking.vehicleType}</p>
                        </div>
                        <div class="col-md-6 text-md-end mt-3 mt-md-0">
                            <h6 class="text-muted fw-bold mb-2">Base Fare</h6>
                            <h3 class="fw-bold">LKR <span id="baseFare">${booking.fare}</span></h3>
                        </div>
                    </div>

                    <hr>

                    <h5 class="fw-bold mb-3"><i class="fa-solid fa-tags text-warning me-2"></i> Available Offers</h5>
                    
                    <form action="/ride/pay/${booking.id}" method="post" id="paymentForm">
                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <div class="offer-card p-3 selected" onclick="selectOffer('NONE', 0, this)">
                                    <h6 class="fw-bold mb-1">No Offer</h6>
                                    <p class="text-muted small mb-0">Pay full amount</p>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="offer-card p-3" onclick="selectOffer('WELCOME10', 10, this)">
                                    <h6 class="fw-bold mb-1 text-primary"><i class="fa-solid fa-gift me-1"></i> WELCOME10</h6>
                                    <p class="text-muted small mb-0">10% off your ride</p>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="offer-card p-3" onclick="selectOffer('WEEKEND20', 20, this)">
                                    <h6 class="fw-bold mb-1 text-success"><i class="fa-solid fa-sun me-1"></i> WEEKEND20</h6>
                                    <p class="text-muted small mb-0">20% off weekend special</p>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="offer-card p-3" onclick="selectOffer('FLAT50', 'flat-50', this)">
                                    <h6 class="fw-bold mb-1 text-danger"><i class="fa-solid fa-tag me-1"></i> FLAT50</h6>
                                    <p class="text-muted small mb-0">Flat LKR 50 off</p>
                                </div>
                            </div>
                        </div>

                        <input type="hidden" name="appliedOffer" id="appliedOffer" value="NONE">
                        <input type="hidden" name="finalFare" id="finalFareInput" value="${booking.fare}">

                        <div class="bg-light p-3 rounded-3 mb-4 border">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="fw-bold text-muted">Subtotal:</span>
                                <span class="fw-bold">LKR ${booking.fare}</span>
                            </div>
                            <div class="d-flex justify-content-between mb-2 text-success">
                                <span class="fw-bold">Discount:</span>
                                <span class="fw-bold">- LKR <span id="discountAmount">0</span></span>
                            </div>
                            <hr class="my-2">
                            <div class="d-flex justify-content-between">
                                <span class="fw-bold fs-5">Total to Pay:</span>
                                <span class="fw-bold fs-5 text-primary">LKR <span id="totalFare">${booking.fare}</span></span>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary w-100 py-3 fw-bold shadow-sm fs-5">
                            <i class="fa-solid fa-check-circle me-2"></i> Confirm Payment
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    let baseFare = parseFloat(document.getElementById('baseFare').innerText);
    
    function selectOffer(offerCode, discountValue, element) {
        // Update styling
        document.querySelectorAll('.offer-card').forEach(card => card.classList.remove('selected'));
        element.classList.add('selected');
        
        // Calculate discount
        let discount = 0;
        if (discountValue === 'flat-50') {
            discount = 50;
        } else {
            discount = baseFare * (discountValue / 100);
        }
        
        // Ensure fare doesn't go negative
        let finalFare = baseFare - discount;
        if (finalFare < 0) finalFare = 0;
        
        // Update UI
        document.getElementById('discountAmount').innerText = discount.toFixed(2);
        document.getElementById('totalFare').innerText = finalFare.toFixed(2);
        
        // Update hidden inputs
        document.getElementById('appliedOffer').value = offerCode;
        document.getElementById('finalFareInput').value = finalFare.toFixed(2);
    }
</script>
</body>
</html>
