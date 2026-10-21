/* =============================================================================
   NEXTGO Dashboard - Clean Read-Only Telemetry Overview
   Matches requested design: Hero Banner, 3 Metric Cards, 2-Column Split
   ============================================================================= */

(async () => {
  pageShell("Dashboard", "Your school transport, all in one place.", skeletons(1));
  const main = document.querySelector(".app-main");

  try {
    const [students, parent, notices] = await Promise.all([
      Api.getStudents().then(apiList).catch(() => []),
      Api.getParent().catch(() => ({})),
      fetchCombinedNotifications()
    ]);

    if (!students.length) {
      main.insertAdjacentHTML("beforeend", '<section class="glass empty-state">No students are linked to this parent account.</section>');
      return;
    }

    setCachedStudents(students);
    const studentExists = students.some(s => String(s.id || s.studentId) === String(selectedStudent()));
    if (!studentExists) setStudent(students[0].id || students[0].studentId);
    const activeStudentId = selectedStudent();

    // Student and Parent Details
    const activeStudent = students.find(s => String(s.id || s.studentId) === String(activeStudentId)) || students[0];
    const childName = personName(activeStudent);
    const parentName = personName(parent) || "John";
    const parentFirstName = parentName.split(" ")[0] || "John";

    let transport = {};
    try {
      transport = await Api.getStudentTransport(activeStudentId);
    } catch {}

    // Resolve Assignment
    const saved = localStorage.getItem(`nextgo_transport_assignment_${activeStudentId}`);
    let assignedRoute = ALL_ROUTES_DATA[0];
    let assignedMorningBus = assignedRoute.buses[0];
    let assignedReturnBus = assignedRoute.buses[1] || assignedRoute.buses[0];
    let pickupStop = assignedRoute.stops[3]?.name || assignedRoute.stops[0].name;
    let dropoffStop = assignedRoute.stops[assignedRoute.stops.length - 1]?.name || assignedRoute.stops[0].name;

    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        assignedRoute = ALL_ROUTES_DATA.find(r => r.id === parsed.routeId) || ALL_ROUTES_DATA[0];
        assignedMorningBus = assignedRoute.buses.find(b => b.id === parsed.morningBusId) || assignedRoute.buses[0];
        assignedReturnBus = assignedRoute.buses.find(b => b.id === parsed.returnBusId) || assignedRoute.buses[1] || assignedRoute.buses[0];
        if (parsed.pickupStop) pickupStop = parsed.pickupStop;
        if (parsed.dropoffStop) dropoffStop = parsed.dropoffStop;
      } catch {}
    } else {
      const school = activeStudent?.schoolName || "";
      let rId = 1;
      if (school.toLowerCase().includes("nalanda")) rId = 4;
      else if (school.toLowerCase().includes("ananda")) rId = 2;
      else if (school.toLowerCase().includes("musaeus")) rId = 3;
      else if (school.toLowerCase().includes("royal")) rId = 5;
      else if (school.toLowerCase().includes("isipathana")) rId = 6;
      assignedRoute = ALL_ROUTES_DATA.find(r => r.id === rId) || ALL_ROUTES_DATA[0];
      assignedMorningBus = assignedRoute.buses[0];
      assignedReturnBus = assignedRoute.buses[1] || assignedRoute.buses[0];
      pickupStop = transport?.pickupStopName || assignedRoute.stops[3]?.name || assignedRoute.stops[0].name;
      dropoffStop = transport?.dropOffStopName || assignedRoute.stops[assignedRoute.stops.length - 1]?.name;
    }

    const schoolName = field(activeStudent, "schoolName", "school", assignedRoute.schoolName);
    const gradeLevel = field(activeStudent, "grade", "gradeLevel", "Grade 5");
    const telem = (typeof NextGoSimulation !== "undefined") ? NextGoSimulation.getCurrentTelemetry() : null;
    const dynamicEta = telem ? `${telem.eta} min` : "8 min";
    const currentStop = telem?.nextStop || assignedRoute.stops[2]?.name || pickupStop;
    const nowTimeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const nowDateStr = new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });

    // --- Monthly Transport Payment Calculation (Rate: 250 LKR / KM) ---
    const RATE_PER_KM = 250;
    function computeStopsDistance(route, pName, dName) {
      if (!route || !route.stops || route.stops.length < 2) return 1.0;
      const pStop = route.stops.find(s => s.name === pName) || route.stops[0];
      const dStop = route.stops.find(s => s.name === dName) || route.stops[route.stops.length - 1];
      if (!pStop || !dStop || !pStop.lat || !pStop.lon || !dStop.lat || !dStop.lon) return 1.0;
      const R = 6371;
      const dLat = (dStop.lat - pStop.lat) * Math.PI / 180;
      const dLon = (dStop.lon - pStop.lon) * Math.PI / 180;
      const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(pStop.lat * Math.PI / 180) * Math.cos(dStop.lat * Math.PI / 180) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return Math.max(1.0, Math.round(R * c * 10) / 10);
    }

    const routeDistanceKm = computeStopsDistance(assignedRoute, pickupStop, dropoffStop);
    const savedDistance = localStorage.getItem(`nextgo_school_distance_${activeStudentId}`);
    // Default to 1 KM (matching '1KM*rate per km = 250LKR') unless explicitly customized
    let appliedDistanceKm = savedDistance ? parseFloat(savedDistance) : 1;
    if (isNaN(appliedDistanceKm) || appliedDistanceKm <= 0) appliedDistanceKm = 1;

    let monthlyPayment = Math.round(appliedDistanceKm * RATE_PER_KM);
    const billingMonth = new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    const paymentStorageKey = `nextgo_school_payment_${activeStudentId}_${new Date().getFullYear()}_${new Date().getMonth()}`;
    let isPaid = localStorage.getItem(paymentStorageKey) === "PAID";

    // Recent Updates List (filtered or fallback)
    const recentNotices = notices.length ? notices.slice(0, 4) : [
      {
        title: "GPS ACTIVE",
        message: `Live telemetry tracking active on ${assignedRoute.name}.`,
        time: "Just now"
      },
      {
        title: "BUS ON SCHEDULE",
        message: `School Bus ${assignedMorningBus.reg} departed origin on time.`,
        time: "10 mins ago"
      },
      {
        title: "ASSIGNMENT CONFIRMED",
        message: `Pickup assigned at ${pickupStop}, destination ${schoolName}.`,
        time: "Today, 06:30 AM"
      }
    ];

    main.innerHTML = `
      <header class="topbar">
        ${renderTopbarLeft("Dashboard", "Your school transport, all in one place.")}
        ${renderTopControls(students, activeStudentId)}
      </header>

      <div class="dash-canvas">
        <!-- 1. HERO BANNER (Soft Sage / Teal Aesthetic) -->
        <section class="dash-hero-card">
          <div class="dash-hero-content">
            <span class="dash-hero-eyebrow">THE JOURNEY MATTERS</span>
            <h2 class="dash-hero-title">Good to see you, ${escapeHtml(parentFirstName)}.</h2>
            <p class="dash-hero-subtitle">Your school transport, all in one place.</p>
            <a class="dash-hero-btn" href="tracking">
              <span>Follow the journey</span>
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
                <line x1="7" y1="17" x2="17" y2="7"></line>
                <polyline points="7 7 17 7 17 17"></polyline>
              </svg>
            </a>
          </div>

          <div class="dash-hero-art">
            <!-- Modern Scalable School Bus Vector Illustration -->
            <svg class="school-bus-vector" viewBox="0 0 340 170" fill="none" xmlns="http://www.w3.org/2000/svg">
              <!-- Ground Shadow -->
              <ellipse cx="170" cy="155" rx="140" ry="12" fill="currentColor" class="bus-ground-shadow" />
              <!-- Bus Body Main -->
              <path d="M 45 60 C 45 42 58 32 75 32 L 275 32 C 295 32 305 45 305 65 L 305 125 C 305 130 300 135 295 135 L 55 135 C 49 135 45 130 45 125 Z" fill="#f7b731" stroke="#e09a1b" stroke-width="3" />
              <!-- Bus Front Bumper & Grill -->
              <path d="M 285 75 L 310 82 C 314 83 316 88 315 92 L 312 120 C 311 125 306 128 301 128 L 285 128 Z" fill="#eb9816" />
              <rect x="305" y="98" width="8" height="18" rx="3" fill="#333f48" />
              <circle cx="308" cy="85" r="5" fill="#ffffff" />
              <!-- Windows Frame Strip -->
              <rect x="58" y="44" width="232" height="34" rx="6" fill="#2d3748" />
              <!-- Individual Windows -->
              <rect x="63" y="47" width="34" height="28" rx="4" fill="#a0aec0" />
              <rect x="103" y="47" width="34" height="28" rx="4" fill="#a0aec0" />
              <rect x="143" y="47" width="34" height="28" rx="4" fill="#a0aec0" />
              <rect x="183" y="47" width="34" height="28" rx="4" fill="#a0aec0" />
              <rect x="223" y="47" width="34" height="28" rx="4" fill="#a0aec0" />
              <path d="M 263 47 L 285 47 C 288 47 290 49 290 52 L 290 75 L 263 75 Z" fill="#cbd5e0" />
              <!-- Black Side Stripe -->
              <rect x="45" y="88" width="250" height="12" fill="#2d3748" />
              <text x="52" y="97" fill="#f7fafc" font-size="8" font-family="system-ui, sans-serif" font-weight="900" letter-spacing="1">NEXTGO</text>
              <text x="180" y="112" fill="#5c3b00" font-size="7" font-family="system-ui, sans-serif" font-weight="800" letter-spacing="0.5">SCHOOL TRANSPORT</text>
              <!-- Rear Wheel Cutout & Wheel -->
              <circle cx="95" cy="135" r="22" fill="#1a202c" />
              <circle cx="95" cy="135" r="10" fill="#a0aec0" />
              <circle cx="95" cy="135" r="4" fill="#1a202c" />
              <!-- Front Wheel Cutout & Wheel -->
              <circle cx="245" cy="135" r="22" fill="#1a202c" />
              <circle cx="245" cy="135" r="10" fill="#a0aec0" />
              <circle cx="245" cy="135" r="4" fill="#1a202c" />
            </svg>
            <div class="dash-hero-caption">Your child's journey, in view.</div>
          </div>
        </section>

        <!-- 2. FOUR SUMMARY METRIC CARDS IN A ROW -->
        <section class="dash-metrics-grid">
          <!-- Metric 1: Bus Status -->
          <article class="metric-card">
            <span class="metric-eyebrow">BUS STATUS</span>
            <div class="metric-value-wrap">
              <h3 class="metric-title">GPS ACTIVE</h3>
              <span class="metric-pill">SIMULATION</span>
            </div>
          </article>

          <!-- Metric 2: Estimated Arrival -->
          <article class="metric-card">
            <span class="metric-eyebrow">ESTIMATED ARRIVAL</span>
            <div class="metric-value-wrap">
              <h3 class="metric-title">${escapeHtml(dynamicEta)}</h3>
              <span class="metric-sub">${escapeHtml(schoolName)} &middot; ${telem ? escapeHtml(telem.statusText) : 'approximate'}</span>
            </div>
          </article>

          <!-- Metric 3: Latest Location -->
          <article class="metric-card">
            <span class="metric-eyebrow">LATEST LOCATION</span>
            <div class="metric-value-wrap">
              <h3 class="metric-title">${escapeHtml(currentStop)}</h3>
              <span class="metric-sub">${nowDateStr}, ${nowTimeStr}</span>
            </div>
          </article>

          <!-- Metric 4: Monthly Transport Payment (250 LKR / KM) -->
          <article class="metric-card payment-metric-card" id="dashPaymentMetricCard">
            <span class="metric-eyebrow">MONTHLY PAYMENT</span>
            <div class="metric-value-wrap">
              <h3 class="metric-title" id="dashMonthlyAmount">LKR ${monthlyPayment.toLocaleString()}</h3>
              <span class="metric-pill metric-pill-emerald" id="dashPaymentPill">${isPaid ? 'PAID' : '250 LKR / KM'}</span>
            </div>
            <div class="metric-sub" id="dashPaymentSub">
              ${appliedDistanceKm} KM &times; 250 LKR = <b>LKR ${monthlyPayment.toLocaleString()} / mo</b>
            </div>
          </article>
        </section>

        <!-- 3. MONTHLY TRANSPORT PAYMENT & BILLING PANEL -->
        <section class="dash-payment-section">
          <article class="dash-panel-card payment-banner-card">
            <div class="panel-card-head" style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:12px;">
              <div>
                <span class="panel-eyebrow">MONTHLY TRANSPORT PAYMENT</span>
                <h3 class="panel-title">Transport Billing & Fee Calculator</h3>
                <p class="panel-subtitle">Calculated monthly transport fee based on 1 KM &times; rate per km (Rate: 250 LKR/km).</p>
              </div>
              <button type="button" class="btn-pay-monthly" id="openPayModalBtn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>
                <span id="payBtnLabel">${isPaid ? "View Payment Receipt" : "Pay Monthly Fee (LKR " + monthlyPayment.toLocaleString() + ")"}</span>
              </button>
            </div>

            <div class="payment-overview-grid">
              <div class="payment-stat-box">
                <span class="stat-label">RATE PER KM</span>
                <div class="stat-value text-emerald">250 LKR</div>
                <small class="stat-hint">Fixed rate per 1 KM</small>
              </div>

              <div class="payment-stat-box">
                <span class="stat-label">APPLIED DISTANCE</span>
                <div class="stat-value d-flex align-items-baseline gap-2">
                  <span id="displayDistanceVal">${appliedDistanceKm}</span> <span class="stat-unit">KM</span>
                </div>
                <div class="distance-selector-pills">
                  <button type="button" class="dist-pill ${appliedDistanceKm === 1 ? 'active' : ''}" data-km="1">1 KM (Base)</button>
                  <button type="button" class="dist-pill ${appliedDistanceKm === routeDistanceKm ? 'active' : ''}" data-km="${routeDistanceKm}">Route (${routeDistanceKm} KM)</button>
                  <button type="button" class="dist-pill" id="customKmBtn">Custom KM</button>
                </div>
              </div>

              <div class="payment-stat-box">
                <span class="stat-label">BILLING FORMULA</span>
                <div class="stat-value font-monospace" style="font-size:16px" id="displayFormula">
                  ${appliedDistanceKm} KM &times; 250 LKR
                </div>
                <small class="stat-hint">1 KM &times; rate per km calculation</small>
              </div>

              <div class="payment-stat-box highlight-box">
                <span class="stat-label">MONTHLY TOTAL</span>
                <div class="stat-value" id="displayMonthlyTotal" style="color:var(--cyan)">
                  LKR ${monthlyPayment.toLocaleString()}
                </div>
                <div class="stat-status">
                  <span class="status-indicator-badge ${isPaid ? 'paid' : 'due'}" id="dashPaymentBadge">
                    ${isPaid ? 'PAID &bull; ' + billingMonth.toUpperCase() : 'PAYMENT DUE &bull; ' + billingMonth.toUpperCase()}
                  </span>
                </div>
              </div>
            </div>
          </article>
        </section>

        <!-- 4. TWO-COLUMN SPLIT (Read-only Comprehensive Details + Recent Updates) -->
        <section class="dash-split-grid">
          <!-- LEFT: YOUR CHILD Comprehensive Read-only Card -->
          <article class="dash-panel-card">
            <div class="panel-card-head">
              <span class="panel-eyebrow">YOUR CHILD</span>
              <h3 class="panel-title">${escapeHtml(childName)}</h3>
              <p class="panel-subtitle">${escapeHtml(gradeLevel)} &middot; ${escapeHtml(schoolName)}</p>
            </div>

            <div class="telemetry-rows-list">
              <div class="telemetry-row">
                <span class="row-label">Assigned bus</span>
                <b class="row-value">${escapeHtml(assignedMorningBus.reg)} <small class="row-tag">Morning</small> &middot; ${escapeHtml(assignedReturnBus.reg)} <small class="row-tag">Return</small></b>
              </div>

              <div class="telemetry-row">
                <span class="row-label">Route</span>
                <b class="row-value">${escapeHtml(assignedRoute.name)}</b>
              </div>

              <div class="telemetry-row">
                <span class="row-label">Monthly transport payment</span>
                <b class="row-value" style="color:var(--cyan)">
                  LKR ${monthlyPayment.toLocaleString()} / month 
                  <small class="row-tag" style="background:rgba(16,185,129,0.18);color:#10b981">Rate: 250 LKR/km &times; ${appliedDistanceKm} km</small>
                </b>
              </div>

              <div class="telemetry-row">
                <span class="row-label">Current bus location</span>
                <b class="row-value">${escapeHtml(currentStop)} <span class="row-speed">(28 km/h)</span></b>
              </div>

              <div class="telemetry-row">
                <span class="row-label">Estimated arrival</span>
                <b class="row-value" style="color:var(--cyan)">~8 min remaining</b>
              </div>

              <div class="telemetry-row">
                <span class="row-label">GPS Telemetry</span>
                <b class="row-value"><span class="status-indicator-dot"></span> Active &middot; Live tracking enabled</b>
              </div>

              <div class="telemetry-row">
                <span class="row-label">Pickup stop</span>
                <b class="row-value">${escapeHtml(pickupStop)} (06:45 AM)</b>
              </div>

              <div class="telemetry-row">
                <span class="row-label">Drop-off stop</span>
                <b class="row-value">${escapeHtml(dropoffStop)} (07:30 AM)</b>
              </div>

              <div class="telemetry-row">
                <span class="row-label">Assigned driver</span>
                <b class="row-value">${escapeHtml(assignedMorningBus.driver)} &middot; ${escapeHtml(assignedMorningBus.phone)}</b>
              </div>
            </div>

            <div class="panel-card-foot">
              <a href="student" class="panel-link">
                <span>View student details</span>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                  <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
              </a>
            </div>
          </article>

          <!-- RIGHT: RECENT UPDATES List -->
          <article class="dash-panel-card">
            <div class="panel-card-head">
              <span class="panel-eyebrow">RECENT UPDATES</span>
              <h3 class="panel-title">Stay in the know.</h3>
            </div>

            <div class="updates-feed-list">
              ${recentNotices.map(n => {
                const title = escapeHtml(field(n, "title", "subject", "UPDATE"));
                const msg = escapeHtml(field(n, "message", "body", "content", ""));
                return `
                  <div class="update-feed-item">
                    <h4 class="update-item-title">${title}</h4>
                    <p class="update-item-msg">${msg}</p>
                  </div>
                `;
              }).join("")}
            </div>

            <div class="panel-card-foot">
              <a href="notifications" class="panel-link">
                <span>View all updates</span>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                  <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
              </a>
            </div>
          </article>
        </section>
      </div>
    `;

    // --- Interactive Distance & Payment Logic ---
    function updatePaymentDisplay(newKm) {
      appliedDistanceKm = Math.max(0.5, Math.round(newKm * 10) / 10);
      localStorage.setItem(`nextgo_school_distance_${activeStudentId}`, String(appliedDistanceKm));
      monthlyPayment = Math.round(appliedDistanceKm * RATE_PER_KM);

      // Metric card
      const amtEl = document.getElementById("dashMonthlyAmount");
      if (amtEl) amtEl.textContent = `LKR ${monthlyPayment.toLocaleString()}`;
      const subEl = document.getElementById("dashPaymentSub");
      if (subEl) subEl.innerHTML = `${appliedDistanceKm} KM &times; 250 LKR = <b>LKR ${monthlyPayment.toLocaleString()} / mo</b>`;

      // Payment panel
      const distVal = document.getElementById("displayDistanceVal");
      if (distVal) distVal.textContent = appliedDistanceKm;
      const formulaEl = document.getElementById("displayFormula");
      if (formulaEl) formulaEl.textContent = `${appliedDistanceKm} KM × 250 LKR`;
      const totalEl = document.getElementById("displayMonthlyTotal");
      if (totalEl) totalEl.textContent = `LKR ${monthlyPayment.toLocaleString()}`;

      // Pay button label
      const payLabel = document.getElementById("payBtnLabel");
      if (payLabel && !isPaid) {
        payLabel.textContent = `Pay Monthly Fee (LKR ${monthlyPayment.toLocaleString()})`;
      }

      // Update pills
      document.querySelectorAll(".dist-pill[data-km]").forEach(p => {
        const pKm = parseFloat(p.getAttribute("data-km"));
        if (Math.abs(pKm - appliedDistanceKm) < 0.05) {
          p.classList.add("active");
        } else {
          p.classList.remove("active");
        }
      });
    }

    document.querySelectorAll(".dist-pill[data-km]").forEach(btn => {
      btn.addEventListener("click", () => {
        const km = parseFloat(btn.getAttribute("data-km"));
        if (!isNaN(km)) updatePaymentDisplay(km);
      });
    });

    const customBtn = document.getElementById("customKmBtn");
    if (customBtn) {
      customBtn.addEventListener("click", () => {
        const val = prompt("Enter transport distance in KM (rate: 250 LKR / km):", String(appliedDistanceKm));
        if (val !== null) {
          const num = parseFloat(val);
          if (!isNaN(num) && num > 0) {
            updatePaymentDisplay(num);
          } else {
            alert("Please enter a valid positive number for distance in KM.");
          }
        }
      });
    }

    // Modal Payment Flow
    function openPaymentDialog() {
      const existingModal = document.querySelector(".school-modal-backdrop");
      if (existingModal) existingModal.remove();

      const modalHtml = `
        <div class="school-modal-backdrop" id="schoolPaymentModalBackdrop">
          <div class="school-modal-dialog">
            <div class="school-modal-head">
              <h3 style="margin:0;font-size:18px;font-weight:800;color:var(--text);display:flex;align-items:center;gap:8px;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>
                ${isPaid ? "Payment Receipt" : "Monthly Transport Payment"}
              </h3>
              <button type="button" id="closeModalBtn" style="background:none;border:none;color:var(--muted);cursor:pointer;font-size:22px;line-height:1;">&times;</button>
            </div>
            <div class="school-modal-body">
              ${isPaid ? `
                <div style="text-align:center;padding:12px 0 20px;">
                  <div style="width:52px;height:52px;border-radius:50%;background:rgba(16,185,129,0.15);color:#10b981;display:inline-flex;align-items:center;justify-content:center;margin-bottom:12px;">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                  </div>
                  <h4 style="margin:0 0 4px;font-size:20px;font-weight:800;color:#10b981;">Payment Confirmed</h4>
                  <p style="margin:0;font-size:13px;color:var(--muted);">Receipt #REC-SCH-${activeStudentId}-${new Date().getMonth() + 1}${new Date().getFullYear()}</p>
                </div>
                <div style="background:rgba(16,185,129,0.06);border:1px solid rgba(16,185,129,0.2);border-radius:12px;padding:16px;margin-bottom:16px;">
                  <div style="display:flex;justify-content:space-between;margin-bottom:8px;font-size:13.5px;">
                    <span style="color:var(--muted)">Student Name:</span>
                    <strong style="color:var(--text)">${escapeHtml(childName)}</strong>
                  </div>
                  <div style="display:flex;justify-content:space-between;margin-bottom:8px;font-size:13.5px;">
                    <span style="color:var(--muted)">School & Route:</span>
                    <span style="color:var(--text)">${escapeHtml(schoolName)}</span>
                  </div>
                  <div style="display:flex;justify-content:space-between;margin-bottom:8px;font-size:13.5px;">
                    <span style="color:var(--muted)">Distance Billed:</span>
                    <span style="color:var(--text)">${appliedDistanceKm} KM</span>
                  </div>
                  <div style="display:flex;justify-content:space-between;margin-bottom:8px;font-size:13.5px;">
                    <span style="color:var(--muted)">Rate Applied:</span>
                    <span style="color:var(--text)">250 LKR / KM</span>
                  </div>
                  <div style="display:flex;justify-content:space-between;border-top:1px dashed var(--border);padding-top:10px;margin-top:6px;font-size:15px;">
                    <strong style="color:var(--text)">Amount Paid:</strong>
                    <strong style="color:#10b981;font-size:18px;">LKR ${monthlyPayment.toLocaleString()}</strong>
                  </div>
                </div>
              ` : `
                <div style="margin-bottom:16px;">
                  <p style="margin:0 0 12px;font-size:13.5px;color:var(--muted);">
                    Pay your student's monthly transport subscription for <strong>${billingMonth}</strong>.
                  </p>
                  <div style="background:var(--card-hover, rgba(0,0,0,0.02));border:1px solid var(--border);border-radius:12px;padding:14px;margin-bottom:16px;">
                    <div style="display:flex;justify-content:space-between;margin-bottom:8px;font-size:13.5px;">
                      <span style="color:var(--muted)">Child:</span>
                      <strong style="color:var(--text)">${escapeHtml(childName)}</strong>
                    </div>
                    <div style="display:flex;justify-content:space-between;margin-bottom:8px;font-size:13.5px;">
                      <span style="color:var(--muted)">Destination:</span>
                      <span style="color:var(--text)">${escapeHtml(schoolName)}</span>
                    </div>
                    <div style="display:flex;justify-content:space-between;margin-bottom:8px;font-size:13.5px;">
                      <span style="color:var(--muted)">Pickup Stop:</span>
                      <span style="color:var(--text)">${escapeHtml(pickupStop)}</span>
                    </div>
                    <div style="display:flex;justify-content:space-between;margin-bottom:8px;font-size:13.5px;">
                      <span style="color:var(--muted)">One-Way Distance:</span>
                      <span style="color:var(--text)"><strong>${appliedDistanceKm} KM</strong></span>
                    </div>
                    <div style="display:flex;justify-content:space-between;margin-bottom:8px;font-size:13.5px;">
                      <span style="color:var(--muted)">Rate per KM:</span>
                      <span style="color:var(--text)">250 LKR</span>
                    </div>
                    <div style="display:flex;justify-content:space-between;border-top:1px solid var(--border);padding-top:10px;margin-top:8px;font-size:16px;">
                      <strong style="color:var(--text)">Monthly Total:</strong>
                      <strong style="color:var(--cyan);font-size:19px;">LKR ${monthlyPayment.toLocaleString()}</strong>
                    </div>
                  </div>

                  <div style="margin-bottom:12px;">
                    <label style="display:block;font-size:12px;font-weight:700;color:var(--muted);text-transform:uppercase;margin-bottom:6px;">Select Payment Method</label>
                    <select id="schoolPayMethod" style="width:100%;padding:10px 12px;border-radius:10px;border:1px solid var(--border);background:var(--card);color:var(--text);font-size:13.5px;">
                      <option value="CARD">Credit / Debit Card (Online)</option>
                      <option value="BANK">Bank Transfer (Direct Slip)</option>
                      <option value="CASH">Cash at Transport Office</option>
                      <option value="WALLET">NEXTGO Passenger Wallet</option>
                    </select>
                  </div>
                </div>
              `}
            </div>
            <div class="school-modal-foot">
              <button type="button" id="cancelModalBtn" style="padding:10px 16px;border-radius:10px;border:1px solid var(--border);background:transparent;color:var(--text);font-weight:600;cursor:pointer;">
                ${isPaid ? "Close" : "Cancel"}
              </button>
              ${!isPaid ? `
                <button type="button" id="confirmPayBtn" class="btn-pay-monthly" style="padding:10px 20px;">
                  Confirm & Pay LKR ${monthlyPayment.toLocaleString()}
                </button>
              ` : `
                <button type="button" id="resetPayBtn" style="padding:10px 16px;border-radius:10px;border:1px solid var(--border);background:transparent;color:var(--muted);font-size:12px;cursor:pointer;">
                  Mark as Unpaid (Reset)
                </button>
              `}
            </div>
          </div>
        </div>
      `;

      document.body.insertAdjacentHTML("beforeend", modalHtml);

      const close = () => {
        const m = document.getElementById("schoolPaymentModalBackdrop");
        if (m) m.remove();
      };

      document.getElementById("closeModalBtn")?.addEventListener("click", close);
      document.getElementById("cancelModalBtn")?.addEventListener("click", close);

      document.getElementById("confirmPayBtn")?.addEventListener("click", () => {
        localStorage.setItem(paymentStorageKey, "PAID");
        isPaid = true;
        close();
        if (typeof showToast === "function") {
          showToast(`Monthly payment of LKR ${monthlyPayment.toLocaleString()} completed successfully!`);
        }

        // Update badges & labels in UI
        const pill = document.getElementById("dashPaymentPill");
        if (pill) pill.textContent = "PAID";
        const badge = document.getElementById("dashPaymentBadge");
        if (badge) {
          badge.className = "status-indicator-badge paid";
          badge.innerHTML = `PAID &bull; ${billingMonth.toUpperCase()}`;
        }
        const payLabel = document.getElementById("payBtnLabel");
        if (payLabel) payLabel.textContent = "View Payment Receipt";
      });

      document.getElementById("resetPayBtn")?.addEventListener("click", () => {
        localStorage.removeItem(paymentStorageKey);
        isPaid = false;
        close();
        if (typeof showToast === "function") {
          showToast("Payment status reset to Due.");
        }
        const pill = document.getElementById("dashPaymentPill");
        if (pill) pill.textContent = "250 LKR / KM";
        const badge = document.getElementById("dashPaymentBadge");
        if (badge) {
          badge.className = "status-indicator-badge due";
          badge.innerHTML = `PAYMENT DUE &bull; ${billingMonth.toUpperCase()}`;
        }
        const payLabel = document.getElementById("payBtnLabel");
        if (payLabel) payLabel.textContent = `Pay Monthly Fee (LKR ${monthlyPayment.toLocaleString()})`;
      });
    }

    const openPayBtn = document.getElementById("openPayModalBtn");
    if (openPayBtn) openPayBtn.addEventListener("click", openPaymentDialog);

    syncTopbarChildSelector();
    refreshNotificationsUI();
  } catch (err) {
    main.innerHTML = `
      <header class="topbar">
        ${renderTopbarLeft("Dashboard", "School Student Transport")}
        ${renderTopControls()}
      </header>
      ${errorView(err.message)}
    `;
  }
})();
