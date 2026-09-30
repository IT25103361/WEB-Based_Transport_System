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

        <!-- 2. THREE SUMMARY METRIC CARDS IN A ROW -->
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
        </section>

        <!-- 3. TWO-COLUMN SPLIT (Read-only Comprehensive Details + Recent Updates) -->
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
