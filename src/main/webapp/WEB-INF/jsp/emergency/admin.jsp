<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="ctx" content="${pageContext.request.contextPath}">
  <title>Emergency Operations Console | NEXT GO Admin</title>

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
      <a href="${pageContext.request.contextPath}/emergency/admin" class="navbar-brand">
        <i class="fa-solid fa-tower-broadcast"></i>
        <span>NEXT GO <span class="fw-light text-dark">Emergency Operations Console</span></span>
      </a>
      <div class="d-flex align-items-center gap-2">
        <button type="button" class="emergency-theme-toggle" data-emergency-theme-toggle aria-label="Toggle theme" title="Switch light/dark theme">
          🌙
        </button>
        <span class="badge bg-danger"><i class="fa-solid fa-lock me-1"></i> Admin Authorized</span>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline-dark btn-sm nav-link-btn">
          <i class="fa-solid fa-gauge me-1"></i> Admin Portal
        </a>
      </div>
    </div>
  </nav>

  <div class="emergency-container">

    <!-- Top Summary Banner -->
    <div class="card shadow-sm border-0 mb-4 p-4 rounded-3 bg-dark text-white">
      <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div>
          <h4 class="fw-bold mb-1"><i class="fa-solid fa-shield-halved text-danger me-2"></i>Emergency Operational Console</h4>
          <p class="text-white-50 small mb-0">Authorized administration and responder management for recorded emergency incidents.</p>
        </div>
        <button type="button" class="btn btn-outline-light btn-sm" id="refreshBtn" onclick="loadRequests()">
          <i class="fa-solid fa-rotate me-1"></i> Refresh Requests
        </button>
      </div>
    </div>

    <!-- Requests Table Card -->
    <div class="card shadow-sm border-0 rounded-3 p-4 mb-4">
      <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
        <h5 class="fw-bold text-dark mb-0">Recorded Incidents</h5>
        <div class="d-flex align-items-center gap-2">
          <select id="filterType" class="form-select form-select-sm" style="width: auto;" onchange="loadRequests()">
            <option value="active" selected>Active Incidents</option>
            <option value="all">All (Audit History & Deleted)</option>
          </select>
          <div id="requestCountBadge" class="badge bg-secondary">Loading…</div>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead class="table-light">
            <tr>
              <th>Reference</th>
              <th>Services</th>
              <th>Location</th>
              <th>Contact Phone</th>
              <th>Status</th>
              <th>Agents</th>
              <th>Time</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody id="requestsTableBody">
            <tr>
              <td colspan="8" class="text-center py-4 text-muted">
                <i class="fa-solid fa-spinner fa-spin me-2"></i> Loading incident records…
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>

  <!-- Operational Update Modal -->
  <div class="modal fade" id="updateModal" tabindex="-1" aria-labelledby="updateModalLabel" aria-hidden="true">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="updateModalLabel">Manage Incident: <span id="modalRef"></span></h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <form id="updateStatusForm">
          <input type="hidden" id="modalIncidentId" />
          <div class="modal-body">
            <div class="mb-3">
              <label for="modalStatus" class="form-label small fw-bold">Update Operational Status</label>
              <select class="form-select" id="modalStatus" required>
                <option value="SUBMITTED">SUBMITTED</option>
                <option value="UNDER_REVIEW">UNDER_REVIEW</option>
                <option value="RESPONDERS_ASSIGNED">RESPONDERS_ASSIGNED</option>
                <option value="ON_SCENE">ON_SCENE</option>
                <option value="RESOLVED">RESOLVED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>
            <div class="mb-3">
              <label for="modalAgents" class="form-label small fw-bold">Assigned Responders / Agents</label>
              <input type="number" class="form-control" id="modalAgents" min="0" max="5" value="0" />
            </div>
            <div class="mb-3">
              <label for="modalNote" class="form-label small fw-bold">Operational Note</label>
              <textarea class="form-control" id="modalNote" rows="2" placeholder="e.g. Unit dispatched, situation contained…"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
            <button type="submit" class="btn btn-danger btn-sm fw-bold" id="modalSaveBtn">Save Updates</button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- Bootstrap JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
  <!-- Emergency Script -->
  <script src="${pageContext.request.contextPath}/emergency/js/emergency.js"></script>

  <script>
    const metaCtx = document.querySelector('meta[name="ctx"]');
    const ctx = metaCtx ? metaCtx.content : "";
    let updateModalInstance = null;

    document.addEventListener("DOMContentLoaded", () => {
      updateModalInstance = new bootstrap.Modal(document.getElementById("updateModal"));
      loadRequests();
      initModalForm();
    });

    async function loadRequests() {
      const tbody = document.getElementById("requestsTableBody");
      const countBadge = document.getElementById("requestCountBadge");
      const filter = document.getElementById("filterType") ? document.getElementById("filterType").value : "active";
      const includeDeleted = filter === "all";

      try {
        const res = await fetch(ctx + "/api/emergency/admin/requests?includeDeleted=" + includeDeleted);
        if (!res.ok) {
          throw new Error("HTTP " + res.status + " " + res.statusText);
        }
        const list = await res.json();
        countBadge.textContent = list.length + (includeDeleted ? " Total Records" : " Active Incidents");

        if (list.length === 0) {
          tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">No emergency requests found.</td></tr>';
          return;
        }

        tbody.innerHTML = list.map(item => `
          <tr class="${item.isDeleted ? 'opacity-75' : ''}">
            <td>
              <span class="fw-bold font-monospace text-danger">${item.trackingToken}</span>
              ${item.isDeleted ? '<br><span class="badge bg-secondary" style="font-size: 0.7rem;">Soft-Deleted Audit</span>' : ''}
            </td>
            <td><small class="badge bg-light text-dark border">${(item.requestedServices || []).join(", ")}</small></td>
            <td><small>${item.locationAddress || (item.latitude ? item.latitude + ", " + item.longitude : "Recorded")}</small></td>
            <td><a href="tel:${item.callbackPhone}" class="text-decoration-none fw-semibold">${item.callbackPhone}</a></td>
            <td>
              <span class="badge ${getStatusBadgeClass(item.status)}">${item.status}</span>
              ${item.isDeleted && item.deletionReason ? `<div class="small text-muted mt-1" style="font-size:0.75rem;"><b>Reason:</b> ${item.deletionReason}${item.deletionNote ? ' (' + item.deletionNote + ')' : ''}</div>` : ''}
            </td>
            <td><small>${item.assignedAgentsCount || 0} / ${item.requestedAgentCount || 0}</small></td>
            <td><small class="text-muted">${item.createdAt ? new Date(item.createdAt).toLocaleTimeString() : "-"}</small></td>
            <td>
              ${item.isDeleted ? `
                <button class="btn btn-sm btn-outline-secondary" disabled title="Deleted by ${item.deletedByActorType || 'requester'} on ${item.deletedAt || ''}">
                  <i class="fa-solid fa-ban"></i>
                </button>
              ` : `
                <button class="btn btn-sm btn-outline-danger" onclick="openUpdateModal(${item.id}, '${item.trackingToken}', '${item.status}', ${item.assignedAgentsCount || 0})">
                  <i class="fa-solid fa-pen-to-square"></i>
                </button>
              `}
            </td>
          </tr>
        `).join("");
      } catch (err) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-danger text-center py-4">Failed to load emergency data: ${err.message}</td></tr>`;
      }
    }

    function getStatusBadgeClass(status) {
      switch(status) {
        case "SUBMITTED": return "bg-danger";
        case "UNDER_REVIEW": return "bg-warning text-dark";
        case "RESPONDERS_ASSIGNED": return "bg-info text-dark";
        case "ON_SCENE": return "bg-primary";
        case "RESOLVED": return "bg-success";
        case "CANCELLED": return "bg-secondary";
        case "DELETED": return "bg-secondary text-white";
        default: return "bg-dark";
      }
    }

    function openUpdateModal(id, token, status, agents) {
      document.getElementById("modalIncidentId").value = id;
      document.getElementById("modalRef").textContent = token;
      document.getElementById("modalStatus").value = status;
      document.getElementById("modalAgents").value = agents;
      document.getElementById("modalNote").value = "";
      updateModalInstance.show();
    }

    function initModalForm() {
      const form = document.getElementById("updateStatusForm");
      form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const id = document.getElementById("modalIncidentId").value;
        const status = document.getElementById("modalStatus").value;
        const agents = parseInt(document.getElementById("modalAgents").value, 10);
        const note = document.getElementById("modalNote").value;

        try {
          const res = await fetch(ctx + "/api/emergency/admin/requests/" + id + "/status", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ status, assignedAgents: agents, note })
          });
          if (!res.ok) throw new Error("Failed to update status");
          updateModalInstance.hide();
          loadRequests();
        } catch (err) {
          alert("Error updating incident: " + err.message);
        }
      });
    }
  </script>
</body>
</html>
