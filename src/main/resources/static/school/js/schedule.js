/* =============================================================================
   NEXTGO Transport Schedule - 2-Column Clean Cards (Strictly No Maps)
   Left Column: Morning Trips (To Schools)
   Right Column: Return Trips (Back Home)
   Highlights: Nalanda College & SLIIT Malabe
   ============================================================================= */

let currentStudentId = null;
let currentFilter = "all";
let activeStudentAssignment = null;
let currentStudentData = null;
let currentTransportData = null;

(async () => {
  pageShell("Transport schedule", "All morning departures to school & return trips back home.", skeletons(2));
  currentStudentId = selectedStudent() || "1";
  syncTopbarChildSelector();

  try {
    const [transport, student] = await Promise.all([
      Api.getStudentTransport(currentStudentId).catch(() => ({})),
      Api.getStudent(currentStudentId).catch(() => ({}))
    ]);

    currentTransportData = transport;
    currentStudentData = student;

    initAssignment(transport, student);
    renderAllSchedules();
  } catch (err) {
    const main = document.querySelector(".app-main");
    main.innerHTML = `
      <header class="topbar">
        ${renderTopbarLeft("Transport schedule", "School Student Transport")}
        ${renderTopControls(null, currentStudentId)}
      </header>
      ${errorView(err.message)}
    `;
  }
})();

function getStudentDestinationSchool() {
  const customSaved = localStorage.getItem(`nextgo_student_school_${currentStudentId}`);
  if (customSaved) return customSaved;
  const assignmentRaw = localStorage.getItem(`nextgo_transport_assignment_${currentStudentId}`);
  if (assignmentRaw) {
    try {
      const parsed = JSON.parse(assignmentRaw);
      if (parsed?.destinationSchool) return parsed.destinationSchool;
    } catch {}
  }
  if (currentStudentData?.schoolName) return currentStudentData.schoolName;
  if (currentTransportData?.schedule?.destinationSchool) return currentTransportData.schedule.destinationSchool;
  if (currentTransportData?.destinationSchool) return currentTransportData.destinationSchool;
  return "SLIIT Campus Malabe";
}

/**
 * Returns true if the student's destination school is served by this route.
 * A route like "Ananda & Nalanda College" serves BOTH schools, so students
 * of either school should be allowed to use it.
 */
function isSchoolCompatible(studentSchool, routeSchoolName, routeName = "") {
  if (!studentSchool || !routeSchoolName) return true;
  const s = studentSchool.toLowerCase().trim();
  const r = routeSchoolName.toLowerCase().trim();
  const rn = (routeName || "").toLowerCase().trim();

  const knownSchools = ["sliit", "ananda", "nalanda", "royal", "musaeus", "isipathana"];

  // Find which known school(s) the student belongs to
  const studentKeywords = knownSchools.filter(kw => s.includes(kw));

  // Find which known school(s) this route serves
  const routeKeywords = knownSchools.filter(kw => r.includes(kw) || rn.includes(kw));

  // If we can identify the student's school keyword(s), check at least one matches the route
  if (studentKeywords.length > 0 && routeKeywords.length > 0) {
    // Compatible if ANY of the student's school keywords appear in the route's list
    return studentKeywords.some(kw => routeKeywords.includes(kw));
  }

  // Fallback: simple substring match
  return r.includes(s) || s.includes(r);
}

/**
 * For a given route, returns only the school label that is relevant to the
 * current student (instead of the combined "Ananda & Nalanda College" string).
 */
function getRouteDestLabelForStudent(routeSchoolName, studentSchool) {
  if (!studentSchool) return routeSchoolName;
  const s = studentSchool.toLowerCase().trim();
  const knownSchools = ["sliit", "ananda", "nalanda", "royal", "musaeus", "isipathana"];
  const match = knownSchools.find(kw => s.includes(kw) && routeSchoolName.toLowerCase().includes(kw));
  if (match) {
    // Return the student's own school name instead of the combined route label
    return studentSchool;
  }
  return routeSchoolName;
}

function initAssignment(transport, student) {
  const saved = localStorage.getItem(`nextgo_transport_assignment_${currentStudentId}`);
  if (saved) {
    try {
      activeStudentAssignment = JSON.parse(saved);
      return;
    } catch {}
  }

  const school = getStudentDestinationSchool();
  let routeId = transport?.schedule?.routeId || 1;
  if (!transport?.schedule?.routeId) {
    if (school.toLowerCase().includes("nalanda")) routeId = 4;
    else if (school.toLowerCase().includes("ananda")) routeId = 2;
    else if (school.toLowerCase().includes("musaeus")) routeId = 3;
    else if (school.toLowerCase().includes("royal")) routeId = 5;
    else if (school.toLowerCase().includes("isipathana")) routeId = 6;
  }

  const r = ALL_ROUTES_DATA.find(x => x.id === routeId) || ALL_ROUTES_DATA[0];
  activeStudentAssignment = {
    routeId: r.id,
    routeName: r.name,
    morningBusId: r.buses[0]?.id,
    returnBusId: r.buses[1]?.id || r.buses[0]?.id,
    pickupStop: transport?.pickupStopName || r.stops[3]?.name || r.stops[0].name,
    dropoffStop: transport?.dropOffStopName || r.stops[3]?.name || r.stops[0].name
  };
}

function setFilter(filter) {
  currentFilter = filter;
  renderAllSchedules();
}

function renderAllSchedules() {
  const main = document.querySelector(".app-main");

  // Collect and filter routes
  let filteredRoutes = ALL_ROUTES_DATA;
  if (currentFilter === "sliit") {
    filteredRoutes = ALL_ROUTES_DATA.filter(r => r.schoolName.toLowerCase().includes("sliit"));
  } else if (currentFilter === "nalanda") {
    filteredRoutes = ALL_ROUTES_DATA.filter(r => r.name.toLowerCase().includes("nalanda") || r.schoolName.toLowerCase().includes("nalanda"));
  } else if (currentFilter === "ananda") {
    filteredRoutes = ALL_ROUTES_DATA.filter(r => r.schoolName.toLowerCase().includes("ananda"));
  } else if (currentFilter === "royal") {
    filteredRoutes = ALL_ROUTES_DATA.filter(r => r.schoolName.toLowerCase().includes("royal"));
  } else if (currentFilter === "musaeus") {
    filteredRoutes = ALL_ROUTES_DATA.filter(r => r.schoolName.toLowerCase().includes("musaeus"));
  } else if (currentFilter === "isipathana") {
    filteredRoutes = ALL_ROUTES_DATA.filter(r => r.schoolName.toLowerCase().includes("isipathana"));
  }

  // Generate Morning Cards (Left Column)
  const morningCards = [];
  // Generate Return Cards (Right Column)
  const returnCards = [];

  const studentSchool = getStudentDestinationSchool();

  filteredRoutes.forEach(r => {
    const isNalanda = r.name.toLowerCase().includes("nalanda") || r.schoolName.toLowerCase().includes("nalanda");
    const isSliit = r.schoolName.toLowerCase().includes("sliit");
    const originTerminal = r.stops[0]?.name || r.startLocation;
    const destSchool = r.schoolName;
    const isCompatible = isSchoolCompatible(studentSchool, r.schoolName, r.name);
    // For display: show only the student's own school name when the route serves multiple schools
    const destSchoolLabel = isCompatible
      ? getRouteDestLabelForStudent(r.schoolName, studentSchool)
      : r.schoolName; // show full combined name only when truly incompatible

    (r.buses || []).forEach(b => {
      const isAssignedMorning = activeStudentAssignment?.routeId === r.id && activeStudentAssignment?.morningBusId === b.id;
      const isAssignedReturn = activeStudentAssignment?.routeId === r.id && activeStudentAssignment?.returnBusId === b.id;

      const cardClass = "schedule-card-clean";

      // --- Morning Card ---
      morningCards.push(`
        <article class="${cardClass}">
          <div class="sched-card-top">
            <span class="sched-count-pill">${escapeHtml(r.code)}</span>
            <span class="sched-count-pill" style="color:var(--cyan);background:var(--cyan-light)">MORNING TRIP</span>
            ${!isCompatible ? `<span class="sched-dest-mismatch-pill" title="Destination does not match child's school (${escapeHtml(studentSchool)})">⚠️ To: ${escapeHtml(destSchoolLabel)}</span>` : ''}
          </div>

          <div class="sched-bus-badge">
            <span><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="15" rx="3"></rect><path d="M4 11h16"></path><circle cx="7.5" cy="18.5" r="1.5"></circle><circle cx="16.5" cy="18.5" r="1.5"></circle></svg></span>
            <span><b>${escapeHtml(b.name)}</b> (${escapeHtml(b.reg)}) &bull; <span class="sched-route-name">${escapeHtml(r.name)}</span></span>
          </div>

          <div class="sched-stop-block">
            <div class="sched-stop-label">Departure From Origin</div>
            <div class="sched-time-line">
              <span class="sched-green-dot"></span>
              <span class="sched-time-text">${escapeHtml(b.morningDep)}</span>
            </div>
            <div class="sched-stop-name">${escapeHtml(originTerminal)}</div>
          </div>

          <div class="sched-stop-block">
            <div class="sched-stop-label">Arrival At School Gate</div>
            <div class="sched-time-line">
              <span class="sched-green-dot"></span>
              <span class="sched-time-text">${escapeHtml(b.schoolArrival)}</span>
            </div>
            <div class="sched-stop-name">${escapeHtml(destSchoolLabel)}</div>
          </div>

          <div class="sched-driver-line">
            <span>Driver: <b>${escapeHtml(b.driver)}</b></span>
            <span>${escapeHtml(b.phone)}</span>
          </div>

          <div class="sched-days-line">
            OPERATING DAYS: MON &bull; TUE &bull; WED &bull; THU &bull; FRI
          </div>

          <div class="sched-card-bottom">
            ${isAssignedMorning ? `
              <span class="sched-child-assigned-tag">${SVG_ICONS.check} Assigned to Your Child</span>
            ` : `
              <span style="font-size:12px;color:#94a3b8">${isCompatible ? 'Available Schedule' : 'Different Destination'}</span>
            `}
            <button class="sched-assign-btn ${isAssignedMorning ? 'secondary' : ''}" onclick="assignBusToChild('morning', ${r.id}, ${b.id}, '${escapeHtml(r.name)}', '${escapeHtml(b.reg)}')">
              ${isAssignedMorning ? 'Re-confirm Morning' : 'Assign Morning Bus'}
            </button>
          </div>
        </article>
      `);

      // --- Return Card ---
      returnCards.push(`
        <article class="${cardClass}">
          <div class="sched-card-top">
            <span class="sched-count-pill">${escapeHtml(r.code)}</span>
            <span class="sched-count-pill" style="color:#2563eb;background:#eff6ff">RETURN TRIP</span>
            ${!isCompatible ? `<span class="sched-dest-mismatch-pill" title="Destination does not match child's school (${escapeHtml(studentSchool)})">⚠️ From: ${escapeHtml(destSchoolLabel)}</span>` : ''}
          </div>

          <div class="sched-bus-badge">
            <span><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="15" rx="3"></rect><path d="M4 11h16"></path><circle cx="7.5" cy="18.5" r="1.5"></circle><circle cx="16.5" cy="18.5" r="1.5"></circle></svg></span>
            <span><b>${escapeHtml(b.name)}</b> (${escapeHtml(b.reg)}) &bull; <span class="sched-route-name">${escapeHtml(r.name)}</span></span>
          </div>

          <div class="sched-stop-block">
            <div class="sched-stop-label">Departure From School</div>
            <div class="sched-time-line">
              <span class="sched-green-dot return"></span>
              <span class="sched-time-text">${escapeHtml(b.returnDep)}</span>
            </div>
            <div class="sched-stop-name">${escapeHtml(destSchoolLabel)}</div>
          </div>

          <div class="sched-stop-block">
            <div class="sched-stop-label">Arrival At Terminal / Drop-off</div>
            <div class="sched-time-line">
              <span class="sched-green-dot return"></span>
              <span class="sched-time-text">${escapeHtml(b.returnArr)}</span>
            </div>
            <div class="sched-stop-name">${escapeHtml(originTerminal)}</div>
          </div>

          <div class="sched-driver-line">
            <span>Driver: <b>${escapeHtml(b.driver)}</b></span>
            <span>${escapeHtml(b.phone)}</span>
          </div>

          <div class="sched-days-line">
            OPERATING DAYS: MON &bull; TUE &bull; WED &bull; THU &bull; FRI
          </div>

          <div class="sched-card-bottom">
            ${isAssignedReturn ? `
              <span class="sched-child-assigned-tag">${SVG_ICONS.check} Assigned to Your Child</span>
            ` : `
              <span style="font-size:12px;color:#94a3b8">${isCompatible ? 'Available Schedule' : 'Different Destination'}</span>
            `}
            <button class="sched-assign-btn ${isAssignedReturn ? 'secondary' : ''}" onclick="assignBusToChild('return', ${r.id}, ${b.id}, '${escapeHtml(r.name)}', '${escapeHtml(b.reg)}')">
              ${isAssignedReturn ? 'Re-confirm Return' : 'Assign Return Bus'}
            </button>
          </div>
        </article>
      `);
    });
  });

  main.innerHTML = `
    <header class="topbar">
      ${renderTopbarLeft("Transport schedule", "All morning departures to schools and return trips back home across Sri Lanka.")}
      ${renderTopControls(null, currentStudentId)}
    </header>

    <div class="schedules-container">
      <!-- Quick Filter Chips -->
      <nav class="sched-filter-bar" aria-label="Filter school routes">
        <button class="sched-filter-chip ${currentFilter === 'all' ? 'active' : ''}" onclick="setFilter('all')">
          All Schools (18 Buses)
        </button>
        <button class="sched-filter-chip ${currentFilter === 'nalanda' ? 'active' : ''}" onclick="setFilter('nalanda')">
          Nalanda College (Maradana)
        </button>
        <button class="sched-filter-chip ${currentFilter === 'sliit' ? 'active' : ''}" onclick="setFilter('sliit')">
          SLIIT Campus Malabe
        </button>
        <button class="sched-filter-chip ${currentFilter === 'ananda' ? 'active' : ''}" onclick="setFilter('ananda')">
          Ananda College
        </button>
        <button class="sched-filter-chip ${currentFilter === 'royal' ? 'active' : ''}" onclick="setFilter('royal')">
          Royal College
        </button>
        <button class="sched-filter-chip ${currentFilter === 'musaeus' ? 'active' : ''}" onclick="setFilter('musaeus')">
          Musaeus College
        </button>
        <button class="sched-filter-chip ${currentFilter === 'isipathana' ? 'active' : ''}" onclick="setFilter('isipathana')">
          Isipathana College
        </button>
      </nav>

      <!-- 2-Column Split: Left = Morning (To School), Right = Return (Back Home) -->
      <div class="sched-columns-split">
        <!-- LEFT COLUMN: MORNING TRIPS -->
        <section class="sched-column">
          <div class="sched-col-header">
            <div>
              <h2>Morning Trips &mdash; To School</h2>
              <p>Pickups from suburbs & arrival at school gate</p>
            </div>
            <span class="sched-count-pill">${morningCards.length} Runs</span>
          </div>

          ${morningCards.join("")}
        </section>

        <!-- RIGHT COLUMN: RETURN TRIPS -->
        <section class="sched-column">
          <div class="sched-col-header">
            <div>
              <h2>Return Trips &mdash; Back Home</h2>
              <p>Departures from school & drop-offs back home</p>
            </div>
            <span class="sched-count-pill">${returnCards.length} Runs</span>
          </div>

          ${returnCards.join("")}
        </section>
      </div>
    </div>
  `;

  syncTopbarChildSelector();
}

async function assignBusToChild(runType, routeId, busId, routeName, busReg) {
  const route = ALL_ROUTES_DATA.find(r => r.id === routeId);
  if (!route) return;

  const bus = route.buses.find(b => b.id === busId);
  if (!bus) return;

  const studentSchool = getStudentDestinationSchool();
  const studentName = currentStudentData?.name || currentStudentData?.fullName || currentStudentData?.studentName || "Your child";

  // Validate destination compatibility
  if (!isSchoolCompatible(studentSchool, route.schoolName, route.name)) {
    const routeLabel = getRouteDestLabelForStudent(route.schoolName, route.schoolName); // use full name for error
    showErrorToast(`Incorrect destination: This bus travels to ${route.schoolName}, but ${studentName}'s destination is ${studentSchool}. You cannot assign a bus heading to a different school.`);
    return;
  }

  if (runType === "morning") {
    activeStudentAssignment.routeId = routeId;
    activeStudentAssignment.routeName = routeName;
    activeStudentAssignment.morningBusId = busId;
  } else {
    activeStudentAssignment.routeId = routeId;
    activeStudentAssignment.routeName = routeName;
    activeStudentAssignment.returnBusId = busId;
  }

  // Persist locally
  localStorage.setItem(`nextgo_transport_assignment_${currentStudentId}`, JSON.stringify(activeStudentAssignment));

  // Sync with backend
  try {
    await Api.updateStudentStops(currentStudentId, {
      routeId: activeStudentAssignment.routeId,
      routeName: activeStudentAssignment.routeName,
      busId: activeStudentAssignment.morningBusId,
      returnBusId: activeStudentAssignment.returnBusId,
      pickupStop: activeStudentAssignment.pickupStop,
      dropOffStop: activeStudentAssignment.dropoffStop
    });
  } catch (err) {
    console.warn("Backend sync notice:", err);
  }

  renderAllSchedules();
  showToast(`Assigned ${runType === 'morning' ? 'Morning' : 'Return'} Bus ${busReg} (${bus.name}) to your child!`);
}

function showToast(msg) {
  const old = document.querySelector(".sched-toast");
  if (old) old.remove();

  const toast = document.createElement("div");
  toast.className = "sched-toast";
  toast.innerHTML = `<span>${SVG_ICONS.check}</span> <span>${escapeHtml(msg)}</span>`;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.style.transition = "opacity 0.4s ease, transform 0.4s ease";
    toast.style.opacity = "0";
    toast.style.transform = "translateY(10px)";
    setTimeout(() => toast.remove(), 400);
  }, 2800);
}

function showErrorToast(msg) {
  const old = document.querySelector(".sched-toast");
  if (old) old.remove();

  const toast = document.createElement("div");
  toast.className = "sched-toast error";
  toast.innerHTML = `
    <div class="toast-error-icon">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
    </div>
    <div class="toast-msg-body">
      <strong>Incorrect Destination</strong>
      <p>${escapeHtml(msg)}</p>
    </div>
  `;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.style.transition = "opacity 0.4s ease, transform 0.4s ease";
    toast.style.opacity = "0";
    toast.style.transform = "translateY(10px)";
    setTimeout(() => toast.remove(), 400);
  }, 4500);
}

