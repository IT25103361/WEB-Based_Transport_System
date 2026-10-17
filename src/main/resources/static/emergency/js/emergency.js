/**
 * NEXT GO - Emergency Services Module Frontend Script
 * Handles service selection, Leaflet interactive map, GPS location,
 * server validation, idempotency, honest demonstration reporting,
 * requester management authorization, soft deletion, and Emergency theme toggling.
 */

(function () {
  "use strict";

  // Context path from meta tag or window
  const metaCtx = document.querySelector('meta[name="ctx"]');
  const contextPath = (metaCtx && metaCtx.content) ? metaCtx.content : "";
  const csrfMeta = document.querySelector('meta[name="csrf-token"]');
  let csrfToken = csrfMeta ? csrfMeta.content : "";

  // Emergency Theme Preference Key
  const THEME_KEY = "nextgo.emergency.theme";

  // Client-side idempotency key for this incident session
  let idempotencyKey = (window.crypto && crypto.randomUUID)
    ? crypto.randomUUID()
    : "idemp-" + Date.now() + "-" + Math.random().toString(36).substring(2, 10);

  // State
  const selectedServices = new Set();
  let agentCount = 1;
  let selectedLat = null;
  let selectedLng = null;
  let isLocationConfirmed = false;
  let leafletMap = null;
  let emergencyMarker = null;

  // Requester management state for active deletion
  let activeTrackingToken = null;
  let activeManagementToken = null;
  let deletionModalInstance = null;

  /* ---------------- Immediate Theme Setup ---------------- */
  function applyTheme(theme) {
    if (window.NextGoTheme) {
      window.NextGoTheme.applyTheme(theme);
      return;
    }
    document.documentElement.setAttribute("data-theme", theme);
    document.documentElement.setAttribute("data-bs-theme", theme);
    document.querySelectorAll("[data-emergency-theme-toggle]").forEach(btn => {
      btn.innerHTML = theme === "dark" ? "☀️" : "🌙";
      btn.setAttribute("aria-label", theme === "dark" ? "Switch to light theme" : "Switch to dark theme");
      btn.setAttribute("title", theme === "dark" ? "Switch to light theme" : "Switch to dark theme");
      btn.setAttribute("aria-pressed", theme === "dark" ? "true" : "false");
    });
  }

  function initTheme() {
    if (window.NextGoTheme) {
      // Centrally managed by nextgo-theme.js
      return;
    }

    let saved = null;
    try {
      saved = localStorage.getItem("nextgo.theme") || localStorage.getItem(THEME_KEY);
    } catch (e) {}

    if (!saved) {
      saved = (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches) ? "dark" : "light";
    }
    applyTheme(saved);

    document.addEventListener("click", (e) => {
      const btn = e.target.closest("[data-emergency-theme-toggle]");
      if (!btn) return;
      e.preventDefault();

      const current = document.documentElement.getAttribute("data-theme") || "light";
      const next = current === "dark" ? "light" : "dark";
      try {
        localStorage.setItem("nextgo.theme", next);
        localStorage.setItem(THEME_KEY, next);
      } catch (err) {}
      applyTheme(next);
    });
  }

  // Initialize theme immediately
  initTheme();

  document.addEventListener("DOMContentLoaded", () => {
    initServiceSelection();
    initAgentStepper();
    initEmergencyMap();
    initLocationControls();
    initFormSubmission();
    initDeletionFlow();
    initUpdateFlow();
    initTrackingPageManagement();
    ensureCsrfToken();
  });

  async function ensureCsrfToken() {
    if (!csrfToken) {
      try {
        const res = await fetch(contextPath + "/api/emergency/csrf");
        if (res.ok) {
          const data = await res.json();
          csrfToken = data.csrfToken;
        }
      } catch (e) {
        console.warn("Could not fetch CSRF token:", e);
      }
    }
  }

  /* ---------------- 1. Service Selection ---------------- */
  function initServiceSelection() {
    const cards = document.querySelectorAll(".service-card");
    const agentBox = document.getElementById("agentQuantityBox");
    if (!cards.length) return;

    cards.forEach(card => {
      card.addEventListener("click", () => {
        const serviceKey = card.getAttribute("data-service");
        if (selectedServices.has(serviceKey)) {
          selectedServices.delete(serviceKey);
          card.classList.remove("selected");
          card.setAttribute("aria-checked", "false");
        } else {
          selectedServices.add(serviceKey);
          card.classList.add("selected");
          card.setAttribute("aria-checked", "true");
        }

        // Toggle Response Agents quantity box
        if (selectedServices.has("RESPONSE_AGENTS")) {
          agentBox.classList.add("active");
        } else {
          agentBox.classList.remove("active");
        }
      });
    });
  }

  /* ---------------- 2. Agent Stepper (1 to 5) ---------------- */
  function initAgentStepper() {
    const input = document.getElementById("agentCountInput");
    const btnMinus = document.getElementById("agentMinusBtn");
    const btnPlus = document.getElementById("agentPlusBtn");
    if (!input || !btnMinus || !btnPlus) return;

    function updateCount(val) {
      val = parseInt(val, 10);
      if (isNaN(val) || val < 1) val = 1;
      if (val > 5) val = 5;
      agentCount = val;
      input.value = val;
      btnMinus.disabled = val <= 1;
      btnPlus.disabled = val >= 5;
    }

    btnMinus.addEventListener("click", () => updateCount(agentCount - 1));
    btnPlus.addEventListener("click", () => updateCount(agentCount + 1));
    input.addEventListener("change", () => updateCount(input.value));
    updateCount(1);
  }

  /* ---------------- 3. Leaflet Map & Location ---------------- */
  function initEmergencyMap() {
    const mapEl = document.getElementById("emergencyMap");
    if (!mapEl || typeof L === "undefined") return;

    // Default center: Colombo, Western Province, Sri Lanka
    const defaultCenter = [6.9271, 79.8612];

    try {
      leafletMap = L.map("emergencyMap").setView(defaultCenter, 12);

      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 19
      }).addTo(leafletMap);

      // Custom high-contrast emergency marker icon
      const emergencyIcon = L.divIcon({
        className: "custom-emg-marker",
        html: '<div style="background-color:#dc2626; color:#ffffff; width:34px; height:34px; border-radius:50%; display:flex; align-items:center; justify-content:center; border:3px solid #ffffff; box-shadow:0 3px 8px rgba(0,0,0,0.4);"><i class="fa-solid fa-location-dot" style="font-size:18px;"></i></div>',
        iconSize: [34, 34],
        iconAnchor: [17, 34]
      });

      // Map click places or moves the pin
      leafletMap.on("click", (e) => {
        placeMarker(e.latlng.lat, e.latlng.lng, emergencyIcon, true);
      });
    } catch (err) {
      console.error("Map initialization failed:", err);
      showMapError("Interactive map could not load. You can enter your incident address manually below.");
    }
  }

  function placeMarker(lat, lng, icon, fetchReverseGeocode) {
    selectedLat = parseFloat(lat.toFixed(6));
    selectedLng = parseFloat(lng.toFixed(6));
    isLocationConfirmed = true;

    if (!emergencyMarker && leafletMap) {
      emergencyMarker = L.marker([selectedLat, selectedLng], {
        draggable: true,
        icon: icon || new L.Icon.Default()
      }).addTo(leafletMap);

      emergencyMarker.on("dragend", (event) => {
        const pos = event.target.getLatLng();
        placeMarker(pos.lat, pos.lng, icon, true);
      });
    } else if (emergencyMarker) {
      emergencyMarker.setLatLng([selectedLat, selectedLng]);
    }

    updateLocationDisplay(selectedLat, selectedLng);

    if (fetchReverseGeocode) {
      reverseGeocode(selectedLat, selectedLng);
    }
  }

  function updateLocationDisplay(lat, lng, addressText) {
    const coordsEl = document.getElementById("coordsDisplay");
    const badgeEl = document.getElementById("locationStatusBadge");
    const addressInput = document.getElementById("locationAddress");

    if (coordsEl) {
      coordsEl.textContent = `Latitude: ${lat}, Longitude: ${lng}`;
    }
    if (badgeEl) {
      badgeEl.className = "location-confirmed-badge confirmed";
      badgeEl.innerHTML = '<i class="fa-solid fa-circle-check"></i> Location Set on Map';
    }
    if (addressText && addressInput && !addressInput.value.trim()) {
      addressInput.value = addressText;
    }
  }

  /* ---------------- 4. Location Controls & GPS ---------------- */
  function initLocationControls() {
    const gpsBtn = document.getElementById("useGpsBtn");
    const searchBtn = document.getElementById("searchLocationBtn");
    const searchInput = document.getElementById("mapSearchInput");

    if (gpsBtn) {
      gpsBtn.addEventListener("click", () => {
        if (!navigator.geolocation) {
          alert("Geolocation is not supported by your browser. Please tap directly on the map or type an address.");
          return;
        }

        gpsBtn.disabled = true;
        gpsBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-1"></i> Acquiring GPS…';

        navigator.geolocation.getCurrentPosition(
          (position) => {
            gpsBtn.disabled = false;
            gpsBtn.innerHTML = '<i class="fa-solid fa-location-crosshairs me-1"></i> Use My Current Location';
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            if (leafletMap) {
              leafletMap.setView([lat, lng], 16);
              placeMarker(lat, lng, null, true);
            }
          },
          (err) => {
            gpsBtn.disabled = false;
            gpsBtn.innerHTML = '<i class="fa-solid fa-location-crosshairs me-1"></i> Use My Current Location';
            let message = "Could not retrieve your location. ";
            if (err.code === 1) {
              message += "Location permission was denied. You can select your incident location directly on the map.";
            } else if (err.code === 2) {
              message += "Position unavailable. Please pick your location on the map.";
            } else if (err.code === 3) {
              message += "Location request timed out. Please select on the map.";
            }
            alert(message);
          },
          { timeout: 10000, enableHighAccuracy: true, maximumAge: 0 }
        );
      });
    }

    if (searchBtn && searchInput) {
      const doSearch = async () => {
        const query = searchInput.value.trim();
        if (!query) return;

        searchBtn.disabled = true;
        searchBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>';

        try {
          const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&countrycodes=lk&limit=1`;
          const res = await fetch(url, { headers: { "Accept-Language": "en" } });
          const results = await res.json();

          if (results && results.length > 0) {
            const first = results[0];
            const lat = parseFloat(first.lat);
            const lng = parseFloat(first.lon);
            if (leafletMap) {
              leafletMap.setView([lat, lng], 15);
              placeMarker(lat, lng, null, false);
              updateLocationDisplay(lat, lng, first.display_name);
            }
          } else {
            alert("No matching locations found in Sri Lanka. Please click directly on the map to set the point.");
          }
        } catch (e) {
          console.warn("Geocoding service unavailable:", e);
          alert("Address search is currently offline. Please tap on the map to set your location pin.");
        } finally {
          searchBtn.disabled = false;
          searchBtn.innerHTML = '<i class="fa-solid fa-magnifying-glass"></i> Search';
        }
      };

      searchBtn.addEventListener("click", doSearch);
      searchInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          doSearch();
        }
      });
    }
  }

  async function reverseGeocode(lat, lng) {
    try {
      const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`;
      const res = await fetch(url, { headers: { "Accept-Language": "en" } });
      const data = await res.json();
      if (data && data.display_name) {
        updateLocationDisplay(lat, lng, data.display_name);
      }
    } catch (e) {}
  }

  function showMapError(msg) {
    const mapBox = document.getElementById("mapErrorBox");
    if (mapBox) {
      mapBox.textContent = msg;
      mapBox.style.display = "block";
    }
  }

  /* ---------------- 5. Request Submission ---------------- */
  function initFormSubmission() {
    const form = document.getElementById("emergencyRequestForm");
    const submitBtn = document.getElementById("submitEmergencyBtn");
    const errorBox = document.getElementById("formErrorAlert");
    const successPanel = document.getElementById("confirmationPanel");

    if (!form || !submitBtn) return;

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      errorBox.style.display = "none";
      errorBox.textContent = "";

      // Client Validations
      if (selectedServices.size === 0) {
        showError("Please select at least one emergency service (e.g. Ambulance, Fire, Police, or Response Agents).");
        return;
      }

      const addressVal = document.getElementById("locationAddress").value.trim();
      const landmarkVal = document.getElementById("locationLandmark").value.trim();

      if (!isLocationConfirmed && !addressVal && !landmarkVal) {
        showError("Please set your emergency location on the map or type an address / landmark.");
        return;
      }

      const phoneVal = document.getElementById("callbackPhone").value.trim();
      if (!phoneVal) {
        showError("Please provide a callback phone number so responders can reach you.");
        return;
      }

      const descVal = document.getElementById("incidentDescription").value.trim();
      if (!descVal || descVal.length < 5) {
        showError("Please describe the incident briefly (at least 5 characters).");
        return;
      }

      // Build Payload
      const payload = {
        services: Array.from(selectedServices),
        agentCount: selectedServices.has("RESPONSE_AGENTS") ? agentCount : null,
        latitude: selectedLat,
        longitude: selectedLng,
        locationAddress: addressVal || null,
        locationLandmark: landmarkVal || null,
        contactName: document.getElementById("contactName").value.trim() || null,
        callbackPhone: phoneVal,
        incidentDescription: descVal,
        idempotencyKey: idempotencyKey,
        csrfToken: csrfToken
      };

      // Disable button & show spinner
      submitBtn.disabled = true;
      submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-2"></i> Recording Request…';

      try {
        const res = await fetch(contextPath + "/api/emergency/request", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
          },
          body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (!res.ok) {
          throw new Error(data.error || "Failed to submit emergency request.");
        }

        // Store the separate management token locally for authorized deletion
        if (data.managementToken && data.trackingToken) {
          activeTrackingToken = data.trackingToken;
          activeManagementToken = data.managementToken;
          try {
            localStorage.setItem("nextgo.emergency.mgmt." + data.trackingToken, data.managementToken);
          } catch (e) {}
        }

        // Display Confirmation Screen
        renderConfirmation(data);
      } catch (err) {
        showError(err.message || "An error occurred while submitting. Please call direct emergency numbers if urgent.");
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fa-solid fa-triangle-exclamation me-2"></i> Send Emergency Request';
      }
    });

    function showError(msg) {
      errorBox.textContent = msg;
      errorBox.style.display = "block";
      errorBox.scrollIntoView({ behavior: "smooth", block: "center" });
    }

    function renderConfirmation(incident) {
      form.style.display = "none";
      const serviceSection = document.getElementById("serviceSelectionSection");
      const mapSection = document.getElementById("locationSection");
      const formCard = document.getElementById("incidentFormCard");

      if (serviceSection) serviceSection.style.display = "none";
      if (mapSection) mapSection.style.display = "none";
      if (formCard) formCard.style.display = "none";

      successPanel.style.display = "block";
      successPanel.scrollIntoView({ behavior: "smooth", block: "start" });

      document.getElementById("confirmToken").textContent = incident.trackingToken;
      document.getElementById("confirmStatus").textContent = incident.statusDisplayName || incident.status;
      document.getElementById("confirmServices").textContent = incident.requestedServices ? incident.requestedServices.join(", ") : "Recorded";
      
      const agentEl = document.getElementById("confirmAgents");
      if (incident.requestedAgentCount) {
        agentEl.textContent = `${incident.requestedAgentCount} Requested (${incident.assignedAgentsCount || 0} Assigned)`;
      } else {
        agentEl.textContent = "N/A";
      }

      document.getElementById("confirmLocation").textContent = incident.locationSummary || "Recorded";
      document.getElementById("confirmTime").textContent = new Date().toLocaleString();

      const trackUrl = contextPath + "/emergency/track?token=" + encodeURIComponent(incident.trackingToken);
      const trackLink = document.getElementById("confirmTrackLink");
      if (trackLink) {
        trackLink.href = trackUrl;
        trackLink.textContent = window.location.origin + trackUrl;
      }

      // Show Delete Request button on confirmation panel if eligible
      const deleteBtn = document.getElementById("deleteRequestBtn");
      if (deleteBtn) {
        deleteBtn.style.display = "inline-flex";
        deleteBtn.onclick = () => {
          openDeletionModal(incident.trackingToken, activeManagementToken);
        };
      }

      // Copy token helper
      const copyBtn = document.getElementById("copyTokenBtn");
      if (copyBtn) {
        copyBtn.onclick = () => {
          navigator.clipboard.writeText(incident.trackingToken).then(() => {
            copyBtn.innerHTML = '<i class="fa-solid fa-check me-1"></i> Copied!';
            setTimeout(() => {
              copyBtn.innerHTML = '<i class="fa-solid fa-copy me-1"></i> Copy Reference';
            }, 2500);
          });
        };
      }
    }
  }

  /* ---------------- 6. Deletion Modal Flow ---------------- */
  function openDeletionModal(trackingToken, managementToken) {
    activeTrackingToken = trackingToken;
    activeManagementToken = managementToken;

    const modalRef = document.getElementById("deleteModalRef");
    const reasonSelect = document.getElementById("deletionReasonSelect");
    const noteInput = document.getElementById("deletionNoteInput");
    const errorAlert = document.getElementById("deleteErrorAlert");
    const submitBtn = document.getElementById("confirmDeleteSubmitBtn");

    if (modalRef) modalRef.textContent = trackingToken;
    if (reasonSelect) reasonSelect.value = "";
    if (noteInput) {
      noteInput.value = "";
      noteInput.required = false;
    }
    if (errorAlert) {
      errorAlert.style.display = "none";
      errorAlert.textContent = "";
    }
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<i class="fa-solid fa-trash me-1"></i> Confirm deletion';
    }

    const modalEl = document.getElementById("deletionConfirmModal");
    if (modalEl && typeof bootstrap !== "undefined") {
      if (!deletionModalInstance) {
        deletionModalInstance = new bootstrap.Modal(modalEl);
      }
      deletionModalInstance.show();
    }
  }

  function initDeletionFlow() {
    const modalEl = document.getElementById("deletionConfirmModal");
    const form = document.getElementById("deletionConfirmForm");
    const reasonSelect = document.getElementById("deletionReasonSelect");
    const noteGroup = document.getElementById("deletionNoteGroup");
    const noteLabel = document.getElementById("deletionNoteLabel");
    const noteInput = document.getElementById("deletionNoteInput");
    const errorAlert = document.getElementById("deleteErrorAlert");
    const submitBtn = document.getElementById("confirmDeleteSubmitBtn");

    if (!modalEl || !form) return;

    if (typeof bootstrap !== "undefined") {
      deletionModalInstance = new bootstrap.Modal(modalEl);
    }

    // Toggle mandatory note when "Other" is selected
    if (reasonSelect) {
      reasonSelect.addEventListener("change", () => {
        const val = reasonSelect.value;
        if (val === "Other") {
          noteLabel.innerHTML = 'Additional Explanation <span class="text-danger">* (Mandatory for "Other", min 5 chars)</span>';
          noteInput.required = true;
          noteInput.setAttribute("aria-required", "true");
        } else {
          noteLabel.innerHTML = 'Additional Explanation <span class="text-muted fw-normal">(Optional)</span>';
          noteInput.required = false;
          noteInput.removeAttribute("aria-required");
        }
      });
    }

    // Handle Deletion Form Submit
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      errorAlert.style.display = "none";
      errorAlert.textContent = "";

      const reason = reasonSelect.value;
      const note = noteInput.value.trim();

      if (!reason) {
        showDeleteError("Please select a mandatory deletion reason.");
        return;
      }

      if (reason === "Other" && (!note || note.length < 5)) {
        showDeleteError("Please provide an explanation when selecting 'Other' (at least 5 characters).");
        return;
      }

      // If management token is missing in memory, look up in localStorage
      if (!activeManagementToken && activeTrackingToken) {
        try {
          activeManagementToken = localStorage.getItem("nextgo.emergency.mgmt." + activeTrackingToken);
        } catch (err) {}
      }

      submitBtn.disabled = true;
      submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-1"></i> Deleting…';

      try {
        const res = await fetch(contextPath + "/api/emergency/request/delete", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
          },
          body: JSON.stringify({
            trackingToken: activeTrackingToken,
            managementToken: activeManagementToken || null,
            reason: reason,
            note: note || null,
            csrfToken: csrfToken
          })
        });

        const data = await res.json();

        if (!res.ok) {
          throw new Error(data.error || "Failed to delete emergency request.");
        }

        // Successfully deleted: clear local token
        try {
          localStorage.removeItem("nextgo.emergency.mgmt." + activeTrackingToken);
        } catch (err) {}

        if (deletionModalInstance) {
          deletionModalInstance.hide();
        }

        // Update UI
        handlePostDeletionUI(activeTrackingToken);
      } catch (err) {
        showDeleteError(err.message || "An error occurred while deleting the request.");
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fa-solid fa-trash me-1"></i> Confirm deletion';
      }
    });

    function showDeleteError(msg) {
      errorAlert.textContent = msg;
      errorAlert.style.display = "block";
    }
  }

  function handlePostDeletionUI(token) {
    // If on index.jsp confirmation panel
    const confPanel = document.getElementById("confirmationPanel");
    if (confPanel) {
      confPanel.innerHTML = `
        <div class="text-center py-4">
          <div class="d-inline-flex p-3 rounded-circle bg-secondary bg-opacity-10 text-secondary mb-3">
            <i class="fa-solid fa-trash-can fa-3x"></i>
          </div>
          <h3 class="fw-bold text-dark">Emergency Request Deleted</h3>
          <p class="text-muted small">Incident <b>${token}</b> was removed by the requester. Responders will not be assigned.</p>
          <div class="mt-4">
            <button type="button" class="btn btn-outline-danger" onclick="window.location.reload()">
              <i class="fa-solid fa-plus me-1"></i> Submit New Request
            </button>
            <a href="${contextPath}/ride/home" class="btn btn-secondary ms-2">
              <i class="fa-solid fa-house me-1"></i> Return to Main Hub
            </a>
          </div>
        </div>
      `;
      return;
    }

    // If on tracking page
    const trackCard = document.getElementById("trackingDetailsCard");
    if (trackCard) {
      trackCard.innerHTML = `
        <div class="alert alert-secondary py-4 text-center rounded-3 shadow-sm">
          <i class="fa-solid fa-circle-exclamation fa-2x mb-2 text-secondary"></i>
          <h5 class="fw-bold">This request is no longer available</h5>
          <p class="text-muted mb-0 small">The incident <b>${token}</b> has been deleted by the requester.</p>
        </div>
      `;
    }
  }

  /* ---------------- 7. Tracking Page Management Check ---------------- */
  function initTrackingPageManagement() {
    const trackTokenEl = document.getElementById("trackingPageToken");
    if (!trackTokenEl) return;

    const token = trackTokenEl.value;
    if (!token) return;

    let mgmtToken = null;
    try {
      mgmtToken = localStorage.getItem("nextgo.emergency.mgmt." + token);
    } catch (e) {}

    const deleteBtn = document.getElementById("trackDeleteBtn");
    if (deleteBtn) {
      deleteBtn.addEventListener("click", () => {
        openDeletionModal(token, mgmtToken);
      });
    }

    const updateBtn = document.getElementById("trackUpdateBtn");
    if (updateBtn) {
      updateBtn.addEventListener("click", () => {
        openUpdateModal(token, mgmtToken);
      });
    }
  }

  /* ---------------- 8. Update Modal Flow ---------------- */
  let updateModalInstance = null;

  function openUpdateModal(trackingToken, managementToken) {
    activeTrackingToken = trackingToken;
    activeManagementToken = managementToken || null;

    const modalRef = document.getElementById("updateModalRef");
    const errorAlert = document.getElementById("updateErrorAlert");
    const submitBtn = document.getElementById("confirmUpdateSubmitBtn");

    if (modalRef) modalRef.textContent = trackingToken;
    if (errorAlert) {
      errorAlert.style.display = "none";
      errorAlert.textContent = "";
    }
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<i class="fa-solid fa-save me-1"></i> Save Changes';
    }

    const modalEl = document.getElementById("updateIncidentModal");
    if (modalEl && typeof bootstrap !== "undefined") {
      if (!updateModalInstance) {
        updateModalInstance = new bootstrap.Modal(modalEl);
      }
      updateModalInstance.show();
    }
  }

  function initUpdateFlow() {
    const modalEl = document.getElementById("updateIncidentModal");
    const form = document.getElementById("updateIncidentForm");
    const addrInput = document.getElementById("updateLocationAddress");
    const lmarkInput = document.getElementById("updateLocationLandmark");
    const descInput = document.getElementById("updateIncidentDescription");
    const errorAlert = document.getElementById("updateErrorAlert");
    const submitBtn = document.getElementById("confirmUpdateSubmitBtn");

    if (!modalEl || !form) return;

    if (typeof bootstrap !== "undefined") {
      updateModalInstance = new bootstrap.Modal(modalEl);
    }

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (errorAlert) {
        errorAlert.style.display = "none";
        errorAlert.textContent = "";
      }

      const address = addrInput ? addrInput.value.trim() : "";
      const landmark = lmarkInput ? lmarkInput.value.trim() : "";
      const description = descInput ? descInput.value.trim() : "";

      if (!address) {
        showUpdateError("Please provide an incident location address.");
        return;
      }
      if (!description || description.length < 5) {
        showUpdateError("Please provide an incident description with at least 5 characters.");
        return;
      }

      if (!activeManagementToken && activeTrackingToken) {
        try {
          activeManagementToken = localStorage.getItem("nextgo.emergency.mgmt." + activeTrackingToken);
        } catch (err) {}
      }

      submitBtn.disabled = true;
      submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-1"></i> Saving…';

      try {
        const res = await fetch(contextPath + "/api/emergency/request/update", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
          },
          body: JSON.stringify({
            trackingToken: activeTrackingToken,
            managementToken: activeManagementToken || null,
            locationAddress: address,
            locationLandmark: landmark || null,
            incidentDescription: description,
            csrfToken: csrfToken
          })
        });

        const data = await res.json();
        if (!res.ok) {
          throw new Error(data.error || "Failed to update incident report.");
        }

        if (updateModalInstance) {
          updateModalInstance.hide();
        }

        // Live update the elements on the tracking card if present
        const locSummaryEl = document.getElementById("displayLocationSummary");
        if (locSummaryEl) locSummaryEl.textContent = data.incident.locationSummary || address;

        const descEl = document.getElementById("displayIncidentDesc");
        if (descEl) descEl.textContent = data.incident.incidentDescription || description;

        const updatedEl = document.getElementById("displayUpdatedAt");
        if (updatedEl && data.incident.updatedAt) updatedEl.textContent = data.incident.updatedAt;

        // Reload so status history is updated cleanly
        window.location.reload();
      } catch (err) {
        showUpdateError(err.message || "An error occurred while updating the report.");
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fa-solid fa-save me-1"></i> Save Changes';
      }
    });

    function showUpdateError(msg) {
      if (errorAlert) {
        errorAlert.textContent = msg;
        errorAlert.style.display = "block";
      }
    }
  }

  // Export for global access if needed
  window.openEmergencyDeletionModal = openDeletionModal;
  window.openEmergencyUpdateModal = openUpdateModal;

})();
