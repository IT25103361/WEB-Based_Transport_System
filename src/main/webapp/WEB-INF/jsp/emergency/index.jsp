<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="ctx" content="${pageContext.request.contextPath}">
  <meta name="csrf-token" content="${csrfToken}">
  <title>Emergency Services Portal | NEXT GO</title>

  <!-- Bootstrap 5 CSS -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
  <!-- FontAwesome 6 -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <!-- Leaflet Map CSS -->
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
  <!-- Scoped Emergency CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/emergency/css/emergency.css">
  <!-- Shared NextGo Theme -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nextgo-theme.css">
  <script src="${pageContext.request.contextPath}/js/nextgo-theme.js"></script>

  <!-- Immediate Theme Initializer to prevent flashing -->
  <script>
    (function () {
      try {
        var theme = localStorage.getItem("nextgo.theme") || localStorage.getItem("nextgo.emergency.theme");
        if (!theme && window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches) {
          theme = "dark";
        }
        if (theme) {
          document.documentElement.setAttribute("data-theme", theme);
        }
      } catch (e) {}
    })();
  </script>
</head>
<body class="emergency-body emergency-page">

  <!-- Navigation Bar -->
  <nav class="emergency-navbar">
    <div class="d-flex justify-content-between align-items-center max-w-1080 mx-auto w-100">
      <a href="${pageContext.request.contextPath}/emergency" class="navbar-brand">
        <i class="fa-solid fa-truck-medical"></i>
        <span>NEXT GO <span class="fw-light text-dark">Emergency Services</span></span>
      </a>
      <div class="d-flex align-items-center gap-2">
        <button type="button" class="emergency-theme-toggle" data-emergency-theme-toggle aria-label="Toggle theme" title="Switch light/dark theme">
          🌙
        </button>
        <a href="${pageContext.request.contextPath}/emergency/track" class="btn btn-outline-secondary btn-sm nav-link-btn">
          <i class="fa-solid fa-magnifying-glass me-1"></i> Track a Request
        </a>
        <a href="${pageContext.request.contextPath}/ride/home" class="btn btn-outline-dark btn-sm nav-link-btn">
          <i class="fa-solid fa-house me-1"></i> Main Hub
        </a>
      </div>
    </div>
  </nav>

  <div class="emergency-container">

    <!-- Direct-Call Assistance Area (Displayed Before Form Submission) -->
    <div class="direct-call-card" id="directCallSection">
      <div class="direct-call-header">
        <div class="direct-call-title">
          <i class="fa-solid fa-phone-volume fa-shake"></i>
          <span>Official Sri Lanka Emergency Helplines (Direct Call)</span>
        </div>
        <small class="text-white-50">Toll-free 24/7 National Emergency Numbers</small>
      </div>

      <div class="helpline-grid">
        <a href="tel:119" class="helpline-box" title="Call Police Emergency 119">
          <div class="info">
            <h6>Police Emergency</h6>
            <div class="number">119</div>
          </div>
          <div class="call-icon"><i class="fa-solid fa-phone"></i></div>
        </a>

        <a href="tel:1990" class="helpline-box" title="Call 1990 Suwa Seriya Ambulance">
          <div class="info">
            <h6>Suwa Seriya Ambulance</h6>
            <div class="number">1990</div>
          </div>
          <div class="call-icon"><i class="fa-solid fa-phone"></i></div>
        </a>

        <a href="tel:110" class="helpline-box" title="Call Fire & Rescue 110">
          <div class="info">
            <h6>Fire & Rescue</h6>
            <div class="number">110</div>
          </div>
          <div class="call-icon"><i class="fa-solid fa-phone"></i></div>
        </a>

        <a href="tel:117" class="helpline-box" title="Call Disaster Management Centre 117">
          <div class="info">
            <h6>Disaster Management</h6>
            <div class="number">117</div>
          </div>
          <div class="call-icon"><i class="fa-solid fa-phone"></i></div>
        </a>
      </div>
    </div>

    <!-- 3. Confirmation Panel (Revealed on Successful Submission) -->
    <div class="confirmation-panel" id="confirmationPanel">
      <div class="text-center mb-4">
        <div class="d-inline-flex p-3 rounded-circle bg-success bg-opacity-10 text-success mb-2">
          <i class="fa-solid fa-circle-check fa-3x"></i>
        </div>
        <h3 class="fw-bold text-dark">Emergency Request Recorded</h3>
        <p class="text-muted small">Your incident has been saved in the project system with the tracking reference below.</p>
      </div>

      <!-- Honest System Dispatch Disclaimer -->
      <div class="alert alert-warning border-warning d-flex align-items-center gap-3 py-3 mb-4">
        <i class="fa-solid fa-circle-info fa-2x text-warning"></i>
        <div class="small">
          <strong>Important Dispatch Notice:</strong> Your request has been recorded in this project. 
          Emergency responders have <strong>not</strong> been contacted. This is a demonstration system and is not connected to official emergency dispatch.
        </div>
      </div>

      <div class="token-copy-box">
        <div>
          <small class="text-muted text-uppercase fw-bold">Tracking Reference Code</small>
          <div class="token-display" id="confirmToken">EMG-XXXXXXXX</div>
        </div>
        <button type="button" class="btn btn-outline-danger btn-sm fw-semibold" id="copyTokenBtn">
          <i class="fa-solid fa-copy me-1"></i> Copy Reference
        </button>
      </div>

      <div class="row g-3 py-2 border-top border-bottom my-3">
        <div class="col-sm-6">
          <small class="text-muted d-block">Current Status</small>
          <span class="badge bg-danger fs-6 px-3 py-1" id="confirmStatus">SUBMITTED</span>
        </div>
        <div class="col-sm-6">
          <small class="text-muted d-block">Submission Time</small>
          <span class="fw-semibold text-dark" id="confirmTime">-</span>
        </div>
        <div class="col-sm-6">
          <small class="text-muted d-block">Requested Services</small>
          <span class="fw-semibold text-dark" id="confirmServices">-</span>
        </div>
        <div class="col-sm-6">
          <small class="text-muted d-block">Requested Response Agents</small>
          <span class="fw-semibold text-dark" id="confirmAgents">None</span>
        </div>
        <div class="col-12">
          <small class="text-muted d-block">Submitted Incident Location</small>
          <span class="fw-semibold text-dark" id="confirmLocation">-</span>
        </div>
      </div>

      <div class="mb-4">
        <label class="form-label small text-muted">Public Status Tracking URL (Access without logging in):</label>
        <p class="small"><a href="#" id="confirmTrackLink" target="_blank" class="text-danger fw-bold text-break"></a></p>
      </div>

      <div class="d-flex gap-2 justify-content-end align-items-center flex-wrap">
        <button type="button" class="btn btn-outline-danger btn-delete-request" id="deleteRequestBtn" style="display:none;">
          <i class="fa-solid fa-trash me-1"></i> Delete request
        </button>
        <button type="button" class="btn btn-outline-secondary" onclick="window.location.reload()">
          <i class="fa-solid fa-plus me-1"></i> File Another Incident
        </button>
        <a href="${pageContext.request.contextPath}/ride/home" class="btn btn-danger">
          <i class="fa-solid fa-house me-1"></i> Return to Main Hub
        </a>
      </div>
    </div>

    <!-- 4. Service Selection Section -->
    <div id="serviceSelectionSection">
      <div class="section-title">
        <i class="fa-solid fa-list-check text-danger"></i>
        <span>1. Select Required Emergency Services</span>
      </div>
      <p class="section-subtitle small mb-3">You can select one or more services needed for this emergency incident.</p>

      <div class="services-grid">
        <!-- Service 1: Ambulance -->
        <div class="service-card" data-service="AMBULANCE" tabindex="0" role="checkbox" aria-checked="false">
          <div class="check-indicator"><i class="fa-solid fa-check"></i></div>
          <div class="service-icon"><i class="fa-solid fa-truck-medical"></i></div>
          <h5>Ambulance</h5>
          <p>Medical emergency, paramedic attention, and patient transport.</p>
        </div>

        <!-- Service 2: Fire Vehicle -->
        <div class="service-card" data-service="FIRE_VEHICLE" tabindex="0" role="checkbox" aria-checked="false">
          <div class="check-indicator"><i class="fa-solid fa-check"></i></div>
          <div class="service-icon"><i class="fa-solid fa-fire-extinguisher"></i></div>
          <h5>Fire Vehicle</h5>
          <p>Fire outbreak, hazardous materials, and rescue extraction.</p>
        </div>

        <!-- Service 3: Police Assistance -->
        <div class="service-card" data-service="POLICE_ASSISTANCE" tabindex="0" role="checkbox" aria-checked="false">
          <div class="check-indicator"><i class="fa-solid fa-check"></i></div>
          <div class="service-icon"><i class="fa-solid fa-shield-halved"></i></div>
          <h5>Police Assistance</h5>
          <p>Security protection, crime prevention, and accident containment.</p>
        </div>

        <!-- Service 4: Emergency Response Agents -->
        <div class="service-card" data-service="RESPONSE_AGENTS" tabindex="0" role="checkbox" aria-checked="false">
          <div class="check-indicator"><i class="fa-solid fa-check"></i></div>
          <div class="service-icon"><i class="fa-solid fa-user-shield"></i></div>
          <h5>Response Agents</h5>
          <p>Certified crowd control, scene protection, and incident safety personnel.</p>
        </div>
      </div>

      <!-- Emergency Response Agents Quantity Selector (1 to 5) -->
      <div class="agent-quantity-box" id="agentQuantityBox">
        <div>
          <h6 class="fw-bold mb-1 text-dark">
            <i class="fa-solid fa-user-shield text-danger me-1"></i> Requested Response Agents (1 to 5)
          </h6>
          <small class="text-muted">Specify how many response agents you request. Note: Responders are assigned based on incident assessment.</small>
        </div>
        <div class="d-flex align-items-center gap-2">
          <div class="agent-stepper">
            <button type="button" id="agentMinusBtn" aria-label="Decrease agent count">&minus;</button>
            <input type="text" id="agentCountInput" value="1" readonly aria-label="Agent count value" />
            <button type="button" id="agentPlusBtn" aria-label="Increase agent count">&plus;</button>
          </div>
          <span class="badge bg-secondary">Max 5</span>
        </div>
      </div>
    </div>

    <!-- 5. Location Selection Section -->
    <div id="locationSection">
      <div class="section-title">
        <i class="fa-solid fa-location-dot text-danger"></i>
        <span>2. Pin Incident Location</span>
      </div>
      <p class="section-subtitle small mb-3">Click or drag the pin on the map to pinpoint where assistance is needed, or search for a location in Sri Lanka.</p>

      <div class="map-panel">
        <div class="map-controls">
          <button type="button" class="btn btn-outline-danger btn-sm fw-semibold" id="useGpsBtn">
            <i class="fa-solid fa-location-crosshairs me-1"></i> Use My Current Location
          </button>
          <div class="map-search-group">
            <input type="text" class="form-control form-control-sm" id="mapSearchInput" placeholder="Search address or area in Sri Lanka…" />
            <button type="button" class="btn btn-secondary btn-sm" id="searchLocationBtn">
              <i class="fa-solid fa-magnifying-glass"></i> Search
            </button>
          </div>
        </div>

        <div id="mapErrorBox" class="alert alert-warning py-2 small mb-2" style="display:none;"></div>
        <div id="emergencyMap"></div>

        <div class="location-summary-box">
          <div>
            <span class="small text-muted d-block">Selected Coordinates:</span>
            <span class="fw-bold small" id="coordsDisplay">No location pinned yet (click map or enter address below)</span>
          </div>
          <div class="location-confirmed-badge unconfirmed" id="locationStatusBadge">
            <i class="fa-solid fa-circle-question"></i> Location Not Set
          </div>
        </div>
      </div>
    </div>

    <!-- 6. Incident Request Form -->
    <div class="incident-form-card" id="incidentFormCard">
      <div class="section-title mb-3">
        <i class="fa-solid fa-file-waveform text-danger"></i>
        <span>3. Incident & Contact Details</span>
      </div>

      <div id="formErrorAlert" class="alert alert-danger py-2 small fw-bold mb-3" style="display:none;"></div>

      <form id="emergencyRequestForm">
        <div class="row g-3">
          <div class="col-md-6">
            <label for="locationAddress" class="form-label small fw-semibold text-dark">
              Address / Road Description <span class="text-muted fw-normal">(Auto-filled or manual)</span>
            </label>
            <input type="text" class="form-control" id="locationAddress" placeholder="e.g. Galle Road, Bambalapitiya, Colombo 04" />
          </div>

          <div class="col-md-6">
            <label for="locationLandmark" class="form-label small fw-semibold text-dark">
              Nearby Landmark <span class="text-muted fw-normal">(Optional)</span>
            </label>
            <input type="text" class="form-control" id="locationLandmark" placeholder="e.g. Opposite Majestic City, Near Bus Stand" />
          </div>

          <div class="col-md-6">
            <label for="callbackPhone" class="form-label small fw-semibold text-dark">
              Callback Phone Number <span class="text-danger">*</span>
            </label>
            <input type="tel" class="form-control" id="callbackPhone" placeholder="e.g. 0771234567 or +94771234567" required />
            <div class="form-text small">Required for situation verification and responder coordination.</div>
          </div>

          <div class="col-md-6">
            <label for="contactName" class="form-label small fw-semibold text-dark">
              Your Name <span class="text-muted fw-normal">(Optional)</span>
            </label>
            <input type="text" class="form-control" id="contactName" placeholder="Leave blank if reporting anonymously" />
          </div>

          <div class="col-12">
            <label for="incidentDescription" class="form-label small fw-semibold text-dark">
              Brief Incident Description <span class="text-danger">*</span>
            </label>
            <textarea class="form-control" id="incidentDescription" rows="3" placeholder="Please describe what happened, casualties, hazards, or immediate dangers…" required minlength="5" maxlength="1000"></textarea>
          </div>
        </div>

        <button type="submit" class="submit-btn-emergency" id="submitEmergencyBtn">
          <i class="fa-solid fa-triangle-exclamation me-1"></i> Send Emergency Request
        </button>
      </form>
    </div>

  </div>

  <!-- Deletion Confirmation Accessible Modal -->
  <div class="modal fade" id="deletionConfirmModal" tabindex="-1" aria-labelledby="deletionModalTitle" aria-hidden="true" role="dialog">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header border-bottom">
          <h5 class="modal-title text-danger fw-bold" id="deletionModalTitle">
            <i class="fa-solid fa-triangle-exclamation me-1"></i> Delete Emergency Request
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <form id="deletionConfirmForm" novalidate>
          <div class="modal-body">
            <div class="mb-3">
              <span class="small text-muted d-block">Request Reference:</span>
              <span class="fs-5 fw-bold font-monospace text-dark" id="deleteModalRef">-</span>
            </div>

            <div class="deletion-warning-callout mb-3" role="alert">
              <i class="fa-solid fa-circle-exclamation me-1"></i>
              <strong>Warning:</strong> Deletion cannot be undone through the public interface. 
              Once confirmed, response units will not be dispatched for this incident.
            </div>

            <div id="deleteErrorAlert" class="alert alert-danger py-2 small fw-bold mb-3" style="display:none;" role="alert"></div>

            <div class="mb-3">
              <label for="deletionReasonSelect" class="form-label small fw-semibold text-dark">
                Reason for Deletion <span class="text-danger">*</span>
              </label>
              <select class="form-select" id="deletionReasonSelect" required aria-required="true">
                <option value="" disabled selected>-- Select a reason --</option>
                <option value="Created by mistake">Created by mistake</option>
                <option value="Duplicate request">Duplicate request</option>
                <option value="Assistance no longer required">Assistance no longer required</option>
                <option value="Incorrect information">Incorrect information</option>
                <option value="Other">Other</option>
              </select>
            </div>

            <div class="mb-3" id="deletionNoteGroup">
              <label for="deletionNoteInput" class="form-label small fw-semibold text-dark" id="deletionNoteLabel">
                Additional Explanation <span class="text-muted fw-normal">(Optional)</span>
              </label>
              <textarea class="form-control" id="deletionNoteInput" rows="3" 
                        placeholder="Provide additional details regarding this deletion…" 
                        maxlength="500"></textarea>
            </div>
          </div>
          <div class="modal-footer border-top">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
              Keep request
            </button>
            <button type="submit" class="btn btn-danger fw-bold" id="confirmDeleteSubmitBtn">
              <i class="fa-solid fa-trash me-1"></i> Confirm deletion
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- Leaflet JS -->
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <!-- Bootstrap JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
  <!-- Emergency Script -->
  <script src="${pageContext.request.contextPath}/emergency/js/emergency.js"></script>
</body>
</html>
