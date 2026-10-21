(function () {
  const params = new URLSearchParams(window.location.search);
  const initialCode = params.get("code") || params.get("trackingCode");
  const codeInput = document.getElementById("codeInput");
  const searchBtn = document.getElementById("searchBtn");
  const result = document.getElementById("result");

  const CODE_REGEX = /^CR-[A-Za-z0-9]{4,12}$/i;
  const STEPS = ["REQUESTED", "MATCHED", "PICKED_UP", "IN_TRANSIT", "ARRIVED", "DELIVERED"];
  const TERMINAL = ["DELIVERED", "CANCELLED", "FAILED", "RETURNED"];

  let map = null, pickupMarker = null, dropMarker = null, partnerMarker = null, routeLine = null;
  let stompClient = null, pollTimer = null;
  let activeLookupCode = null;
  let lastLocationUpdate = null;

  function cleanupLiveUpdates() {
    if (pollTimer) {
      clearInterval(pollTimer);
      pollTimer = null;
    }
    if (stompClient) {
      try { stompClient.disconnect(); } catch (e) {}
      stompClient = null;
    }
  }

  function cleanupMap() {
    if (map) {
      try { map.remove(); } catch (e) {}
      map = null;
    }
    pickupMarker = dropMarker = partnerMarker = routeLine = null;
  }

  function cleanupAll() {
    cleanupLiveUpdates();
    cleanupMap();
  }

  window.addEventListener("beforeunload", cleanupAll);

  if (initialCode && codeInput) {
    const trimmed = initialCode.trim().toUpperCase();
    codeInput.value = trimmed;
  }

  if (searchBtn && codeInput) {
    searchBtn.addEventListener("click", () => {
      const raw = codeInput.value;
      const code = (raw || "").trim();

      if (!code) {
        cleanupAll();
        result.innerHTML = `
          <div class="card">
            <div class="alert alert-error">Please enter a tracking ID.</div>
          </div>`;
        return;
      }

      if (!CODE_REGEX.test(code)) {
        cleanupAll();
        result.innerHTML = `
          <div class="card">
            <div class="alert alert-error">Invalid tracking ID format. Expected format: CR-XXXXXX (e.g. CR-9A4B2C).</div>
          </div>`;
        return;
      }

      const formatted = code.toUpperCase();
      codeInput.value = formatted;
      const url = new URL(window.location.href);
      url.searchParams.set("code", formatted);
      url.searchParams.delete("trackingCode");
      window.history.replaceState({}, "", url);
      load(formatted);
    });

    codeInput.addEventListener("keydown", (e) => {
      if (e.key === "Enter") {
        e.preventDefault();
        searchBtn.click();
      }
    });
  }

  async function load(code) {
    cleanupAll();
    activeLookupCode = code;
    lastLocationUpdate = null;

    if (searchBtn) searchBtn.disabled = true;
    result.innerHTML = `
      <div class="card center-page" style="min-height:200px;">
        <span class="spinner dark"></span>
        <div style="margin-top:12px;color:var(--text-muted);font-weight:600;">Looking up parcel ${escapeHtml(code)}...</div>
      </div>`;

    try {
      const data = await api("/api/courier/track/" + encodeURIComponent(code));
      if (activeLookupCode !== code) return;

      render(data, false);

      // Only start live polling and WebSocket subscriptions if parcel is still in active flight
      if (!TERMINAL.includes(data.status)) {
        pollTimer = setInterval(async () => {
          if (activeLookupCode !== code) return;
          try {
            const fresh = await api("/api/courier/track/" + encodeURIComponent(code));
            if (activeLookupCode === code) {
              render(fresh, true);
              if (TERMINAL.includes(fresh.status)) {
                cleanupLiveUpdates();
              }
            }
          } catch (e) {
            if (e && e.message && (e.message.includes("Session expired") || e.message.includes("401"))) {
              cleanupLiveUpdates();
            }
          }
        }, 12000);
      }
    } catch (err) {
      if (activeLookupCode !== code) return;
      cleanupAll();
      renderError(err, code);
    } finally {
      if (searchBtn) searchBtn.disabled = false;
    }
  }

  function renderError(err, code) {
    const msg = (err && err.message) ? err.message : "";
    const isAuth = msg.includes("Session expired") || msg.includes("Authentication required") || msg.includes("401");
    const isNotFound = msg.includes("404") || msg.toLowerCase().includes("no parcel found");

    let title = "Lookup Failed";
    let body = msg || "An unexpected error occurred while retrieving tracking information.";
    let action = "";

    if (isAuth) {
      title = "Session Expired";
      body = "Your session has expired or you are not logged in. Please sign in to view tracking details.";
      const currentUrl = window.location.pathname + window.location.search;
      action = `<a href="${appPath('/login?redirect=' + encodeURIComponent(currentUrl))}" class="btn btn-brand" style="display:inline-flex;width:auto;margin-top:14px;">Sign in now</a>`;
    } else if (isNotFound) {
      title = "Parcel Not Found";
      body = `No parcel found with tracking code <b>${escapeHtml(code)}</b>. Please verify the code and try again.`;
      action = `<button class="btn btn-outline" style="display:inline-flex;width:auto;margin-top:14px;" onclick="document.getElementById('codeInput').focus();document.getElementById('codeInput').select();">Check tracking code</button>`;
    } else {
      action = `<button class="btn btn-outline" id="retryLookupBtn" style="display:inline-flex;width:auto;margin-top:14px;">Try again</button>`;
    }

    result.innerHTML = `
      <div class="card">
        <h2 style="color:var(--danger);">${escapeHtml(title)}</h2>
        <div class="alert alert-error" style="margin-top:8px;">${body}</div>
        <div>${action}</div>
      </div>
    `;

    const retryBtn = document.getElementById("retryLookupBtn");
    if (retryBtn) {
      retryBtn.addEventListener("click", () => load(code));
    }
  }

  function render(data, isRefresh) {
    const curIdx = STEPS.indexOf(data.status);
    const isTerminalBad = ["CANCELLED", "FAILED", "RETURNED"].includes(data.status);
    const isTerminal = TERMINAL.includes(data.status);

    const hasLocation = Boolean(data.partnerLat && data.partnerLng);
    if (hasLocation && !lastLocationUpdate) {
      lastLocationUpdate = new Date();
    }

    if (!isRefresh) {
      result.innerHTML = `
        <div class="card">
          <div class="hero-row">
            <div>
              <div style="font-size:12px;color:var(--text-muted);font-weight:700;">TRACKING CODE</div>
              <div style="font-size:20px;font-weight:800;letter-spacing:0.02em;">${escapeHtml(data.trackingCode)}</div>
            </div>
            <div class="eta-box">
              <div class="eta-lbl">${data.status === "DELIVERED" ? "Delivered" : "Estimated"}</div>
              <div class="eta-val">${fmtDate(data.expectedDelivery)}</div>
            </div>
          </div>

          <div style="margin-top:10px;">
            <span class="${pillClass(data.status)}" id="statusPill">${statusLabel(data.status)}</span>
          </div>
          <div id="statusMsg" style="margin-top:8px;font-size:13.5px;color:var(--text-muted);">${escapeHtml(data.statusMessage || "")}</div>

          ${isTerminalBad ? "" : `<div class="stepper" id="stepper" style="margin-top:18px;"></div>`}

          <div id="partnerChipWrap"></div>

          <div id="map" class="map-box tall"></div>

          <div class="grid-2" style="margin-top:16px;">
            <div class="job-detail-row"><div class="ic">📍</div><div class="addr">${escapeHtml(data.pickupAddress)}</div></div>
            <div class="job-detail-row"><div class="ic">🏁</div><div class="addr">${escapeHtml(data.dropAddress)}</div></div>
          </div>
          <div class="job-detail-row"><div class="ic">🙋</div><div class="addr">For ${escapeHtml(data.receiverFirstName)} · ${fmtMoney(data.price)} · ${data.serviceTier === "INSTANT" ? "⚡ Instant" : "🚚 Islandwide"}</div></div>
          
          <div id="locationNoticeWrap" style="margin-top:10px;"></div>
        </div>

        <div class="card">
          <h2>Timeline</h2>
          <ul class="tl" id="timeline"></ul>
        </div>
      `;

      buildMap(data);
      if (data.parcelId && !isTerminal) {
        subscribeLive(data.parcelId);
      }
    }

    const pill = document.getElementById("statusPill");
    if (pill) { pill.className = pillClass(data.status); pill.textContent = statusLabel(data.status); }
    const msg = document.getElementById("statusMsg");
    if (msg) msg.textContent = data.statusMessage || "";

    const stepperEl = document.getElementById("stepper");
    if (stepperEl && !isTerminalBad) {
      stepperEl.innerHTML = STEPS.map((s, i) => `
        <div class="step ${i < curIdx ? "done" : ""} ${i === curIdx ? "current" : ""}">
          <div class="bar"></div><div class="lbl">${statusLabel(s)}</div>
        </div>`).join("");
    }

    const chipWrap = document.getElementById("partnerChipWrap");
    if (chipWrap) {
      if (data.partnerName) {
        const isLive = ["PICKED_UP", "IN_TRANSIT", "ARRIVED"].includes(data.status);
        const locLabel = hasLocation
          ? (isLive ? `<span class="live-badge"><span class="blip"></span>LIVE</span> · Last known location` : `· Last known location`)
          : `· <span style="opacity:0.8;">Location not available yet</span>`;
        chipWrap.innerHTML = `
          <div class="partner-chip">
            🛵 ${escapeHtml(data.partnerName)} is on this delivery ${locLabel}
          </div>
        `;
      } else {
        chipWrap.innerHTML = "";
      }
    }

    const locNotice = document.getElementById("locationNoticeWrap");
    if (locNotice) {
      if (!hasLocation && !isTerminal) {
        locNotice.innerHTML = `
          <div style="font-size:12.5px;color:var(--text-muted);display:flex;align-items:center;gap:6px;padding:6px 0;">
            <span>📡</span> Location not available yet
          </div>
        `;
      } else if (hasLocation && lastLocationUpdate) {
        locNotice.innerHTML = `
          <div style="font-size:12px;color:var(--text-muted);display:flex;align-items:center;gap:6px;padding:4px 0;">
            <span>⏱️</span> Last location update: ${fmtDate(lastLocationUpdate.toISOString())}
          </div>
        `;
      } else {
        locNotice.innerHTML = "";
      }
    }

    const tl = document.getElementById("timeline");
    if (tl) {
      tl.innerHTML = (data.timeline || []).slice().reverse().map(h => `
        <li><b>${statusLabel(h.status)}</b>${h.note ? " — " + escapeHtml(h.note) : ""}
          <div class="meta">${fmtDate(h.changedAt)} · ${escapeHtml(h.changedByName)}</div>
        </li>`).join("") || `<div class="empty">No updates yet.</div>`;
    }

    if (map && hasLocation) {
      updatePartnerMarker(data.partnerLat, data.partnerLng, data.partnerName);
    }
  }

  function buildMap(data) {
    const el = document.getElementById("map");
    if (!el) return;
    if (map) {
      try { map.remove(); } catch (e) {}
      map = null;
      pickupMarker = dropMarker = partnerMarker = routeLine = null;
    }

    const pLat = Number(data.pickupLat) || 6.9271;
    const pLng = Number(data.pickupLng) || 79.8612;
    const dLat = Number(data.dropLat) || 6.9344;
    const dLng = Number(data.dropLng) || 79.8443;

    map = L.map("map").setView([pLat, pLng], 12);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "© OpenStreetMap contributors",
      maxZoom: 19
    }).addTo(map);

    pickupMarker = L.marker([pLat, pLng], {
      icon: L.divIcon({ className: "leaflet-div-icon-marker", html: "📍", iconSize: [26, 26] })
    }).addTo(map).bindPopup("Pickup: " + escapeHtml(data.pickupAddress || ""));

    dropMarker = L.marker([dLat, dLng], {
      icon: L.divIcon({ className: "leaflet-div-icon-marker", html: "🏁", iconSize: [26, 26] })
    }).addTo(map).bindPopup("Drop-off: " + escapeHtml(data.dropAddress || ""));

    routeLine = L.polyline([[pLat, pLng], [dLat, dLng]], {
      color: "#4F46E5", weight: 4, dashArray: "8 8"
    }).addTo(map);
    map.fitBounds(routeLine.getBounds(), { padding: [50, 50] });

    if (data.partnerLat && data.partnerLng) {
      updatePartnerMarker(data.partnerLat, data.partnerLng, data.partnerName);
    }
  }

  function updatePartnerMarker(lat, lng, name) {
    if (!map) return;
    const numLat = Number(lat);
    const numLng = Number(lng);
    if (isNaN(numLat) || isNaN(numLng)) return;

    lastLocationUpdate = new Date();
    const locNotice = document.getElementById("locationNoticeWrap");
    if (locNotice) {
      locNotice.innerHTML = `
        <div style="font-size:12px;color:var(--text-muted);display:flex;align-items:center;gap:6px;padding:4px 0;">
          <span>⏱️</span> Last location update: ${fmtDate(lastLocationUpdate.toISOString())}
        </div>
      `;
    }

    const icon = L.divIcon({ className: "leaflet-div-icon-marker", html: "🛵", iconSize: [28, 28] });
    if (!partnerMarker) {
      partnerMarker = L.marker([numLat, numLng], { icon }).addTo(map).bindPopup("Partner: " + escapeHtml(name || ""));
    } else {
      partnerMarker.setLatLng([numLat, numLng]);
      partnerMarker.setPopupContent("Partner: " + escapeHtml(name || ""));
    }
  }

  function subscribeLive(parcelId) {
    if (stompClient) { try { stompClient.disconnect(); } catch (e) {} }
    const socket = new SockJS(appPath("/ws"));
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, () => {
      stompClient.subscribe("/topic/parcel/" + parcelId + "/location", (message) => {
        try {
          const data = JSON.parse(message.body);
          if (data && data.lat && data.lng) {
            updatePartnerMarker(data.lat, data.lng, data.partnerName);
          }
        } catch (e) {}
      });
    }, () => {
      // WS unavailable: regular polling timer keeps data refreshed
    });
  }

  if (initialCode) {
    load(initialCode.trim().toUpperCase());
  } else {
    result.innerHTML = `<div class="card"><div class="empty"><span class="emoji">📦</span>Enter a tracking code above to see live status.</div></div>`;
  }
})();
