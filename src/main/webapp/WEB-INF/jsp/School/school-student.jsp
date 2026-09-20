<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Student School Transport Portal - NEXT GO</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
  <div class="container px-4">
    <a class="navbar-brand fw-bold" href="/school-hub">
      <i class="fa-solid fa-school-bus text-warning me-2"></i> Student School Transport
    </a>
    <a href="/school-hub" class="btn btn-outline-light btn-sm">
      <i class="fa-solid fa-arrow-left me-1"></i> Back to Hub
    </a>
  </div>
</nav>

<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="card shadow border-0 rounded-4 p-4 p-md-5 bg-white">
        <div class="text-center mb-4">
          <div class="bg-warning bg-opacity-10 text-warning rounded-circle d-inline-flex p-3 mb-2">
            <i class="fa-solid fa-clipboard-user fa-2x"></i>
          </div>
          <h3 class="fw-bold text-dark">Student Monthly Transport Registration</h3>
          <p class="text-muted small">Book your school transport package and choose your preferred registered driver.</p>
        </div>

        <form action="/book-school-transport" method="post">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label fw-semibold">Student ID Code (For Parent Tracking)</label>
              <input type="text" name="studentIdCode" class="form-control" placeholder="e.g. STU-101 (Auto generated if empty)">
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">Student Name</label>
              <input type="text" name="studentName" class="form-control" placeholder="Enter student full name" required>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">School Name</label>
              <input type="text" name="schoolName" class="form-control" placeholder="Enter school name" required>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">Home Pickup Location</label>
              <input type="text" name="pickupLocation" class="form-control" placeholder="Enter pickup address" required>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">Select Vehicle Type</label>
              <select name="vehicleType" class="form-select" required>
                <option value="" selected disabled>Choose vehicle...</option>
                <option value="School Van">School Van (AC / Comfortable)</option>
                <option value="School Bus">School Bus (Pooling Service)</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">Select Preferred Driver</label>
              <select name="preferredDriver" class="form-select border-success border-2" required>
                <option value="" selected disabled>Choose registered driver...</option>
                <c:forEach var="drv" items="${drivers}">
                  <option value="${drv.driverName} - ${drv.vehicleNumber}">${drv.driverName} (${drv.vehicleType} - ${drv.vehicleNumber})</option>
                </c:forEach>
              </select>
            </div>
            <div class="col-md-12">
              <label class="form-label fw-semibold">Select Monthly Package Plan</label>
              <select name="packageType" class="form-select border-warning border-2" required>
                <option value="" selected disabled>Select monthly package...</option>
                <option value="Standard Monthly Package (Both Ways)">Standard Monthly Package (Both Ways) - LKR 12,500 / mo</option>
                <option value="Morning Only Package">Morning Only Package (Pickup to School) - LKR 7,500 / mo</option>
                <option value="Evening Only Package">Evening Only Package (Drop to Home) - LKR 7,500 / mo</option>
              </select>
            </div>
            <div class="col-12 mt-4">
              <button type="submit" class="btn btn-warning text-dark fw-bold w-100 py-3 shadow-sm">
                <i class="fa-solid fa-check-circle me-2"></i> Confirm Monthly Package Subscription
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>