<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Parent Live Tracking - NEXT GO</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
  <style>
    .map-placeholder {
      background: linear-gradient(135deg, #e3f2fd, #bbdefb);
      border-radius: 12px;
      height: 380px;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      overflow: hidden;
    }
    .pulse-dot {
      width: 20px;
      height: 20px;
      background: #0d6efd;
      border-radius: 50%;
      box-shadow: 0 0 0 rgba(13, 110, 253, 0.4);
      animation: pulse 1.5s infinite;
    }
    @keyframes pulse {
      0% { box-shadow: 0 0 0 0 rgba(13, 110, 253, 0.7); }
      70% { box-shadow: 0 0 0 20px rgba(13, 110, 253, 0); }
      100% { box-shadow: 0 0 0 0 rgba(13, 110, 253, 0); }
    }
  </style>
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
  <div class="container px-4">
    <a class="navbar-brand fw-bold" href="/school-hub">
      <i class="fa-solid fa-school-bus text-info me-2"></i> Parent Live Tracking Portal
    </a>
    <a href="/school-hub" class="btn btn-outline-light btn-sm">
      <i class="fa-solid fa-arrow-left me-1"></i> Back to Hub
    </a>
  </div>
</nav>

<div class="container py-5">
  <div class="row justify-content-center mb-4">
    <div class="col-lg-8 text-center">
            <span class="badge bg-info bg-opacity-10 text-info px-3 py-2 rounded-pill fw-bold mb-2">
                <i class="fa-solid fa-location-crosshairs me-1"></i> Real-Time Monitoring
            </span>
      <h2 class="fw-bold text-dark">Parent Tracking & Student Finder</h2>
      <p class="text-muted small">Enter your assigned Student ID (e.g., <strong>STU-101</strong> or <strong>STU-102</strong>) to check live vehicle location and trip details.</p>

      <!-- Student ID Search Form -->
      <form action="/track-student" method="post" class="card shadow-sm border-0 p-3 bg-white rounded-4 mt-3">
        <div class="input-group">
          <span class="input-group-text bg-light border-0"><i class="fa-solid fa-id-card text-primary"></i></span>
          <input type="text" name="studentId" class="form-control border-0 bg-light" placeholder="Enter Student ID (e.g. STU-101)" required value="${studentId != null ? studentId : ''}">
          <button class="btn btn-info text-white fw-bold px-4" type="submit">
            <i class="fa-solid fa-search me-1"></i> Track Vehicle
          </button>
        </div>
      </form>
    </div>
  </div>

  <%-- If Searched and Found --%>
  <% if (request.getAttribute("searched") != null && request.getAttribute("notFound") == null) { %>
  <div class="row g-4 mt-2">
    <!-- Left Column: Live Map Mockup -->
    <div class="col-lg-8">
      <div class="card shadow-sm border-0 rounded-4 p-4 bg-white mb-4 h-100">
        <h5 class="fw-bold text-dark mb-3"><i class="fa-solid fa-map-location-dot text-primary me-2"></i> Live GPS Tracking Map</h5>
        <div class="map-placeholder border shadow-inner">
          <div class="text-center p-4">
            <div class="pulse-dot mx-auto mb-3"></div>
            <h5 class="fw-bold text-primary">${vehicleNo} is Active</h5>
            <p class="text-muted small mb-1">Current Location: <strong>${location}</strong></p>
            <span class="badge bg-success mb-2">${status}</span>
            <div><span class="badge bg-dark">ETA: ${eta}</span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Column: Student & Driver Details -->
    <div class="col-lg-4">
      <div class="card shadow-sm border-0 rounded-4 p-4 bg-white mb-4">
        <h5 class="fw-bold text-dark mb-3"><i class="fa-solid fa-child me-2 text-warning"></i> Student Details</h5>
        <ul class="list-unstyled small mb-4">
          <li class="mb-2"><strong>ID:</strong> ${studentId}</li>
          <li class="mb-2"><strong>Name:</strong> ${studentName}</li>
          <li class="mb-2"><strong>School:</strong> ${schoolName}</li>
          <li class="mb-0"><strong>Package:</strong> ${packageName}</li>
        </ul>
        <hr>
        <h5 class="fw-bold text-dark mb-3 mt-3"><i class="fa-solid fa-id-card-clip me-2 text-success"></i> Assigned Driver</h5>
        <ul class="list-unstyled small mb-3">
          <li class="mb-2"><strong>Driver:</strong> ${driverName}</li>
          <li class="mb-2"><strong>Vehicle:</strong> ${vehicleNo}</li>
          <li class="mb-0"><strong>Phone:</strong> ${driverPhone}</li>
        </ul>
        <a href="tel:${driverPhone}" class="btn btn-outline-success btn-sm w-100 fw-semibold">
          <i class="fa-solid fa-phone me-1"></i> Call Driver Directly
        </a>
      </div>
    </div>
  </div>
  <% } %>

  <%-- If Student ID Not Found --%>
  <% if (request.getAttribute("notFound") != null) { %>
  <div class="row justify-content-center mt-4">
    <div class="col-md-6 text-center">
      <div class="alert alert-danger shadow-sm rounded-4 p-4">
        <i class="fa-solid fa-triangle-exclamation fa-3x mb-3 text-danger"></i>
        <h5 class="fw-bold">Student ID Not Found</h5>
        <p class="small text-muted mb-0">Please check the Student ID and try again. (Tip: Try entering sample ID <strong>STU-101</strong> or <strong>STU-102</strong>)</p>
      </div>
    </div>
  </div>
  <% } %>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>