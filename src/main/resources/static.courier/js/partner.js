const auth = Auth.requireRole("PARTNER");
if (!auth) {
  /* redirected */
} else {

  document.getElementById("whoName").textContent = auth.name || "Partner";
  document.getElementById("logoutBtn").addEventListener("click", () => Auth.logout());

  const tabOrder = ["deliveries", "requests", "earnings"];
  const glider = document.getElementById("tabGlider");

  function switchTab(targetName) {
    const index = tabOrder.indexOf(targetName);
    if (index === -1) return;

    document.querySelectorAll(".tab").forEach(t =>
      t.classList.toggle("active", t.dataset.tab === targetName));
    glider.style.transform = `translateX(${index * 100}%)`;

    tabOrder.forEach(n => {
      const panel = document.getElementById("tab-" + n);
      if (!panel) return;
      if (n === targetName) {
        panel.style.display = "block";
        panel.classList.remove("tab-panel-enter");
        void panel.offsetWidth;
        panel.classList.add("tab-panel-enter");
      } else {
        panel.style.display = "none";
      }
    });

    if (targetName === "deliveries") loadDeliveries();
    if (targetName === "requests")   loadRequests();
    if (targetName === "earnings")   loadEarnings();
  }
  window.switchTab = switchTab;

  document.getElementById("tabsBar").addEventListener("click", (e) => {
    const tab = e.target.closest(".tab");
    if (!tab) return;
    switchTab(tab.dataset.tab);
  });

  let partner = null;
  let activeDelivery = null;
  let watchId = null;
  let lastLocPush = 0;
  let map = null, meMarker = null, pickupMarker = null, dropMarker = null, routeLine = null;

  /* ---------------- Boot ---------------- */
  async function boot() {
    try {
      partner = await api("/api/courier/partner/me");
      document.getElementById("statusDot").classList.toggle("on", !!partner.isOnline);
      switchTab("deliveries");
      startRefreshLoop();
      if (partner.isOnline) startLocationWatch();
    } catch (e) {
      renderVehiclePicker();
    }
  }

  /* ---------------- Vehicle picker (first-time setup) ---------------- */
  function renderVehiclePicker() {
    const root = document.getElementById("root");
    root.innerHTML = `
      <div class="card" style="max-width:420px;margin:40px auto;">
        <h2>One last step</h2>
        <p class="sub">Tell us what you're delivering with.</p>
        <div class="vehicle-grid">
          <div class="vopt active" data-v="BIKE"><span class="icon">🏍️</span>Bike</div>
          <div class="vopt" data-v="CAR"><span class="icon">🚗</span>Car</div>
          <div class="vopt" data-v="VAN"><span class="icon">🚐</span>Van</div>
          <div class="vopt" data-v="TRUCK"><span class="icon">🚚</span>Truck</div>
        </div>
        <div id="vErr" class="alert alert-error" style="display:none;"></div>
        <button class="btn btn-primary" style="margin-top:18px;" id="vSubmit">Start delivering</button>
      </div>
    `;
    let vehicle = "BIKE";
    root.querySelectorAll(".vopt").forEach(el => {
      el.addEventListener("click", () => {
        root.querySelectorAll(".vopt").forEach(x => x.classList.remove("active"));
        el.classList.add("active");
        vehicle = el.dataset.v;
      });
    });
    document.getElementById("vSubmit").addEventListener("click", async () => {
      try {
        partner = await api("/api/courier/partner/register", {
          method: "POST", body: JSON.stringify({ vehicleType: vehicle })
        });
        toast("You're all set!", "ok");
        location.reload();
      } catch (err) {
        const box = document.getElementById("vErr");
        box.textContent = err.message;
        box.style.display = "block";
      }
    });
  }

  /* ---------------- Deliveries tab ---------------- */
  async function loadDeliveries() {
    const panel = document.getElementById("tab-deliveries");
    if (!panel) return;

    let onlineCard = `
      <div class="card">
        <div class="switch-row">
          <div>
            <h2 style="margin-bottom:2px;">${partner.isOnline ? "You're online" : "You're offline"}</h2>
            <div style="font-size:0.82rem;color:var(--text-muted);">
              ${partner.isOnline
                ? "Sharing your location and seeing nearby requests."
                : "Go online to start receiving delivery requests."}
            </div>
          </div>
          <div class="switch ${partner.isOnline ? "on" : ""}" id="onlineSwitch"><div class="knob"></div></div>
        </div>
        <div id="locWarn" class="alert alert-info" style="display:none;margin-top:14px;">
          Waiting for your device location…
        </div>
      </div>`;

    try { activeDelivery = await api("/api/courier/partner/active"); }
    catch (e) { activeDelivery = null; }

    let jobSection = "";
    if (activeDelivery) {
      jobSection = activeJobHtml(activeDelivery);
    } else {
      jobSection = `
        <div class="card">
          <h2>Current delivery</h2>
          <div class="empty">
            <span class="emoji">📦</span>
            No active delivery right now.
            <br>
            <button class="btn btn-outline btn-sm" style="margin-top:12px;"
              onclick="switchTab('requests')">See nearby requests</button>
          </div>
        </div>`;
    }

    panel.innerHTML = onlineCard + jobSection;

    document.getElementById("onlineSwitch").addEventListener("click", toggleOnline);
    bindActiveJobButtons();
    if (activeDelivery) {
      setTimeout(() => buildJobMap(activeDelivery), 0);
    }
  }

  async function toggleOnline() {
    const next = !partner.isOnline;
    try {
      partner = await api("/api/courier/partner/online", {
        method: "POST", body: JSON.stringify({ isOnline: next })
      });
      document.getElementById("statusDot").classList.toggle("on", !!partner.isOnline);
      if (next) startLocationWatch(); else stopLocationWatch();
      loadDeliveries();
    } catch (err) {
      toast(err.message, "error");
    }
  }

  function activeJobHtml(d) {
    const steps = ["MATCHED", "PICKED_UP", "IN_TRANSIT", "ARRIVED", "DELIVERED"];
    const curIdx = steps.indexOf(d.status);

    let actionHtml = "";
    if (d.status === "MATCHED") {
      actionHtml = `<button class="btn btn-primary" id="actionBtn" data-action="pickup">Mark picked up</button>`;
    } else if (d.status === "PICKED_UP") {
      actionHtml = `<button class="btn btn-primary" id="actionBtn" data-action="in-transit">Start transit to receiver</button>`;
    } else if (d.status === "IN_TRANSIT") {
      actionHtml = `<button class="btn btn-primary" id="actionBtn" data-action="arrived">Mark arrived at drop-off</button>`;
    } else if (d.status === "ARRIVED") {
      actionHtml = `
        <label>Ask the receiver for their 6-digit OTP</label>
        <div class="otp-row">
          <input id="otpInput" maxlength="6" placeholder="••••••" />
          <button class="btn btn-primary" id="otpBtn" style="width:auto;flex:0 0 160px;">Verify &amp; deliver</button>
        </div>
      `;
    }

    return `
      <div class="card">
        <div class="switch-row">
          <h2 style="margin:0;">Current delivery <span class="sub">${d.trackingCode}</span></h2>
          <span class="${pillClass(d.status)}">${statusLabel(d.status)}</span>
        </div>

        <div class="stepper">
          ${steps.map((s, i) => `
            <div class="step ${i < curIdx ? "done" : ""} ${i === curIdx ? "current" : ""}">
              <div class="bar"></div><div class="lbl">${statusLabel(s)}</div>
            </div>`).join("")}
        </div>

        <div id="jobMap" class="map-box" style="margin-top:16px;"></div>

        <div style="margin-top:14px;">
          <div class="job-detail-row"><div class="ic">📍</div><div class="addr">${escapeHtml(d.pickupAddress)}</div></div>
          <div class="job-detail-row"><div class="ic">🏁</div><div class="addr">${escapeHtml(d.dropAddress)}</div></div>
          <div class="job-detail-row"><div class="ic">🙋</div><div class="addr">${escapeHtml(d.receiverName)} · ${escapeHtml(d.receiverPhone || "no phone on file")}</div></div>
          <div class="job-detail-row"><div class="ic">💰</div><div class="addr">You'll earn <b>${fmtMoney(d.price)}</b> for this trip</div></div>
        </div>

        <div id="jobErr" class="alert alert-error" style="display:none;"></div>
        <div style="margin-top:14px;">${actionHtml}</div>
      </div>
    `;
  }

  function bindActiveJobButtons() {
    const btn = document.getElementById("actionBtn");
    if (btn) btn.addEventListener("click", () => runAction(btn.dataset.action));
    const otpBtn = document.getElementById("otpBtn");
    if (otpBtn) otpBtn.addEventListener("click", verifyOtp);
  }

  async function runAction(action) {
    const btn = document.getElementById("actionBtn");
    const errBox = document.getElementById("jobErr");
    errBox.style.display = "none";
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Updating…';
    try {
      await api(`/api/courier/partner/parcel/${activeDelivery.parcelId}/${action}`, { method: "POST" });
      toast("Status updated", "ok");
      loadDeliveries();
    } catch (err) {
      errBox.textContent = err.message;
      errBox.style.display = "block";
      btn.disabled = false;
      btn.textContent = "Retry";
    }
  }

  async function verifyOtp() {
    const otp = document.getElementById("otpInput").value.trim();
    const errBox = document.getElementById("jobErr");
    errBox.style.display = "none";
    if (otp.length !== 6) {
      errBox.textContent = "Enter the 6-digit OTP";
      errBox.style.display = "block";
      return;
    }
    const otpBtn = document.getElementById("otpBtn");
    otpBtn.disabled = true;
    otpBtn.innerHTML = '<span class="spinner"></span>';
    try {
      await api(`/api/courier/partner/parcel/${activeDelivery.parcelId}/verify-otp`, {
        method: "POST", body: JSON.stringify({ otp })
      });
      toast("Delivered! 🎉", "ok");
      loadDeliveries();
    } catch (err) {
      errBox.textContent = err.message;
      errBox.style.display = "block";
      otpBtn.disabled = false;
      otpBtn.textContent = "Verify & deliver";
    }
  }

  function buildJobMap(d) {
    const el = document.getElementById("jobMap");
    if (!el) return;
    if (map) { try { map.remove(); } catch (e) {} map = null; meMarker = pickupMarker = dropMarker = routeLine = null; }
    map = L.map("jobMap").setView([d.pickupLat, d.pickupLng], 12);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "© OpenStreetMap contributors", maxZoom: 19
    }).addTo(map);

    pickupMarker = L.marker([d.pickupLat, d.pickupLng], {
      icon: L.divIcon({ className: "leaflet-div-icon-marker", html: "📍", iconSize: [26, 26] })
    }).addTo(map).bindPopup("Pickup");

    dropMarker = L.marker([d.dropLat, d.dropLng], {
      icon: L.divIcon({ className: "leaflet-div-icon-marker", html: "🏁", iconSize: [26, 26] })
    }).addTo(map).bindPopup("Drop-off");

    routeLine = L.polyline(
      [[d.pickupLat, d.pickupLng], [d.dropLat, d.dropLng]],
      { color: "#4F46E5", weight: 4, dashArray: "8 8" }
    ).addTo(map);
    map.fitBounds(routeLine.getBounds(), { padding: [40, 40] });

    if (partner.isOnline) startLocationWatch();
  }

  /* ---------------- Requests tab ---------------- */
  async function loadRequests() {
    const panel = document.getElementById("tab-requests");
    if (!panel) return;

    panel.innerHTML = `
      <div class="card">
        <h2>Nearby requests</h2>
        <span class="sub">${partner.isOnline ? "Live orders near your location." : "Go online to see requests."}</span>
        <div id="reqList"><div class="skeleton" style="height:70px;"></div></div>
      </div>`;

    const box = document.getElementById("reqList");
    if (!partner.isOnline) {
      box.innerHTML = `<div class="empty"><span class="emoji">💤</span>Go online to see nearby delivery requests.</div>`;
      return;
    }
    try {
      const list = (await api("/api/courier/partner/requests")) || [];
      if (!list.length) {
        box.innerHTML = `<div class="empty"><span class="emoji">🔍</span>No requests near you right now.</div>`;
        return;
      }
      box.innerHTML = list.map(r => `
        <div class="req-card">
          <div class="top">
            <span class="code">${r.trackingCode}</span>
            <span class="dist">${r.distanceKm} km away</span>
          </div>
          <div class="job-detail-row" style="padding:2px 0;">
            <div class="ic">📍</div><div class="addr">${escapeHtml(r.pickupAddress)}</div>
          </div>
          <div class="job-detail-row" style="padding:2px 0;">
            <div class="ic">🏁</div><div class="addr">${escapeHtml(r.dropAddress)}</div>
          </div>
          <div style="display:flex;justify-content:space-between;margin-top:8px;font-size:0.78rem;color:var(--text-muted);">
            <span>${r.serviceTier === "INSTANT" ? "⚡ Instant" : "🚚 Islandwide"} · ${(r.size||"").toLowerCase()} · ${r.weightKg}kg</span>
            <span style="font-weight:800;color:var(--text-dark);">${fmtMoney(r.price)}</span>
          </div>
          <button class="btn btn-primary btn-sm" style="margin-top:10px;width:100%;" onclick="acceptRequest(${r.parcelId}, this)">Accept</button>
        </div>
      `).join("");
    } catch (err) {
      box.innerHTML = `<div class="alert alert-error">${err.message}</div>`;
    }
  }

  async function acceptRequest(parcelId, btn) {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Accepting…';
    try {
      await api(`/api/courier/partner/accept/${parcelId}`, { method: "POST" });
      toast("Job accepted — head to pickup!", "ok");
      switchTab("deliveries");
    } catch (err) {
      toast(err.message, "error");
      loadRequests();
    }
  }
  window.acceptRequest = acceptRequest;

  /* ---------------- Earnings tab ---------------- */
  async function loadEarnings() {
    const panel = document.getElementById("tab-earnings");
    if (!panel) return;

    panel.innerHTML = `<div class="card"><span class="spinner dark"></span> Loading earnings…</div>`;

    try {
      const e = await api("/api/courier/partner/earnings");

      const monthly = (e.monthly || []).map(m => `
        <div class="parcel-card" style="margin-bottom:8px;">
          <div class="row1">
            <span class="code">${escapeHtml(m.label)}</span>
            <span class="price">${fmtMoney(m.amount)}</span>
          </div>
          <div style="font-size:0.78rem;color:var(--text-muted);">${m.deliveries} deliveries</div>
        </div>`).join("") || `<div class="empty"><span class="emoji">📊</span>No monthly data yet.</div>`;

      const recent = (e.recent || []).map(r => `
        <div class="parcel-card" style="margin-bottom:8px;">
          <div class="row1">
            <span class="code">${r.trackingCode}</span>
            <span class="price">${fmtMoney(r.amount)}</span>
          </div>
          <div class="route">
            <span class="addr drop">${escapeHtml(r.dropAddress || "—")}</span>
          </div>
          <div class="foot">
            <span>${fmtDate(r.deliveredAt)}</span>
          </div>
        </div>`).join("") || `<div class="empty"><span class="emoji">🗂️</span>No completed deliveries yet.</div>`;

      panel.innerHTML = `
        <div class="card">
          <h2>Earnings</h2>
          <span class="sub">Your delivery income at a glance</span>

          <div class="grid-2">
            <div class="price-box" style="flex-direction:column;align-items:flex-start;">
              <div class="lbl">Total earned</div>
              <div class="amt">${fmtMoney(e.totalEarned)}</div>
              <div class="field-hint">${e.totalDeliveries} deliveries</div>
            </div>
            <div class="price-box" style="flex-direction:column;align-items:flex-start;">
              <div class="lbl">This month</div>
              <div class="amt">${fmtMoney(e.thisMonthEarned)}</div>
              <div class="field-hint">${e.thisMonthDeliveries} deliveries</div>
            </div>
          </div>
        </div>

        <div class="card">
          <h2>Monthly breakdown</h2>
          <div>${monthly}</div>
        </div>

        <div class="card">
          <h2>Recent deliveries</h2>
          <div>${recent}</div>
        </div>
      `;
    } catch (err) {
      panel.innerHTML = `<div class="card"><div class="alert alert-error">${err.message}</div></div>`;
    }
  }

  /* ---------------- Location watch ---------------- */
  function startLocationWatch() {
    if (!navigator.geolocation) return;
    if (watchId !== null) return;
    const w0 = document.getElementById("locWarn");
    if (w0) w0.style.display = "block";

    watchId = navigator.geolocation.watchPosition(async (pos) => {
      const lat = pos.coords.latitude, lng = pos.coords.longitude;
      const now = Date.now();
      if (now - lastLocPush < 4000) return;
      lastLocPush = now;

      const w = document.getElementById("locWarn");
      if (w) w.style.display = "none";

      try {
        await api("/api/courier/partner/location", {
          method: "POST", body: JSON.stringify({ lat, lng })
        });
        if (activeDelivery) {
          await api(`/api/courier/partner/parcel/${activeDelivery.parcelId}/location`, {
            method: "POST", body: JSON.stringify({ lat, lng })
          });
        }
      } catch (e) {}
    }, () => {
      const w = document.getElementById("locWarn");
      if (w) {
        w.textContent = "Location permission denied — enable it to receive requests.";
        w.className = "alert alert-error";
        w.style.display = "block";
      }
    }, { enableHighAccuracy: true, maximumAge: 3000, timeout: 10000 });
  }
  function stopLocationWatch() {
    if (watchId !== null) { navigator.geolocation.clearWatch(watchId); watchId = null; }
  }

  /* ---------------- Refresh loop ---------------- */
  function startRefreshLoop() {
    setInterval(() => {
      const activeTab = document.querySelector(".tab.active")?.dataset.tab;
      if (activeTab === "deliveries") loadDeliveries();
      if (activeTab === "requests")   loadRequests();
      // earnings don't change often — no auto-refresh
    }, 7000);
  }

  boot();
}