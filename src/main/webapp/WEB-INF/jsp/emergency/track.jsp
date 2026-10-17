<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="ctx" content="${pageContext.request.contextPath}">
  <meta name="csrf-token" content="${csrfToken}">
  <title>Track Emergency Request | NEXT GO</title>

  <!-- Bootstrap 5 CSS -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
  <!-- FontAwesome 6 -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
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
        <span>NEXT GO <span class="fw-light text-dark">Emergency Tracking</span></span>
      </a>
      <div class="d-flex align-items-center gap-2">
        <button type="button" class="emergency-theme-toggle" data-emergency-theme-toggle aria-label="Toggle theme" title="Switch light/dark theme">
          🌙
        </button>
        <a href="${pageContext.request.contextPath}/emergency" class="btn btn-outline-danger btn-sm nav-link-btn">
          <i class="fa-solid fa-plus me-1"></i> New Request
        </a>
        <a href="${pageContext.request.contextPath}/ride/home" class="btn btn-outline-dark btn-sm nav-link-btn">
          <i class="fa-solid fa-house me-1"></i> Main Hub
        </a>
      </div>
    </div>
  </nav>

  <div class="emergency-container">

    <!-- Track Form Search Header -->
    <div class="card shadow-sm border-0 mb-4 p-3 rounded-3">
      <form action="${pageContext.request.contextPath}/emergency/track" method="get" class="d-flex gap-2">
        <div class="input-group">
          <span class="input-group-text bg-white border-end-0"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
          <input type="text" name="token" class="form-control border-start-0" 
                 placeholder="Enter emergency tracking token (e.g. EMG-9K3F7W2A)…" 
                 value="<c:out value='${param.token}' />" required />
          <button type="submit" class="btn btn-danger fw-semibold px-4">Track Status</button>
        </div>
      </form>
    </div>

    <!-- If Incident is Deleted: Minimal status message without revealing private details -->
    <c:if test="${found && incident.deleted}">
      <div class="card shadow-sm border-0 rounded-3 p-4 mb-4 text-center">
        <div class="py-4">
          <i class="fa-solid fa-circle-exclamation fa-3x mb-3 text-secondary"></i>
          <h4 class="fw-bold text-dark">This request is no longer available</h4>
          <p class="text-muted small mb-0">The incident reference <strong><c:out value="${incident.trackingToken}" /></strong> is no longer active in the public tracking system.</p>
        </div>
      </div>
    </c:if>

    <!-- If Incident Found and Active -->
    <c:if test="${found && !incident.deleted}">
      <div class="card shadow-sm border-0 rounded-3 p-4 mb-4" id="trackingDetailsCard">
        <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
          <div>
            <span class="badge bg-danger mb-1"><c:out value="${incident.statusDisplayName}" /></span>
            <h3 class="fw-bold text-dark mb-0">Incident: <c:out value="${incident.trackingToken}" /></h3>
            <small class="text-muted">Recorded on: <c:out value="${incident.createdAt}" /></small>
          </div>
          <div class="text-end d-flex align-items-center flex-wrap gap-2">
            <span class="badge bg-light text-dark border p-2">Demonstration Mode</span>
            <input type="hidden" id="trackingPageToken" value="${incident.trackingToken}" data-can-delete="${incident.canDelete}" />
            <button type="button" class="btn btn-outline-primary btn-sm" id="trackUpdateBtn">
              <i class="fa-solid fa-pen-to-square me-1"></i> Update Report
            </button>
            <c:if test="${incident.canDelete}">
              <button type="button" class="btn btn-outline-danger btn-sm" id="trackDeleteBtn">
                <i class="fa-solid fa-trash me-1"></i> Delete Report
              </button>
            </c:if>
          </div>
        </div>

        <!-- Honest Dispatch Notice -->
        <div class="alert alert-warning border-warning d-flex align-items-center gap-3 py-3 mb-4">
          <i class="fa-solid fa-circle-info fa-2x text-warning"></i>
          <div class="small">
            <strong>Demonstration Notice:</strong> <c:out value="${incident.systemNotice}" />
          </div>
        </div>

        <!-- Incident Details Grid -->
        <div class="row g-3 py-2 border-top border-bottom my-2">
          <div class="col-sm-6 col-md-3">
            <small class="text-muted d-block">Requested Services</small>
            <span class="fw-bold text-dark">
              <c:forEach var="svc" items="${incident.requestedServices}" varStatus="status">
                <c:out value="${svc}" /><c:if test="${!status.last}">, </c:if>
              </c:forEach>
            </span>
          </div>
          <div class="col-sm-6 col-md-3">
            <small class="text-muted d-block">Response Agents</small>
            <span class="fw-bold text-dark">
              <c:choose>
                <c:when test="${not empty incident.requestedAgentCount}">
                  <c:out value="${incident.requestedAgentCount}" /> Requested (<c:out value="${incident.assignedAgentsCount}" /> Assigned)
                </c:when>
                <c:otherwise>Not requested</c:otherwise>
              </c:choose>
            </span>
          </div>
          <div class="col-sm-6 col-md-3">
            <small class="text-muted d-block">Reported By</small>
            <span class="fw-bold text-dark"><c:out value="${incident.contactName}" /></span>
          </div>
          <div class="col-sm-6 col-md-3">
            <small class="text-muted d-block">Last Updated</small>
            <span class="fw-bold text-dark" id="displayUpdatedAt"><c:out value="${incident.updatedAt}" /></span>
          </div>
          <div class="col-12">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <small class="text-muted">Incident Location</small>
              <button type="button" class="btn btn-link btn-sm text-primary p-0 text-decoration-none" id="editLocationBtn" onclick="document.getElementById('trackUpdateBtn').click()">
                <i class="fa-solid fa-pen-to-square me-1"></i>Edit
              </button>
            </div>
            <span class="fw-semibold text-dark" id="displayLocationSummary"><c:out value="${incident.locationSummary}" /></span>
          </div>
          <div class="col-12">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <small class="text-muted">Reported Incident Summary</small>
              <button type="button" class="btn btn-link btn-sm text-primary p-0 text-decoration-none" id="editDescriptionBtn" onclick="document.getElementById('trackUpdateBtn').click()">
                <i class="fa-solid fa-pen-to-square me-1"></i>Edit
              </button>
            </div>
            <p class="mb-0 text-dark small bg-light p-2 rounded" id="displayIncidentDesc"><c:out value="${incident.incidentDescription}" /></p>
          </div>
        </div>

        <!-- Status Progression History -->
        <h6 class="fw-bold text-dark mt-4 mb-3"><i class="fa-solid fa-clock-rotate-left text-danger me-1"></i> Status History</h6>
        <div class="timeline-list">
          <c:forEach var="h" items="${incident.history}">
            <div class="timeline-step active">
              <div class="timeline-dot"></div>
              <div class="d-flex justify-content-between align-items-center">
                <strong class="text-dark"><c:out value="${h.status}" /></strong>
                <small class="text-muted"><c:out value="${h.timestamp}" /></small>
              </div>
              <p class="text-muted small mb-0"><c:out value="${h.note}" /></p>
            </div>
          </c:forEach>
        </div>
      </div>
    </c:if>

    <!-- If Token Not Found -->
    <c:if test="${not empty param.token && !found}">
      <div class="alert alert-danger py-4 text-center rounded-3 shadow-sm">
        <i class="fa-solid fa-triangle-exclamation fa-2x mb-2 text-danger"></i>
        <h5 class="fw-bold">No Emergency Request Found</h5>
        <p class="text-muted mb-0 small">
          We could not find any incident matching reference code <strong><c:out value="${searchToken}" /></strong>. 
          Please double-check the tracking code or submit a new emergency request.
        </p>
      </div>
    </c:if>

    <!-- Direct Helpline Fallback -->
    <div class="direct-call-card mt-4">
      <div class="direct-call-header">
        <div class="direct-call-title">
          <i class="fa-solid fa-phone-volume"></i>
          <span>Official Sri Lanka Emergency Contacts</span>
        </div>
        <small class="text-white-50">Free 24/7 Dispatch Helplines</small>
      </div>
      <div class="helpline-grid">
        <a href="tel:119" class="helpline-box">
          <div class="info">
            <h6>Police</h6>
            <div class="number">119</div>
          </div>
          <div class="call-icon"><i class="fa-solid fa-phone"></i></div>
        </a>
        <a href="tel:1990" class="helpline-box">
          <div class="info">
            <h6>Ambulance (1990)</h6>
            <div class="number">1990</div>
          </div>
          <div class="call-icon"><i class="fa-solid fa-phone"></i></div>
        </a>
        <a href="tel:110" class="helpline-box">
          <div class="info">
            <h6>Fire & Rescue</h6>
            <div class="number">110</div>
          </div>
          <div class="call-icon"><i class="fa-solid fa-phone"></i></div>
        </a>
        <a href="tel:117" class="helpline-box">
          <div class="info">
            <h6>Disaster Management</h6>
            <div class="number">117</div>
          </div>
          <div class="call-icon"><i class="fa-solid fa-phone"></i></div>
        </a>
      </div>
    </div>

  </div>

  <!-- Update Incident Accessible Modal -->
  <div class="modal fade" id="updateIncidentModal" tabindex="-1" aria-labelledby="updateModalTitle" aria-hidden="true" role="dialog">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header border-bottom">
          <h5 class="modal-title text-primary fw-bold" id="updateModalTitle">
            <i class="fa-solid fa-pen-to-square me-1"></i> Update Incident Report
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <form id="updateIncidentForm" novalidate>
          <div class="modal-body">
            <div class="mb-3">
              <span class="small text-muted d-block">Incident Reference:</span>
              <span class="fs-5 fw-bold font-monospace text-dark" id="updateModalRef"><c:out value="${incident.trackingToken}" /></span>
            </div>

            <div id="updateErrorAlert" class="alert alert-danger py-2 small fw-bold mb-3" style="display:none;" role="alert"></div>

            <div class="mb-3">
              <label for="updateLocationAddress" class="form-label small fw-semibold text-dark">
                Incident Location / Address <span class="text-danger">*</span>
              </label>
              <input type="text" class="form-control" id="updateLocationAddress" 
                     value="<c:out value='${not empty incident.locationAddress ? incident.locationAddress : incident.locationSummary}' />" 
                     placeholder="e.g. Avissawella Road, Kaduwela, Colombo" required />
              <div class="form-text small">Update the road, street, or area description.</div>
            </div>

            <div class="mb-3">
              <label for="updateLocationLandmark" class="form-label small fw-semibold text-dark">
                Nearby Landmark <span class="text-muted fw-normal">(Optional)</span>
              </label>
              <input type="text" class="form-control" id="updateLocationLandmark" 
                     value="<c:out value='${incident.locationLandmark}' />" 
                     placeholder="e.g. Near clock tower, Opposite gas station" />
            </div>

            <div class="mb-3">
              <label for="updateIncidentDescription" class="form-label small fw-semibold text-dark">
                Reported Incident Description <span class="text-danger">*</span>
              </label>
              <textarea class="form-control" id="updateIncidentDescription" rows="4" 
                        placeholder="Describe what happened or provide updated details." 
                        required minlength="5" maxlength="1000"><c:out value="${incident.incidentDescription}" /></textarea>
              <div class="form-text small">Provide any updated situation details or clarifications.</div>
            </div>
          </div>
          <div class="modal-footer border-top">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
            <button type="submit" class="btn btn-primary fw-bold" id="confirmUpdateSubmitBtn">
              <i class="fa-solid fa-save me-1"></i> Save Changes
            </button>
          </div>
        </form>
      </div>
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

  <!-- Bootstrap JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
  <!-- Emergency Script -->
  <script src="${pageContext.request.contextPath}/emergency/js/emergency.js"></script>
</body>
</html>
