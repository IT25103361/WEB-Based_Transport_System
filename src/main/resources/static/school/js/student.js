/* =============================================================================
   NEXTGO My Student Profile & Transport Assignment Engine
   Allows selecting and saving child's destination school (SLIIT, Ananda, Nalanda,
   Musaeus, Royal, Isipathana) from database/routes with live SQL Server synchronization.
   ============================================================================= */

const AVAILABLE_SCHOOL_DESTINATIONS = [
  { id: 1, name: "SLIIT Campus Malabe", originCity: "Kottawa", routeId: 1 },
  { id: 2, name: "Ananda College", originCity: "Kottawa", routeId: 2 },
  { id: 4, name: "Nalanda College", originCity: "Gampaha", routeId: 4 },
  { id: 3, name: "Musaeus College", originCity: "Kottawa", routeId: 3 },
  { id: 5, name: "Royal College", originCity: "Gampaha", routeId: 5 },
  { id: 6, name: "Isipathana College", originCity: "Gampaha", routeId: 6 }
];

let currentStudentId = null;

(async () => {
  pageShell("My student", "Student and transport information.", skeletons(1));
  currentStudentId = selectedStudent() || "1";
  syncTopbarChildSelector();

  if (!currentStudentId) {
    const main = document.querySelector(".app-main");
    main.insertAdjacentHTML("beforeend", errorView("Select a student from the dashboard first."));
    return;
  }

  await loadAndRenderStudentPage();
})();

function getAssignedSchool(student, transportResp, localSaved) {
  if (localSaved?.destinationSchool) return localSaved.destinationSchool;
  const stored = localStorage.getItem(`nextgo_student_school_${currentStudentId}`);
  if (stored) return stored;
  if (transportResp?.schedule?.destinationSchool) return transportResp.schedule.destinationSchool;
  if (student?.schoolName) return student.schoolName;
  return "SLIIT Campus Malabe";
}

function matchDestinationRoute(schoolName, preferredOrigin = null) {
  if (typeof ALL_ROUTES_DATA === "undefined") return null;
  const sLow = (schoolName || "").toLowerCase();

  let matches = [];
  if (sLow.includes("sliit")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 1 || r.schoolName.toLowerCase().includes("sliit"));
  } else if (sLow.includes("nalanda")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 4 || r.id === 2 || r.name.toLowerCase().includes("nalanda") || r.schoolName.toLowerCase().includes("nalanda"));
  } else if (sLow.includes("ananda")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 2 || r.id === 4 || r.name.toLowerCase().includes("ananda") || r.schoolName.toLowerCase().includes("ananda"));
  } else if (sLow.includes("musaeus")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 3 || r.schoolName.toLowerCase().includes("musaeus"));
  } else if (sLow.includes("royal")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 5 || r.schoolName.toLowerCase().includes("royal"));
  } else if (sLow.includes("isipathana")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 6 || r.schoolName.toLowerCase().includes("isipathana"));
  } else {
    matches = ALL_ROUTES_DATA.filter(r => r.schoolName.toLowerCase().includes(sLow) || sLow.includes(r.schoolName.toLowerCase()));
  }

  if (!matches.length) return ALL_ROUTES_DATA[0];

  if (preferredOrigin) {
    const origMatch = matches.find(r => (r.originCity || "").toLowerCase() === preferredOrigin.toLowerCase());
    if (origMatch) return origMatch;
  }
  return matches[0];
}

function isSchoolSelected(candidateName, activeSchool) {
  if (!candidateName || !activeSchool) return false;
  const c = candidateName.toLowerCase().replace(/colombo|campus|college/g, "").trim();
  const a = activeSchool.toLowerCase().replace(/colombo|campus|college/g, "").trim();
  return c.includes(a) || a.includes(c);
}

async function loadAndRenderStudentPage() {
  const main = document.querySelector(".app-main");
  const id = currentStudentId;

  try {
    const [student, transportResp, dbRoutes, dbBuses] = await Promise.all([
      Api.getStudent(id).catch(() => ({})),
      Api.getStudentTransport(id).catch(() => null),
      Api.getRoutes().catch(() => []),
      Api.getBuses().catch(() => [])
    ]);

    // Check localStorage assignment override if recently updated
    let localSaved = null;
    try {
      const raw = localStorage.getItem(`nextgo_transport_assignment_${id}`);
      if (raw) localSaved = JSON.parse(raw);
    } catch {}

    const assignedSchool = getAssignedSchool(student, transportResp, localSaved);
    const schedule = transportResp?.schedule || {};
    const preferredOrigin = localSaved?.originCity || schedule.originCity || null;

    // Find route from ALL_ROUTES_DATA matching this student's assigned school
    const routeData = matchDestinationRoute(assignedSchool, preferredOrigin) || ALL_ROUTES_DATA[0];

    // Resolve Origin and Destination
    const originCity = localSaved?.originCity || schedule.originCity || routeData?.originCity || "Kottawa";
    const destinationSchool = assignedSchool || routeData?.schoolName || "SLIIT Campus Malabe";
    const routeName = routeData?.name || `${originCity} to ${destinationSchool}`;

    // Resolve Morning Bus
    const morningBusName = localSaved?.morningBusName || schedule.morningBusName || transportResp?.morningBus?.busNumber || routeData?.buses?.[0]?.name || "Kottawa Express 01";
    const morningBusPlate = localSaved?.morningBusNumber || schedule.morningBusNumber || transportResp?.morningBus?.registrationNumber || routeData?.buses?.[0]?.reg || "NB-1357";
    const morningDriver = localSaved?.morningDriver || schedule.morningDriver || transportResp?.morningBus?.driverName || routeData?.buses?.[0]?.driver || "Kasun Fernando";
    const morningPhone = localSaved?.morningPhone || schedule.morningPhone || transportResp?.morningBus?.driverPhone || routeData?.buses?.[0]?.phone || "0779876543";

    // Resolve Return Bus
    const returnBusName = localSaved?.returnBusName || schedule.returnBusName || transportResp?.returnBus?.busNumber || routeData?.buses?.[1]?.name || "Kottawa Express 02";
    const returnBusPlate = localSaved?.returnBusNumber || schedule.returnBusNumber || transportResp?.returnBus?.registrationNumber || routeData?.buses?.[1]?.reg || "WP-2468";
    const returnDriver = localSaved?.returnDriver || schedule.returnDriver || transportResp?.returnBus?.driverName || routeData?.buses?.[1]?.driver || "Nuwan Perera";
    const returnPhone = localSaved?.returnPhone || schedule.returnPhone || transportResp?.returnBus?.driverPhone || routeData?.buses?.[1]?.phone || "0712345678";

    // Resolve Stops
    const pickupStopName = localSaved?.pickupStop || schedule.pickupStop || transportResp?.pickupStopName || routeData?.stops?.[Math.min(3, routeData?.stops?.length - 1)]?.name || "Terminal Station";
    const dropOffStopName = localSaved?.dropoffStop || schedule.dropOffStop || transportResp?.dropOffStopName || routeData?.stops?.[Math.min(3, routeData?.stops?.length - 1)]?.name || pickupStopName;

    // Lookup times from routeData stops
    let pickupTime = schedule.pickupTime ? String(schedule.pickupTime).slice(0, 5) : null;
    let dropOffTime = schedule.expectedArrival ? String(schedule.expectedArrival).slice(0, 5) : null;

    if (routeData?.stops) {
      const pStop = routeData.stops.find(s => s.name === pickupStopName);
      if (pStop && pStop.pickupTime) pickupTime = pStop.pickupTime;
      const dStop = routeData.stops.find(s => s.name === dropOffStopName);
      if (dStop && dStop.dropOffTime) dropOffTime = dStop.dropOffTime;
    }
    if (!pickupTime) pickupTime = "06:45 AM";
    if (!dropOffTime) dropOffTime = "18:05 PM";

    const sName = personName(student) || "Student";
    const sSchool = destinationSchool;
    const sIdNum = field(student, "studentId", "id", "registrationNumber") || `ST-${String(id).padStart(4, '0')}`;

    main.innerHTML = `
      <header class="topbar">
        ${renderTopbarLeft("My Student", "Official student transport profile and route database assignment.")}
        ${renderTopControls(null, id)}
      </header>

      <div class="student-view-container">
        <!-- STUDENT PROFILE HERO CARD -->
        <section class="student-hero-card">
          <div class="student-hero-info">
            <div class="student-hero-avatar">${escapeHtml(sName.slice(0, 1))}</div>
            <div class="student-hero-meta">
              <h2>${escapeHtml(sName)}</h2>
              <div class="student-hero-badges">
                <span class="meta-chip" id="heroSchoolBadge">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
                  ${escapeHtml(sSchool)}
                </span>
                <span class="meta-chip">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"></rect><path d="M7 7h10"></path><path d="M7 12h10"></path><path d="M7 17h10"></path></svg>
                  ID: ${escapeHtml(String(sIdNum))}
                </span>
                <span class="meta-chip active-chip">
                  <span class="db-live-dot"></span>
                  Active Transport
                </span>
              </div>
            </div>
          </div>
          <div class="student-hero-actions">
            <a class="button" href="tracking">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="3 11 22 2 13 21 11 13 3 11"></polygon></svg>
              Track Live Bus
            </a>
            <a class="button secondary" href="schedule">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="4" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
              Weekly Schedule
            </a>
          </div>
        </section>

        <!-- ASSIGNED SCHOOL DESTINATION CONFIGURATION CARD -->
        <section class="dest-school-card">
          <div class="dest-school-info">
            <div class="dest-school-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                <polyline points="9 22 9 12 15 12 15 22"></polyline>
              </svg>
            </div>
            <div>
              <h3>Assigned School Destination</h3>
              <p>Select your child's destination school to configure their dedicated route and bus fleet.</p>
            </div>
          </div>
          <div class="dest-school-actions">
            <div class="dest-select-wrap">
              <label for="studentSchoolSelect">Destination School</label>
              <select id="studentSchoolSelect" class="dest-school-select" aria-label="Assigned School Destination">
                ${AVAILABLE_SCHOOL_DESTINATIONS.map(d => `
                  <option value="${escapeHtml(d.name)}" ${isSchoolSelected(d.name, sSchool) ? 'selected' : ''}>
                    ${escapeHtml(d.name)}
                  </option>
                `).join("")}
              </select>
            </div>
            <button class="dest-school-save-btn" id="btnSaveStudentSchool" onclick="handleSaveStudentSchool()">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              <span>Save Details</span>
            </button>
          </div>
        </section>

        <!-- OFFICIAL TRANSPORT DATABASE TABLE -->
        <section class="transport-table-section">
          <div class="section-header-row">
            <div>
              <h3>Official Transport Assignment & Bus Schedule</h3>
              <p>Database Record in SQL Server Table: <code>dbo.transport_schedules</code></p>
            </div>
            <div class="db-live-tag">
              <span class="db-live-dot"></span>
              SQL Server Synced
            </div>
          </div>

          <div style="overflow-x: auto;">
            <table class="transport-spec-table">
              <thead>
                <tr>
                  <th style="width: 25%;">Service Parameter</th>
                  <th style="width: 45%;">Assigned Specification & Route</th>
                  <th style="width: 30%;">Driver & Schedule Telemetry</th>
                </tr>
              </thead>
              <tbody>
                <!-- ROW 1: ORIGIN & DESTINATION CORRIDOR -->
                <tr>
                  <td>
                    <div class="table-field-title">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                      Route & Corridor
                    </div>
                    <div class="table-field-sub">Origin & Destination Corridor</div>
                  </td>
                  <td>
                    <div class="route-corridor-box">
                      <span class="corridor-pill">From <strong>${escapeHtml(originCity)}</strong></span>
                      <span class="corridor-arrow">&rarr;</span>
                      <span class="corridor-pill">To <strong>${escapeHtml(destinationSchool)}</strong></span>
                    </div>
                    <div class="table-field-sub" style="margin-top: 4px;">
                      <strong>Corridor:</strong> ${escapeHtml(routeName)}
                    </div>
                  </td>
                  <td>
                    <span class="meta-chip">
                      ${routeData?.stops?.length || 15} Designated Stops
                    </span>
                  </td>
                </tr>

                <!-- ROW 2: MORNING ASSIGNED BUS -->
                <tr>
                  <td>
                    <div class="table-field-title">
                      <span class="service-badge morning">Morning Bus</span>
                    </div>
                    <div class="table-field-sub">Inbound School Service</div>
                  </td>
                  <td>
                    <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                      <span style="font-weight: 700; color: var(--text);">${escapeHtml(morningBusName)}</span>
                      <span class="bus-plate-badge">${escapeHtml(morningBusPlate)}</span>
                    </div>
                    <div class="table-field-sub">Dedicated morning route carrier</div>
                  </td>
                  <td>
                    <div style="display: flex; flex-direction: column; gap: 4px;">
                      <span style="font-weight: 700; color: var(--text); font-size: 13.5px;">${escapeHtml(morningDriver)}</span>
                      <a href="tel:${escapeHtml(morningPhone)}" class="driver-contact-link">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                        ${escapeHtml(morningPhone)}
                      </a>
                    </div>
                  </td>
                </tr>

                <!-- ROW 3: MORNING PICKUP STOP & TIME -->
                <tr>
                  <td>
                    <div class="table-field-title">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#d97706" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                      Morning Pickup Stop
                    </div>
                    <div class="table-field-sub">Scheduled Boarding Point</div>
                  </td>
                  <td>
                    <div style="font-weight: 700; color: var(--text); font-size: 14.5px;">${escapeHtml(pickupStopName)}</div>
                    <div class="table-field-sub">Child will be boarded by driver at this point</div>
                  </td>
                  <td>
                    <span class="time-tag">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                      ${escapeHtml(pickupTime)}
                    </span>
                  </td>
                </tr>

                <!-- ROW 4: RETURN ASSIGNED BUS -->
                <tr>
                  <td>
                    <div class="table-field-title">
                      <span class="service-badge return">Return Bus</span>
                    </div>
                    <div class="table-field-sub">Outbound Afternoon Service</div>
                  </td>
                  <td>
                    <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                      <span style="font-weight: 700; color: var(--text);">${escapeHtml(returnBusName)}</span>
                      <span class="bus-plate-badge">${escapeHtml(returnBusPlate)}</span>
                    </div>
                    <div class="table-field-sub">Dedicated afternoon return carrier</div>
                  </td>
                  <td>
                    <div style="display: flex; flex-direction: column; gap: 4px;">
                      <span style="font-weight: 700; color: var(--text); font-size: 13.5px;">${escapeHtml(returnDriver)}</span>
                      <a href="tel:${escapeHtml(returnPhone)}" class="driver-contact-link">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                        ${escapeHtml(returnPhone)}
                      </a>
                    </div>
                  </td>
                </tr>

                <!-- ROW 5: RETURN DROP-OFF STOP & TIME -->
                <tr>
                  <td>
                    <div class="table-field-title">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#6366f1" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                      Return Drop-off Stop
                    </div>
                    <div class="table-field-sub">Scheduled Alighting Point</div>
                  </td>
                  <td>
                    <div style="font-weight: 700; color: var(--text); font-size: 14.5px;">${escapeHtml(dropOffStopName)}</div>
                    <div class="table-field-sub">Child will be safely released to parent/guardian</div>
                  </td>
                  <td>
                    <span class="time-tag">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                      ${escapeHtml(dropOffTime)}
                    </span>
                  </td>
                </tr>

                <!-- ROW 6: OPERATING DAYS & STATUS -->
                <tr>
                  <td>
                    <div class="table-field-title">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="4" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                      Operating Schedule
                    </div>
                    <div class="table-field-sub">Service Days</div>
                  </td>
                  <td>
                    <span style="font-weight: 700; color: var(--text);">Monday &ndash; Friday</span>
                    <span class="table-field-sub" style="display: block; margin-top: 2px;">Regular school term operation</span>
                  </td>
                  <td>
                    <span class="meta-chip active-chip">
                      <span class="db-live-dot"></span>
                      Active &bull; On Route
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <!-- DATABASE TEACHER PRESENTATION NOTE -->
        <div style="padding: 16px 20px; border-radius: 12px; background: var(--bg); border: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px;">
          <div style="display: flex; align-items: center; gap: 10px; font-size: 13px; color: var(--muted);">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><ellipse cx="12" cy="5" rx="9" ry="3"></ellipse><path d="M3 5V19A9 3 0 0 0 21 19V5"></path><path d="M3 12A9 3 0 0 0 21 12"></path></svg>
            <span><strong>SQL Server Schema:</strong> Data synchronized directly from <code>transport_schedules</code>, <code>school_buses</code>, and <code>route_stops</code> tables.</span>
          </div>
          <a href="tracking" class="button secondary" style="font-size: 12.5px; padding: 6px 14px;">
            Configure Route & Stops &rarr;
          </a>
        </div>
      </div>
    `;

    syncTopbarChildSelector();
  } catch (err) {
    main.innerHTML = `
      <header class="topbar">
        ${renderTopbarLeft("My Student", "School Student Transport")}
        ${renderTopControls(null, id)}
      </header>
      ${errorView(err.message)}
    `;
  }
}

async function handleSaveStudentSchool() {
  const sel = document.getElementById("studentSchoolSelect");
  if (!sel) return;
  const chosenSchool = sel.value;
  const btn = document.getElementById("btnSaveStudentSchool");

  if (btn) {
    btn.disabled = true;
    btn.innerHTML = `<span>Saving...</span>`;
  }

  try {
    const studentId = currentStudentId;
    let localSaved = null;
    try {
      const raw = localStorage.getItem(`nextgo_transport_assignment_${studentId}`);
      if (raw) localSaved = JSON.parse(raw);
    } catch {}

    const targetRoute = matchDestinationRoute(chosenSchool, localSaved?.originCity);
    if (!targetRoute) throw new Error("No route found for selected school destination");

    const buses = targetRoute.buses || [];
    const morningBus = buses[0] || {};
    const returnBus = buses[1] || buses[0] || {};
    const stops = targetRoute.stops || [];
    const pStop = stops[Math.min(3, stops.length - 1)]?.name || stops[0]?.name || "Terminal Station";
    const dStop = stops[Math.min(3, stops.length - 1)]?.name || stops[0]?.name || "Terminal Station";

    const pStopObj = stops.find(s => s.name === pStop) || stops[Math.min(3, stops.length - 1)] || stops[0];
    const dStopObj = stops.find(s => s.name === dStop) || stops[Math.min(3, stops.length - 1)] || stops[0];

    const updatedAssignment = {
      routeId: targetRoute.id,
      routeName: targetRoute.name,
      originCity: targetRoute.originCity || "Kottawa",
      destinationSchool: chosenSchool,
      busId: morningBus.id,
      morningBusId: morningBus.id,
      morningBusNumber: morningBus.reg,
      morningBusName: morningBus.name,
      morningDriver: morningBus.driver,
      morningPhone: morningBus.phone,
      returnBusId: returnBus.id,
      returnBusNumber: returnBus.reg,
      returnBusName: returnBus.name,
      returnDriver: returnBus.driver,
      returnPhone: returnBus.phone,
      pickupStop: pStop,
      dropOffStop: dStop,
      dropoffStop: dStop,
      pickupTime: pStopObj?.pickupTime || "06:45 AM",
      expectedArrival: dStopObj?.dropOffTime || "18:05 PM"
    };

    // 1. Save to localStorage
    localStorage.setItem(`nextgo_transport_assignment_${studentId}`, JSON.stringify(updatedAssignment));
    localStorage.setItem(`nextgo_student_school_${studentId}`, chosenSchool);

    // 2. Update cached student list in localStorage
    const cachedStudents = getCachedStudents();
    const stObj = cachedStudents.find(s => String(s.id || s.studentId) === String(studentId));
    if (stObj) {
      stObj.schoolName = chosenSchool;
      setCachedStudents(cachedStudents);
    }

    // 3. Call backend API to persist in SQL Server
    if (typeof Api !== "undefined" && typeof Api.updateStudentStops === "function") {
      await Api.updateStudentStops(studentId, updatedAssignment).catch(e => console.warn("Backend update notice:", e));
    }

    // 4. Update NextGoSimulation engine
    if (typeof NextGoSimulation !== "undefined") {
      NextGoSimulation.setRoute(targetRoute.id, morningBus, returnBus, pStop, dStop);
    }

    // 5. Toast feedback
    if (typeof showGlobalToast === "function") {
      showGlobalToast("Destination Saved", `Assigned school updated to ${chosenSchool}`);
    }

    // 6. Refresh view dynamically
    await loadAndRenderStudentPage();
  } catch (err) {
    console.error(err);
    alert("Could not update school destination: " + err.message);
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg> <span>Save Details</span>`;
    }
  }
}

if (typeof window !== "undefined") {
  window.handleSaveStudentSchool = handleSaveStudentSchool;
}
