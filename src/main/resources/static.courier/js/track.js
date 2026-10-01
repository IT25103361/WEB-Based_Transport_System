(function () {
  const params = new URLSearchParams(window.location.search);
  const initialCode = params.get("code");
  if (initialCode) document.getElementById("codeInput").value = initialCode;

  document.getElementById("searchBtn").addEventListener("click", () => {
    const code = document.getElementById("codeInput").value.trim();
    if (code) {
      const url = new URL(window.location.href);
      url.searchParams.set("code", code);
      window.history.replaceState({}, "", url);
      load(code);
    }
  });
  document.getElementById("codeInput").addEventListener("keydown", (e) => {
    if (e.key === "Enter") document.getElementById("searchBtn").click();
  });

  let map = null, pickupMarker = null, dropMarker = null, partnerMarker = null, routeLine = null;
  let stompClient = null, pollTimer = null;
  const STEPS = ["REQUESTED", "MATCHED", "PICKED_UP", "IN_TRANSIT", "ARRIVED", "DELIVERED"];

  async function load(code) {
    const result = document.getElementById("result");
    result.innerHTML = `<div class="card center-page" style="min-height:200px;"><span class="spinner dark"></span></div>`;

    try {
      const data = await api("/api/courier/track/" + encodeURIComponent(code));
      render(data);
      if (pollTimer) clearInterval(pollTimer);
      pollTimer = setInterval(async () => {
        try { render(await api("/api/courier/track/" + encodeURIComponent(code)), true); } catch (e) {}
      }, 12000);
    } catch (err) {
      result.innerHTML = `<div class="card"><div class="alert alert-error">${err.message}</div></div>`;
    }
  }

  function render(data, isRefresh) {
    const result = document.getElementById("result");
    const terminal = ["CANCELLED", "FAILED", "RETURNED"];
    const curIdx = STEPS.indexOf(data.status);
    const isTerminalBad = terminal.includes(data.status);

    if (!isRefresh) {
      result.innerHTML = `
        <div class="card">
          <div class="hero-row">
            <div>
              <div style="font-size:12px;color:var(--text-muted);font-weight:700;">TRACKING CODE</div>
              <div style="font-size:20px;font-weight:800;letter-spacing:0.02em;">${data.trackingCode}</div>
            </div>
            <div class="eta-box">
              <div class="eta-lbl">${data.status === "DELIVERED" ? "Delivered" : "Estimated"}</div>
              <div class="eta-val">${fmtDate(data.expectedDelivery)}</div>
            </div>
          </div>

          <div style="margin-top:10px;">
            <span class="${pillClass(data.status)}" id="statusPill">${statusLabel(data.status)}</span>
          </div>
          <div id="statusMsg" style="margin-top:8px;font-size:13.5px;color:var(--text-muted);">${data.statusMessage || ""}</div>

          ${isTerminalBad ? "" : `<div class="stepper" id="stepper" style="margin-top:18px;"></div>`}

          <div id="partnerChipWrap"></div>

          <div id="map" class="map-box tall"></div>

          <div class="grid-2" style="margin-top:16px;">
            <div class="job-detail-row"><div class="ic">📍</div><div class="addr">${escapeHtml(data.pickupAddress)}</div></div>
            <div class="job-detail-row"><div class="ic">🏁</div><div class="addr">${escapeHtml(data.dropAddress)}</div></div>
          </div>
          <div class="job-detail-row"><div class="ic">🙋</div><div class="addr">For ${escapeHtml(data.receiverFirstName)} · ${fmtMoney(data.price)} · ${data.serviceTier === "INSTANT" ? "⚡ Instant" : "🚚 Islandwide"}</div></div>
        </div>

        <div class="card">
          <h2>Timeline</h2>
          <ul class="tl" id="timeline"></ul>
        </div>
      `;
      buildMap(data);
      if (data.parcelId) subscribeLive(data.parcelId);
    }

    const pill = document.getElementById("statusPill");
    if (pill) { pill.className = pillClass(data.status); pill.textContent = statusLabel(data.status); }
    const msg = document.getElementById("statusMsg");
    if (msg) msg.textContent = data.statusMessage || "";

    const stepperEl = document.getElementById("stepper");
    if (stepperEl) {
      stepperEl.innerHTML = STEPS.map((s, i) => `
        <div class="step ${i < curIdx ? "done" : ""} ${i === curIdx ? "current" : ""}">
          <div class="bar"></div><div class="lbl">${statusLabel(s)}</div>
        </div>`).join("");
    }

    const chipWrap = document.getElementById("partnerChipWrap");
    if (chipWrap) {
      chipWrap.innerHTML = data.partnerName
        ? `<div class="partner-chip">🛵 ${escapeHtml(data.partnerName)} is on this delivery
            ${["PICKED_UP","IN_TRANSIT","ARRIVED"].includes(data.status) ? `<span class="live-badge"><span class="blip"></span>LIVE</span>` : ""}
           </div>`
        : "";
    }

    const tl = document.getElementById("timeline");
    if (tl) {
      tl.innerHTML = (data.timeline || []).slice().reverse().map(h => `
        <li><b>${statusLabel(h.status)}</b>${h.note ? " — " + escapeHtml(h.note) : ""}
          <div class="meta">${fmtDate(h.changedAt)} · ${escapeHtml(h.changedByName)}</div>
        </li>`).join("") || `<div class="empty">No updates yet.</div>`;
    }

    if (map && data.partnerLat && data.partnerLng) {
      updatePartnerMarker(data.partnerLat, data.partnerLng, data.partnerName);
    }
  }

  function buildMap(data) {
    const el = document.getElementById("map");
    if (!el) return;
    if (map) {
      try { map.remove(); } catch (e) { /* ignore */ }
      map = null;
      pickupMarker = dropMarker = partnerMarker = routeLine = null;
    }
    map = L.map("map").setView([data.pickupLat, data.pickupLng], 12);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", { attribution: "© OpenStreetMap contributors", maxZoom: 19 }).addTo(map);

    pickupMarker = L.marker([data.pickupLat, data.pickupLng], {
      icon: L.divIcon({ className: "leaflet-div-icon-marker", html: "📍", iconSize: [26, 26] })
    }).addTo(map).bindPopup("Pickup: " + data.pickupAddress);

    dropMarker = L.marker([data.dropLat, data.dropLng], {
      icon: L.divIcon({ className: "leaflet-div-icon-marker", html: "🏁", iconSize: [26, 26] })
    }).addTo(map).bindPopup("Drop-off: " + data.dropAddress);

    routeLine = L.polyline([[data.pickupLat, data.pickupLng], [data.dropLat, data.dropLng]], {
      color: "#4F46E5", weight: 4, dashArray: "8 8"
    }).addTo(map);
    map.fitBounds(routeLine.getBounds(), { padding: [50, 50] });

    if (data.partnerLat && data.partnerLng) updatePartnerMarker(data.partnerLat, data.partnerLng, data.partnerName);
  }

  function updatePartnerMarker(lat, lng, name) {
    if (!map) return;
    const icon = L.divIcon({ className: "leaflet-div-icon-marker", html: "🛵", iconSize: [28, 28] });
    if (!partnerMarker) partnerMarker = L.marker([lat, lng], { icon }).addTo(map).bindPopup("Partner: " + (name || ""));
    else { partnerMarker.setLatLng([lat, lng]); partnerMarker.setPopupContent("Partner: " + (name || "")); }
  }

  function subscribeLive(parcelId) {
    if (stompClient) { try { stompClient.disconnect(); } catch (e) {} }
    const socket = new SockJS(appPath("/ws"));
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, () => {
      stompClient.subscribe("/topic/parcel/" + parcelId + "/location", (message) => {
        const data = JSON.parse(message.body);
        updatePartnerMarker(data.lat, data.lng, data.partnerName);
      });
    }, () => { /* WS unavailable — polling still keeps things fresh */ });
  }

  if (initialCode) load(initialCode);
  else document.getElementById("result").innerHTML = `<div class="card"><div class="empty"><span class="emoji">📦</span>Enter a tracking code above to see live status.</div></div>`;
})();
