<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>School Driver Portal - NEXT GO</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
  <div class="container px-4">
    <a class="navbar-brand fw-bold" href="/school-hub">
      <i class="fa-solid fa-school-bus text-success me-2"></i> School Driver Dashboard
    </a>
    <a href="/school-hub" class="btn btn-outline-light btn-sm">
      <i class="fa-solid fa-arrow-left me-1"></i> Back to Hub
    </a>
  </div>
</nav>

<div class="container py-5">
  <div class="row mb-4 align-items-center">
    <div class="col-md-8">
      <h2 class="fw-bold text-dark">Driver Registration & Route Management</h2>
      <p class="text-muted">Register your school transport vehicle, manage passenger lists, and view monthly packages.</p>
    </div>
    <div class="col-md-4 text-md-end">
      <span class="badge bg-success p-2 px-3 fs-6"><i class="fa-solid fa-bus me-1"></i> Active Driver Portal</span>
    </div>
  </div>

  <!-- Driver & Vehicle Registration Form Card -->
  <div class="card shadow-sm border-0 rounded-4 p-4 mb-5 bg-white border-top border-success border-4">
    <h4 class="fw-bold text-success mb-3"><i class="fa-solid fa-id-card-clip me-2"></i> Register as a School Driver & Vehicle</h4>
    <form action="/register-driver" method="post">
      <div class="row g-3">
        <div class="col-md-4">
          <label class="form-label fw-semibold">Driver Full Name</label>
          <input type="text" name="driverName" class="form-control" placeholder="Enter driver name" required>
        </div>
        <div class="col-md-4">
          <label class="form-label fw-semibold">Contact Number</label>
          <input type="text" name="contactNumber" class="form-control" placeholder="+94 77 XXX XXXX" required>
        </div>
        <div class="col-md-4">
          <label class="form-label fw-semibold">Vehicle Type</label>
          <select name="vehicleType" class="form-select" required>
            <option value="" selected disabled>Select vehicle type...</option>
            <option value="Van">School Van (AC/Normal)</option>
            <option value="Bus">School Bus (Pooling)</option>
          </select>
        </div>
        <div class="col-md-4">
          <label class="form-label fw-semibold">Vehicle Registration No.</label>
          <input type="text" name="vehicleNumber" class="form-control" placeholder="e.g. CAB-4589" required>
        </div>
        <div class="col-md-4">
          <label class="form-label fw-semibold">Route / Area Covered</label>
          <input type="text" name="routeArea" class="form-control" placeholder="e.g. Colombo 01 to 07" required>
        </div>
        <div class="col-md-4">
          <label class="form-label fw-semibold">Maximum Passenger Capacity</label>
          <input type="number" name="maxCapacity" class="form-control" placeholder="e.g. 15 students" required>
        </div>
        <div class="col-12 mt-3">
          <button type="submit" class="btn btn-success fw-bold px-4 py-2 shadow-sm">
            <i class="fa-solid fa-circle-plus me-2"></i> Register Vehicle & Driver Profile
          </button>
        </div>
      </div>
    </form>
  </div>

  <!-- Enrolled Students Table (Dynamic from Database) -->
  <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
    <div class="card-header bg-dark text-white py-3">
      <h5 class="mb-0 fw-semibold"><i class="fa-solid fa-users me-2"></i> Enrolled Students for Your Vehicle</h5>
    </div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
          <tr>
            <th class="ps-4">Student ID</th>
            <th>Student Name</th>
            <th>School</th>
            <th>Pickup Location</th>
            <th>Vehicle & Driver</th>
            <th>Package Type</th>
            <th class="text-end pe-4">Action</th>
          </tr>
          </thead>
          <tbody>
          <c:choose>
            <c:when test="${not empty students}">
              <c:forEach var="std" items="${students}">
                <tr>
                  <td class="ps-4 fw-semibold text-primary">${std.studentIdCode}</td>
                  <td class="fw-semibold">${std.studentName}</td>
                  <td>${std.schoolName}</td>
                  <td>${std.pickupLocation}</td>
                  <td><span class="badge bg-info">${std.preferredDriver}</span></td>
                  <td>${std.packageType}</td>
                  <td class="text-end pe-4">
                    <button class="btn btn-sm btn-outline-success fw-semibold"><i class="fa-solid fa-check me-1"></i> Mark Attendance</button>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="7" class="text-center py-4 text-muted">No student registrations found in database yet.</td>
              </tr>
            </c:otherwise>
          </c:choose>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>