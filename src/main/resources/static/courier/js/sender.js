const auth = Auth.requireRole("SENDER");
if (!auth) {
  /* redirected */
} else {

  document.getElementById("whoName").textContent = auth.name || "Sender";
  document.getElementById("logoutBtn").addEventListener("click", () => Auth.logout());

  const tabOrder = ["ongoing", "new", "history"];
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

    if (targetName === "new") setTimeout(() => map.invalidateSize(), 60);
    if (targetName === "history") renderHistory();
  }

  document.getElementById("tabsBar").addEventListener("click", (e) => {
    const tab = e.target.closest(".tab");
    if (!tab) return;
    switchTab(tab.dataset.tab);
  });
  window.switchTab = switchTab;

  const map = L.map("map").setView([7.2083, 79.8358], 12);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "© OpenStreetMap", maxZoom: 19
  }).addTo(map);

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      p => { if (!pickup) map.setView([p.coords.latitude, p.coords.longitude], 14); },
      () => {}
    );
  }

  let pickupMarker = null, dropMarker = null, routeLine = null;
  let pickup = null, drop = null, activePin = "pickup";

  const icon = (e) => L.divIcon({
    className: "leaflet-div-icon-marker",
    html: e, iconSize: [26, 26], iconAnchor: [13, 24]
  });

  document.getElementById("pinPickup").onclick = () => setActivePin("pickup");
  document.getElementById("pinDrop").onclick   = () => setActivePin("drop");

  function setActivePin(w) {
    activePin = w;
    document.getElementById("pinPickup").classList.toggle("active", w === "pickup");
    document.getElementById("pinDrop").classList.toggle("active", w === "drop");
    document.getElementById("mapHint").textContent = w === "pickup"
      ? "Tap the map to place the pickup pin."
      : "Tap the map to place the drop-off pin.";
  }

  map.on("click", (e) => setPin(activePin, e.latlng));

  function setPin(which, latlng) {
    if (which === "pickup") {
      pickup = { lat: latlng.lat, lng: latlng.lng };
      if (pickupMarker) pickupMarker.setLatLng(latlng);
      else {
        pickupMarker = L.marker(latlng, { icon: icon("📍"), draggable: true }).addTo(map);
        pickupMarker.on("dragend", () => {
          const p = pickupMarker.getLatLng();
          pickup = { lat: p.lat, lng: p.lng };
          refreshPins();
        });
      }
      if (!drop) setActivePin("drop");
    } else {
      drop = { lat: latlng.lat, lng: latlng.lng };
      if (dropMarker) dropMarker.setLatLng(latlng);
      else {
        dropMarker = L.marker(latlng, { icon: icon("🏁"), draggable: true }).addTo(map);
        dropMarker.on("dragend", () => {
          const p = dropMarker.getLatLng();
          drop = { lat: p.lat, lng: p.lng };
          refreshPins();
        });
      }
    }
    refreshPins();
  }

  function refreshPins() {
    if (routeLine) { map.removeLayer(routeLine); routeLine = null; }
    if (pickup && drop) {
      routeLine = L.polyline(
        [[pickup.lat, pickup.lng], [drop.lat, drop.lng]],
        { color: "#4F46E5", weight: 4, dashArray: "8 8" }
      ).addTo(map);
    }
    const ps = document.getElementById("pickupStatus");
    const ds = document.getElementById("dropStatus");
    if (pickup) { ps.className = "ok"; ps.textContent = `📍 Pickup set (${pickup.lat.toFixed(4)}, ${pickup.lng.toFixed(4)})`; }
    if (drop)   { ds.className = "ok"; ds.textContent = `🏁 Drop-off set (${drop.lat.toFixed(4)}, ${drop.lng.toFixed(4)})`; }
    updatePriceEstimate();
  }

  let tier = "INSTANT";
  document.getElementById("tierInstant").onclick = () => setTier("INSTANT");
  document.getElementById("tierIslandwide").onclick = () => setTier("ISLANDWIDE");

  function setTier(t) {
    tier = t;
    document.getElementById("tierInstant").classList.toggle("active", t === "INSTANT");
    document.getElementById("tierIslandwide").classList.toggle("active", t === "ISLANDWIDE");
    document.getElementById("scheduleWrap").style.display = t === "ISLANDWIDE" ? "block" : "none";
    updatePriceEstimate();
  }

  document.getElementById("weightKg").oninput = updatePriceEstimate;

  function updatePriceEstimate() {
    const w = parseFloat(document.getElementById("weightKg").value) || 1;
    const p = estimatePrice(tier, pickup, drop, w);
    document.getElementById("priceEstimate").textContent =
      p === null ? "Set both pins" : fmtMoney(p);
  }
  updatePriceEstimate();

  const form = document.getElementById("parcelForm");
  const formErr = document.getElementById("formErr");
  const formOk = document.getElementById("formOk");
  const submitBtn = document.getElementById("submitBtn");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    formErr.style.display = "none";
    formOk.style.display = "none";

    if (!pickup || !drop) {
      formErr.textContent = "Please place both a pickup and a drop-off pin on the map.";
      formErr.style.display = "block";
      document.getElementById("map").scrollIntoView({ behavior: "smooth", block: "center" });
      return;
    }

    const payload = {
      receiverName: document.getElementById("receiverName").value.trim(),
      receiverEmail: document.getElementById("receiverEmail").value.trim(),
      receiverPhone: document.getElementById("receiverPhone").value.trim(),
      pickupAddress: document.getElementById("pickupAddress").value.trim() || null,
      pickupLat: pickup.lat, pickupLng: pickup.lng,
      dropAddress: document.getElementById("dropAddress").value.trim() || null,
      dropLat: drop.lat, dropLng: drop.lng,
      weightKg: parseFloat(document.getElementById("weightKg").value),
      size: document.getElementById("size").value,
      serviceTier: tier,
      scheduledAt: (tier === "ISLANDWIDE" && document.getElementById("scheduledAt").value) || null
    };

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span> Booking…';

    try {
      const parcel = await api("/api/courier/parcels", {
        method: "POST",
        body: JSON.stringify(payload)
      });
      formOk.innerHTML = `Booked! Tracking code <b>${parcel.trackingCode}</b> — share it with your receiver.`;
      formOk.style.display = "block";
      toast("Delivery booked: " + parcel.trackingCode, "ok");

      form.reset();
      pickup = null; drop = null;
      [pickupMarker, dropMarker, routeLine].forEach(m => m && map.removeLayer(m));
      pickupMarker = dropMarker = routeLine = null;
      const ps = document.getElementById("pickupStatus");
      const ds = document.getElementById("dropStatus");
      ps.className = "missing"; ps.textContent = "📍 Pickup not set";
      ds.className = "missing"; ds.textContent = "🏁 Drop-off not set";
      setTier("INSTANT");
      setActivePin("pickup");
      updatePriceEstimate();
      loadParcels();
    } catch (err) {
      formErr.textContent = err.message || "Could not book delivery";
      formErr.style.display = "block";
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = "Book this delivery";
    }
  });

  let allParcels = [];

  async function loadParcels() {
    try {
      allParcels = (await api("/api/courier/parcels/my")) || [];
      renderOngoing();
      renderHistory();
    } catch (err) {
      const box = document.getElementById("ongoingList");
      if (box) box.innerHTML = `<div class="message error">${err.message}</div>`;
    }
  }

  function renderOngoing() {
    const box = document.getElementById("ongoingList");
    if (!box) return;
    const items = allParcels.filter(p => !TERMINAL_STATUSES.includes(p.status));
    box.innerHTML = items.length
      ? items.map(p => parcelCardHtml(p, true)).join("")
      : `<div class="empty">
           <span class="emoji">📭</span>
           Nothing in flight right now.
           <br>
           <button class="btn btn-outline btn-sm" style="margin-top:12px;"
             onclick="switchTab('new')">Book a delivery</button>
         </div>`;
  }

  function renderHistory() {
    const box = document.getElementById("historyList");
    if (!box) return;
    const items = allParcels.filter(p => TERMINAL_STATUSES.includes(p.status));
    box.innerHTML = items.length
      ? items.map(p => parcelCardHtml(p, false)).join("")
      : `<div class="empty"><span class="emoji">🗂️</span>No completed deliveries yet.</div>`;
  }

  function parcelCardHtml(p, ongoing) {
    const canCancel = SENDER_CANCELLABLE.includes(p.status);

    const pickupText = p.pickupAddress
      ? escapeHtml(p.pickupAddress)
      : `Pinned location (${Number(p.pickupLat).toFixed(5)}, ${Number(p.pickupLng).toFixed(5)})`;
    const dropText = p.dropAddress
      ? escapeHtml(p.dropAddress)
      : `Pinned location (${Number(p.dropLat).toFixed(5)}, ${Number(p.dropLng).toFixed(5)})`;

    const otpBlock = (ongoing && p.status === "ARRIVED" && p.deliveryOtp) ? `
      <div class="otp-block">
        <div class="lbl">Delivery code</div>
        <div class="code-num">${p.deliveryOtp}</div>
        <div class="hint">Share this with the receiver</div>
      </div>` : "";

    const refundBlock = (!ongoing && p.refundEligible) ? `
      <div class="message error" style="margin-top:10px;">
        Cancelled after pickup — the partner reported: <b>${escapeHtml(p.cancelReason || "an issue")}</b>.<br>
        You'll get a full refund; it'll reflect within a few business days.
      </div>` : "";

    const cancelBanner = (!ongoing && !p.refundEligible && p.cancelReason) ? `
      <div class="message error" style="margin-top:10px;">
        Cancelled by ${p.cancelledBy === "PARTNER" ? "the delivery partner" : "you"} — ${escapeHtml(p.cancelReason)}
      </div>` : "";

    return `
      <div class="parcel-card ${ongoing ? "ongoing" : ""}">
        <div class="row1">
          <span class="code">${p.trackingCode}</span>
          <span class="${pillClass(p.status)}">${statusLabel(p.status)}</span>
        </div>
        <div class="route">
          <span class="addr pickup">${pickupText}</span>
          <span class="addr drop">${dropText}</span>
        </div>
        <div class="foot">
          <span>${p.serviceTier === "INSTANT" ? "⚡ Instant" : "🚚 Islandwide"} · ${fmtDate(p.createdAt || p.deliveredAt)}</span>
          <span class="price">${fmtMoney(p.price)}</span>
        </div>
        ${otpBlock}
        ${refundBlock}
        ${cancelBanner}
        <div id="pay-${p.id}"></div>
        <div style="margin-top:12px;display:flex;gap:8px;flex-wrap:wrap;">
          <button class="btn btn-outline btn-sm" onclick="window.location.href=appPath('/track?code=${p.trackingCode}')">Track live</button>
          <button class="btn btn-outline btn-sm" onclick="togglePay(${p.id}, event)">Payment</button>
          ${canCancel ? `<button class="btn btn-danger btn-sm" onclick="cancelParcel(${p.id}, event)">Cancel delivery</button>` : ""}
        </div>
      </div>`;
  }

  async function cancelParcel(id, evt) {
    evt.stopPropagation();
    const answer = await askCancelReason("Why are you cancelling?", SENDER_CANCEL_REASONS);
    if (!answer) return;
    try {
      await api(`/api/courier/parcels/${id}/cancel`, {
        method: "POST",
        body: JSON.stringify(answer)
      });
      toast("Delivery cancelled", "ok");
      loadParcels();
    } catch (err) {
      toast(err.message, "error");
    }
  }

  async function togglePay(id, evt) {
    evt.stopPropagation();
    const box = document.getElementById("pay-" + id);
    if (box.dataset.open === "1") {
      box.innerHTML = "";
      box.dataset.open = "0";
      return;
    }
    box.innerHTML = `<div class="pay-panel"><span class="spinner dark"></span> Loading…</div>`;
    box.dataset.open = "1";
    try {
      const pay = await api(`/api/courier/parcels/${id}/payment`);
      box.innerHTML = `<div class="pay-panel">
        Status: <b>${pay.status}</b> · ${pay.method} · ${fmtMoney(pay.amount)}
        ${pay.status === "PAID" ? " · Paid " + fmtDate(pay.paidAt) : ""}
      </div>`;
    } catch {
      box.innerHTML = `<div class="pay-panel">
        Not paid yet.
        <select id="method-${id}">
          <option value="CARD">Card</option>
          <option value="CASH">Cash</option>
          <option value="WALLET">Wallet</option>
        </select>
        <button class="btn btn-primary btn-sm" style="width:auto;" onclick="payNow(${id}, event)">Pay now</button>
      </div>`;
    }
  }

  async function payNow(id, evt) {
    evt.stopPropagation();
    try {
      const pay = await api(`/api/courier/parcels/${id}/pay`, {
        method: "POST",
        body: JSON.stringify({ method: document.getElementById("method-" + id).value })
      });
      toast("Payment " + pay.status.toLowerCase(), "ok");
      document.getElementById("pay-" + id).innerHTML =
        `<div class="pay-panel">Status: <b>${pay.status}</b> · ${fmtMoney(pay.amount)}</div>`;
    } catch (err) {
      toast(err.message, "error");
    }
  }

  window.cancelParcel = cancelParcel;
  window.togglePay = togglePay;
  window.payNow = payNow;

  loadParcels();
  setInterval(loadParcels, 15000);
}
