/* =============================================================================
   NEXTGO Live Bus Tracking - 50/50 Split Layout Engine
   Left: Route, Multi-Bus, and Stop Assignment Controls & Live Telemetry
   Right: Interactive Full-Height Leaflet Map with Clickable Stops, Speed Controls & Live Animation
   ============================================================================= */

let currentStudentId = null;
let activeRoute = null;
let activeMorningBus = null;
let activeReturnBus = null;
let activePickupStop = null;
let activeDropoffStop = null;
let assignedSchool = "";

let map = null;
let routePolyline = null;
let stopMarkersGroup = null;
let busMarker = null;
let tileLayer = null;

const stopMarkersMap = new Map();

(async () => {
  pageShell("Live bus tracking", "Real-time vehicle telemetry & route tracking.", skeletons(1));
  currentStudentId = selectedStudent() || "1";
  syncTopbarChildSelector();

  try {
    const [student, transport, dbRoutes, dbBuses] = await Promise.all([
      Api.getStudent(currentStudentId).catch(() => ({})),
      Api.getStudentTransport(currentStudentId).catch(() => ({})),
      Api.getRoutes().catch(() => []),
      Api.getBuses().catch(() => [])
    ]);
    syncDatabaseDataWithRoutes(dbRoutes, dbBuses);
    initAssignmentData(student, transport);
  } catch {
    initAssignmentData({}, {});
  }

  if (typeof NextGoSimulation !== "undefined") {
    NextGoSimulation.init(currentStudentId, activeRoute, activeMorningBus, activeReturnBus, activePickupStop, activeDropoffStop);
  }

  try {
    renderTrackingLayout();
  } catch (renderErr) {
    console.error("renderTrackingLayout failed:", renderErr);
    const main = document.querySelector(".app-main");
    if (main) main.innerHTML = `<header class="topbar">${renderTopbarLeft("Live bus tracking", "Could not render tracking view. Please reload the page.")}</header><div style="padding:40px;text-align:center;"><p style="color:var(--muted);">Error: ${escapeHtml(renderErr.message)}</p></div>`;
    return;
  }
  syncSimulationControlButtons();
  initLeafletMap();
  startBusAnimation();
})();

function syncDatabaseDataWithRoutes(dbRoutes, dbBuses) {
  if (Array.isArray(dbRoutes) && dbRoutes.length > 0) {
    dbRoutes.forEach(dbr => {
      const match = ALL_ROUTES_DATA.find(r => r.id === dbr.id);
      if (match) {
        if (dbr.routeName) match.name = dbr.routeName;
        if (dbr.originCity) match.originCity = dbr.originCity;
        if (dbr.schoolName) match.schoolName = dbr.schoolName;
      }
    });
  }
  if (Array.isArray(dbBuses) && dbBuses.length > 0) {
    ALL_ROUTES_DATA.forEach(r => {
      const matchingBuses = dbBuses.filter(b => b.routeId === r.id);
      if (matchingBuses.length > 0) {
        r.buses = matchingBuses.map((b, idx) => {
          const existingBus = (r.buses || []).find(eb => eb.id === b.id || eb.reg === b.registrationNumber || eb.name === b.busName);
          const defaultMorning = idx === 0 ? "05:45 AM" : idx === 1 ? "06:15 AM" : "06:45 AM";
          const defaultReturn = idx === 0 ? "01:30 PM" : idx === 1 ? "02:00 PM" : "02:30 PM";
          return {
            id: b.id,
            name: b.busName,
            reg: b.registrationNumber,
            driver: b.driverName,
            phone: b.driverPhone,
            capacity: b.capacity || 40,
            status: b.currentStatus || "ON ROUTE",
            morningDep: existingBus?.morningDep || defaultMorning,
            returnDep: existingBus?.returnDep || defaultReturn
          };
        });
      }
    });
  }
}

function getStorageKey() {
  return `nextgo_transport_assignment_${currentStudentId}`;
}

function getStudentAssignedSchool(student, transport) {
  const customSaved = localStorage.getItem(`nextgo_student_school_${currentStudentId}`);
  if (customSaved) return customSaved;
  const saved = localStorage.getItem(getStorageKey());
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      if (parsed?.destinationSchool) return parsed.destinationSchool;
    } catch {}
  }
  if (transport?.schedule?.destinationSchool) return transport.schedule.destinationSchool;
  if (student?.schoolName) return student.schoolName;
  return "SLIIT Campus Malabe";
}

function getRoutesForSchool(schoolName) {
  if (typeof ALL_ROUTES_DATA === "undefined" || !ALL_ROUTES_DATA.length) return [];
  const sLow = (schoolName || "").toLowerCase();
  let matches = [];
  if (sLow.includes("sliit")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 1 || (r.schoolName && r.schoolName.toLowerCase().includes("sliit")));
  } else if (sLow.includes("nalanda")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 4 || r.id === 2 || (r.schoolName && r.schoolName.toLowerCase().includes("nalanda")) || (r.name && r.name.toLowerCase().includes("nalanda")));
  } else if (sLow.includes("ananda")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 2 || r.id === 4 || (r.schoolName && r.schoolName.toLowerCase().includes("ananda")) || (r.name && r.name.toLowerCase().includes("ananda")));
  } else if (sLow.includes("musaeus")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 3 || (r.schoolName && r.schoolName.toLowerCase().includes("musaeus")));
  } else if (sLow.includes("royal")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 5 || (r.schoolName && r.schoolName.toLowerCase().includes("royal")));
  } else if (sLow.includes("isipathana")) {
    matches = ALL_ROUTES_DATA.filter(r => r.id === 6 || (r.schoolName && r.schoolName.toLowerCase().includes("isipathana")));
  } else {
    matches = ALL_ROUTES_DATA.filter(r => (r.schoolName && r.schoolName.toLowerCase().includes(sLow)) || (sLow && r.schoolName && sLow.includes(r.schoolName.toLowerCase())));
  }
  return matches.length > 0 ? matches : [ALL_ROUTES_DATA[0]];
}

function initAssignmentData(student, transport) {
  const saved = localStorage.getItem(getStorageKey());
  let parsed = null;
  if (saved) {
    try { parsed = JSON.parse(saved); } catch {}
  }

  assignedSchool = getStudentAssignedSchool(student, transport);
  const matchingRoutes = getRoutesForSchool(assignedSchool);

  // Determine route: must match the assigned school
  let targetRoute = null;
  if (parsed?.routeId) {
    targetRoute = matchingRoutes.find(r => r.id === parsed.routeId);
  }
  if (!targetRoute && transport?.schedule?.routeId) {
    targetRoute = matchingRoutes.find(r => r.id === transport.schedule.routeId);
  }
  if (!targetRoute && parsed?.originCity) {
    targetRoute = matchingRoutes.find(r => (r.originCity || "").toLowerCase() === parsed.originCity.toLowerCase());
  }
  if (!targetRoute) {
    targetRoute = matchingRoutes[0] || ALL_ROUTES_DATA[0];
  }

  activeRoute = targetRoute;

  // Determine buses
  const buses = activeRoute.buses || [];
  const targetMorningBusId = parsed?.morningBusId || transport?.schedule?.morningBusId || transport?.schedule?.busId;
  const targetReturnBusId = parsed?.returnBusId || transport?.schedule?.returnBusId;
  activeMorningBus = buses.find(b => b.id === targetMorningBusId) || buses[0];
  activeReturnBus = buses.find(b => b.id === targetReturnBusId) || buses[1] || buses[0];

  // Determine stops
  const stops = activeRoute.stops || [];
  const fallbackStop = stops.length > 0 ? stops[Math.min(3, stops.length - 1)].name : "Terminal";
  const defaultPickup = parsed?.pickupStop || transport?.pickupStopName || transport?.schedule?.pickupStop || fallbackStop;
  const defaultDropoff = parsed?.dropoffStop || transport?.dropOffStopName || transport?.schedule?.dropOffStop || fallbackStop;

  activePickupStop = stops.find(s => s.name === defaultPickup)?.name || (stops[0]?.name ?? "Terminal");
  activeDropoffStop = stops.find(s => s.name === defaultDropoff)?.name || (stops[0]?.name ?? "Terminal");
}

function saveAssignmentState() {
  try {
    const state = {
      routeId: activeRoute.id,
      routeName: activeRoute.name,
      originCity: activeRoute.originCity || "Suburbs",
      destinationSchool: assignedSchool || activeRoute.schoolName,
      morningBusId: activeMorningBus?.id,
      morningBusNumber: activeMorningBus?.reg,
      morningBusName: activeMorningBus?.name,
      morningDriver: activeMorningBus?.driver,
      morningPhone: activeMorningBus?.phone,
      returnBusId: activeReturnBus?.id,
      returnBusNumber: activeReturnBus?.reg,
      returnBusName: activeReturnBus?.name,
      returnDriver: activeReturnBus?.driver,
      returnPhone: activeReturnBus?.phone,
      pickupStop: activePickupStop,
      dropoffStop: activeDropoffStop
    };
    localStorage.setItem(getStorageKey(), JSON.stringify(state));
    if (assignedSchool) {
      localStorage.setItem(`nextgo_student_school_${currentStudentId}`, assignedSchool);
    }
  } catch {}
}


function renderTrackingLayout() {
  const main = document.querySelector(".app-main");

  main.innerHTML = `
    <header class="topbar">
      ${renderTopbarLeft("Live bus tracking", `Real-time vehicle telemetry for ${activeRoute.name}`)}
      ${renderTopControls(null, currentStudentId)}
    </header>

    <div class="tracking-split-container">
      <!-- LEFT COLUMN: Assignment Controls & Telemetry -->
      <aside class="tracking-left-panel">
        <!-- 1. Route Selector (From & To) -->
        <div class="assignment-card glass">
          <div class="card-section-title">
            <span class="step-badge">1</span>
            <div>
              <h3>Bus Route (From &rarr; To)</h3>
              <p>Select origin suburban area and destination school</p>
            </div>
          </div>
          <div class="field-grid-2">
            <div class="field-wrap">
              <label for="routeOriginSelect">From (Origin)</label>
              <select id="routeOriginSelect" class="styled-select" onchange="handleOriginChange(this.value)">
                ${renderOriginOptionsHtml()}
              </select>
            </div>

            <div class="field-wrap">
              <label for="routeDestSelect">
                To (Destination School)
                <span class="dest-lock-tag" title="Assigned on My Student page">Assigned School</span>
              </label>
              <select id="routeDestSelect" class="styled-select readonly-dest-select" disabled title="Assigned school for this student. Change on My Student page.">
                <option value="${escapeHtml(assignedSchool || activeRoute.schoolName)}" selected>
                  ${escapeHtml(assignedSchool || activeRoute.schoolName)} (${activeRoute.stops.length} stops)
                </option>
              </select>
            </div>
          </div>

        </div>

        <!-- 2. Bus Schedule Picker (Morning & Return) -->
        <div class="assignment-card glass">
          <div class="card-section-title">
            <span class="step-badge">2</span>
            <div>
              <h3>Bus Timetable & Driver</h3>
              <p>Select morning pickup and return departure buses</p>
            </div>
          </div>
          <div class="field-grid-2">
            <div class="field-wrap">
              <label for="morningBusSelect">Morning Bus</label>
              <select id="morningBusSelect" class="styled-select" onchange="handleBusChange('morning', Number(this.value))">
                ${(activeRoute.buses || []).map(b => `
                  <option value="${b.id}" ${b.id === activeMorningBus?.id ? 'selected' : ''}>
                    ${b.name} (${b.morningDep} - ${b.driver})
                  </option>
                `).join("")}
              </select>
            </div>

            <div class="field-wrap">
              <label for="returnBusSelect">Return Bus</label>
              <select id="returnBusSelect" class="styled-select" onchange="handleBusChange('return', Number(this.value))">
                ${(activeRoute.buses || []).map(b => `
                  <option value="${b.id}" ${b.id === activeReturnBus?.id ? 'selected' : ''}>
                    ${b.name} (${b.returnDep} - ${b.driver})
                  </option>
                `).join("")}
              </select>
            </div>
          </div>
        </div>

        <!-- 3. Stops Picker & Save Action -->
        <div class="assignment-card glass">
          <div class="card-section-title">
            <span class="step-badge">3</span>
            <div>
              <h3>Assigned Stops</h3>
              <p>Choose from list or click any numbered stop on the map</p>
            </div>
          </div>
          <div class="field-grid-2">
            <div class="field-wrap">
              <label for="pickupStopSelect">Morning Pickup Stop</label>
              <select id="pickupStopSelect" class="styled-select" onchange="handleStopSelect('pickup', this.value)">
                ${(activeRoute.stops || []).map(s => `
                  <option value="${escapeHtml(s.name)}" ${s.name === activePickupStop ? 'selected' : ''}>
                    #${s.seq}. ${escapeHtml(s.name)} (${s.pickupTime})
                  </option>
                `).join("")}
              </select>
            </div>

            <div class="field-wrap">
              <label for="dropoffStopSelect">Return Drop-off Stop</label>
              <select id="dropoffStopSelect" class="styled-select" onchange="handleStopSelect('dropoff', this.value)">
                ${(activeRoute.stops || []).map(s => `
                  <option value="${escapeHtml(s.name)}" ${s.name === activeDropoffStop ? 'selected' : ''}>
                    #${s.seq}. ${escapeHtml(s.name)} (${s.dropOffTime})
                  </option>
                `).join("")}
              </select>
            </div>
          </div>

          <div class="assignment-actions-row">
            <button class="save-assignment-btn" id="saveAssignmentBtn" onclick="saveTransportAssignment()">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path><polyline points="17 21 17 13 7 13 7 21"></polyline><polyline points="7 3 7 8 15 8"></polyline></svg>
              <span>Save Transport Assignment</span>
            </button>
            <button class="delete-assignment-btn" id="deleteAssignmentBtn" onclick="deleteTransportAssignment()">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
              <span>Delete Transport Assignment</span>
            </button>
          </div>
        </div>

        <!-- 4. Live Telemetry Card -->
        <div class="telemetry-card glass">
          <div class="telemetry-top">
            <div class="bus-plate-badge">
              <span class="bus-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="15" rx="3"></rect>
                  <path d="M4 11h16"></path>
                  <circle cx="7.5" cy="18.5" r="1.5"></circle>
                  <circle cx="16.5" cy="18.5" r="1.5"></circle>
                </svg>
              </span>
              <div>
                <b id="telemetryBusName">${escapeHtml(activeMorningBus?.name || 'School Bus')}</b>
                <span id="telemetryBusPlate">${escapeHtml(activeMorningBus?.reg || 'NB-1234')}</span>
              </div>
            </div>
            <span class="live-pulse-badge">
              <span class="radar-dot"></span>
              <span id="liveStatusText">${escapeHtml(activeMorningBus?.status || 'ON ROUTE')}</span>
            </span>
          </div>

          <div class="telemetry-stats-grid">
            <div class="stat-pill">
              <span>SPEED</span>
              <b id="liveSpeed">${(typeof NextGoSimulation !== "undefined" ? NextGoSimulation.getCurrentTelemetry()?.speed : null) ?? 0} <small>km/h</small></b>
            </div>
            <div class="stat-pill">
              <span>NEXT STOP</span>
              <b id="liveNextStop">${escapeHtml(calculateNextStop())}</b>
            </div>
            <div class="stat-pill highlight">
              <span>ETA TO YOUR STOP</span>
              <b id="liveETA">${calculateEta()} <small>min</small></b>
            </div>
            <div class="stat-pill">
              <span>DESTINATION</span>
              <b id="liveDestination">${escapeHtml(activeRoute.schoolName)}</b>
            </div>
          </div>

          <div class="driver-info-strip">
            <div class="driver-avatar">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </div>
            <div class="driver-details">
              <span class="driver-role">ASSIGNED DRIVER</span>
              <b id="driverName">${escapeHtml(activeMorningBus?.driver || 'Kasun Fernando')}</b>
              <span class="driver-phone" id="driverPhone">${escapeHtml(activeMorningBus?.phone || '0779876543')}</span>
            </div>
            <a class="button secondary small" id="driverCallBtn" href="tel:${escapeHtml(activeMorningBus?.phone || '0779876543')}">Call</a>
          </div>
        </div>
      </aside>

      <!-- RIGHT COLUMN: Full Interactive Map -->
      <main class="tracking-right-panel">
        <div class="full-map-wrapper">
          <div id="map"></div>

          <!-- Top Floating Chip & Controls -->
          <div class="map-floating-overlay-top">
            <div class="active-route-chip">
              <span id="mapRouteTitle">${escapeHtml(activeRoute.name)}</span>
              <span class="badge-stops" id="mapStopsCount">${activeRoute.stops.length} Stops</span>
            </div>

            <div class="map-controls">
              <button class="map-btn" id="btnMapStreet" onclick="setMapTileStyle('street')">Street</button>
              <button class="map-btn active" id="btnMapSat" onclick="setMapTileStyle('satellite')">Satellite</button>
              <button class="map-btn" id="btnFitRoute" onclick="fitMapToCurrentRoute()" title="Center route">Fit Route</button>
            </div>
          </div>

          <!-- Simulation Playback & Speed Bar (Bottom Right) -->
          <div class="map-speed-controls-bottom-right">
            <div class="sim-playback-group">
              <button class="sim-ctrl-btn" id="btnSimPlayPause" onclick="toggleSimulationPause()" title="Pause or Resume Bus Simulation">
                <span id="simPlayPauseIcon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>
                </span>
                <span id="simPlayPauseText">Pause</span>
              </button>
              <button class="sim-ctrl-btn" id="btnSimRestart" onclick="restartSimulation()" title="Restart Simulation From Start">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path><path d="M3 3v5h5"></path></svg>
                <span>Restart</span>
              </button>
            </div>
            <div class="sim-ctrl-sep"></div>
            <span class="speed-label">SPEED:</span>
            <button class="speed-btn active" data-speed="1" onclick="setSimulationSpeed(1)">1x</button>
            <button class="speed-btn" data-speed="2" onclick="setSimulationSpeed(2)">2x</button>
            <button class="speed-btn" data-speed="4" onclick="setSimulationSpeed(4)">4x</button>
            <button class="speed-btn" data-speed="8" onclick="setSimulationSpeed(8)">8x</button>
            <button class="speed-btn" data-speed="10" onclick="setSimulationSpeed(10)">10x</button>
          </div>

          <!-- Bottom Legend -->
          <div class="map-legend-bottom">
            <span class="legend-item"><span class="legend-dot bus"></span> Live Bus</span>
            <span class="legend-item"><span class="legend-dot pickup"></span> Assigned Pickup</span>
            <span class="legend-item"><span class="legend-dot stop"></span> Route Stop (Click to set)</span>
            <span class="legend-item"><span class="legend-dot school"></span> School Destination</span>
          </div>
        </div>
      </main>
    </div>
  `;

  syncTopbarChildSelector();
}

function initLeafletMap() {
  const mapEl = document.getElementById("map");
  if (!mapEl) return;

  const waypoints = activeRoute.waypoints || [];
  const startPt = waypoints[0] || [6.8402, 79.9654];

  map = L.map("map", {
    center: startPt,
    zoom: 12,
    zoomControl: true
  });

  setMapTileStyle("satellite");
  drawRouteGeometry();
  setTimeout(() => {
    if (map) {
      map.invalidateSize();
      fitMapToCurrentRoute();
    }
  }, 250);
}

function setMapTileStyle(style) {
  if (!map) return;
  if (tileLayer) map.removeLayer(tileLayer);

  const btnStreet = document.getElementById("btnMapStreet");
  const btnSat = document.getElementById("btnMapSat");

  if (style === "satellite") {
    tileLayer = L.tileLayer("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", {
      maxZoom: 19,
      attribution: "Tiles &copy; Esri"
    }).addTo(map);
    btnStreet?.classList.remove("active");
    btnSat?.classList.add("active");
  } else {
    tileLayer = L.tileLayer("https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png", {
      maxZoom: 19,
      attribution: "&copy; OpenStreetMap &copy; CARTO"
    }).addTo(map);
    btnStreet?.classList.add("active");
    btnSat?.classList.remove("active");
  }
}

function buildStopPopupHtml(st, isPickup, isDropoff, isSchool) {
  let pickupAction = '';
  if (isPickup) {
    pickupAction = `
      <div class="popup-status-chip pickup">Active Morning Pickup</div>
      <button class="popup-set-btn unassign" onclick="handleCancelStop('pickup')">
        Remove Pickup Stop
      </button>
    `;
  } else {
    pickupAction = `
      <button class="popup-set-btn pickup" onclick="handleMapStopClick('pickup', '${escapeHtml(st.name)}')">
        Set as Morning Pickup Stop
      </button>
    `;
  }

  let dropoffAction = '';
  if (isDropoff) {
    dropoffAction = `
      <div class="popup-status-chip dropoff">Active Return Drop-off</div>
      <button class="popup-set-btn unassign" onclick="handleCancelStop('dropoff')">
        Remove Drop-off Stop
      </button>
    `;
  } else {
    dropoffAction = `
      <button class="popup-set-btn dropoff" onclick="handleMapStopClick('dropoff', '${escapeHtml(st.name)}')">
        Set as Return Drop-off Stop
      </button>
    `;
  }

  return `
    <div class="stop-popup-content">
      <div class="stop-popup-title">Stop #${st.seq}: ${escapeHtml(st.name)}</div>
      <div class="stop-popup-meta">
        Morning Arrival: <b>${st.pickupTime}</b><br>
        Return Drop-off: <b>${st.dropOffTime}</b>
      </div>
      <div class="stop-popup-actions">
        ${pickupAction}
        ${dropoffAction}
      </div>
    </div>
  `;
}

function updateStopMarkersUI() {
  if (!map || !activeRoute || !stopMarkersGroup) return;
  const stops = activeRoute.stops || [];

  stops.forEach((st, idx) => {
    const isPickup = st.name === activePickupStop;
    const isDropoff = st.name === activeDropoffStop;
    const isSchool = idx === stops.length - 1;

    let markerClass = "custom-stop-marker";
    if (isPickup) markerClass += " is-pickup";
    else if (isDropoff) markerClass += " is-dropoff";
    else if (isSchool) markerClass += " is-school";

    const stopIcon = L.divIcon({
      className: "",
      html: `<div class="${markerClass}" title="${escapeHtml(st.name)}">${st.seq}</div>`,
      iconSize: [28, 28],
      iconAnchor: [14, 14]
    });

    const marker = stopMarkersMap.get(st.name);
    if (marker) {
      marker.setIcon(stopIcon);
      marker.setPopupContent(buildStopPopupHtml(st, isPickup, isDropoff, isSchool));
    }
  });
}

function drawRouteGeometry(preserveBus = false) {
  if (!map || !activeRoute) return;

  // Clear previous polyline and stop layers
  if (routePolyline) map.removeLayer(routePolyline);
  if (stopMarkersGroup) map.removeLayer(stopMarkersGroup);
  stopMarkersMap.clear();

  const waypoints = activeRoute.waypoints || [];
  const stops = activeRoute.stops || [];

  // 1. Draw Glowing Route Polyline
  routePolyline = L.polyline(waypoints, {
    color: "#28c7fa",
    weight: 5,
    opacity: 0.9,
    lineJoin: "round"
  }).addTo(map);

  // 2. Add Stop Markers
  stopMarkersGroup = L.layerGroup().addTo(map);

  stops.forEach((st, idx) => {
    const isPickup = st.name === activePickupStop;
    const isDropoff = st.name === activeDropoffStop;
    const isSchool = idx === stops.length - 1;

    let markerClass = "custom-stop-marker";
    if (isPickup) markerClass += " is-pickup";
    else if (isDropoff) markerClass += " is-dropoff";
    else if (isSchool) markerClass += " is-school";

    const stopIcon = L.divIcon({
      className: "",
      html: `<div class="${markerClass}" title="${escapeHtml(st.name)}">${st.seq}</div>`,
      iconSize: [28, 28],
      iconAnchor: [14, 14]
    });

    const marker = L.marker([st.lat, st.lon], { icon: stopIcon, zIndexOffset: 500 }).addTo(stopMarkersGroup);
    marker.bindPopup(buildStopPopupHtml(st, isPickup, isDropoff, isSchool));
    stopMarkersMap.set(st.name, marker);
  });

  // 3. Add or preserve Live Bus Marker
  if (!preserveBus || !busMarker) {
    if (busMarker) map.removeLayer(busMarker);
    const initialPos = (typeof NextGoSimulation !== "undefined") ? NextGoSimulation.getLiveCoord() : (waypoints[0] || [6.8402, 79.9654]);
    const initialAngle = (typeof NextGoSimulation !== "undefined") ? NextGoSimulation.getHeadingAngle() : 0;

    const busIcon = L.divIcon({
      className: "",
      html: `
        <div class="custom-bus-marker">
          <div class="bus-radar-halo"></div>
          <div class="bus-marker-body" id="busBodyIcon" style="transform: rotate(${initialAngle}deg)">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="15" rx="3"></rect>
              <path d="M4 11h16"></path>
              <circle cx="7.5" cy="18.5" r="1.5"></circle>
              <circle cx="16.5" cy="18.5" r="1.5"></circle>
            </svg>
          </div>
        </div>
      `,
      iconSize: [44, 44],
      iconAnchor: [22, 22]
    });

    busMarker = L.marker(initialPos, { icon: busIcon, zIndexOffset: 2000 }).addTo(map);
    updateTripDirectionUI();
    fitMapToCurrentRoute();
  }
}

function fitMapToCurrentRoute() {
  if (!map || !routePolyline) return;
  const bounds = routePolyline.getBounds();
  if (bounds.isValid()) {
    map.fitBounds(bounds, { padding: [40, 40], maxZoom: 14 });
  }
}

/* -----------------------------------------------------------------------------
   Simulation Playback & Control (Play, Pause, Restart, Speed)
   ----------------------------------------------------------------------------- */
function syncSimulationControlButtons() {
  if (typeof NextGoSimulation === "undefined") return;
  const simState = NextGoSimulation.getState();
  const textSpan = document.getElementById("simPlayPauseText");
  const iconSpan = document.getElementById("simPlayPauseIcon");
  const btn = document.getElementById("btnSimPlayPause");

  if (simState.isPaused) {
    if (textSpan) textSpan.textContent = "Play";
    if (iconSpan) iconSpan.innerHTML = `<svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>`;
    if (btn) btn.classList.add("is-paused");
  } else {
    if (textSpan) textSpan.textContent = "Pause";
    if (iconSpan) iconSpan.innerHTML = `<svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>`;
    if (btn) btn.classList.remove("is-paused");
  }

  document.querySelectorAll(".speed-btn").forEach(sBtn => {
    sBtn.classList.toggle("active", Number(sBtn.dataset.speed) === Number(simState.speedMultiplier));
  });
}

function toggleSimulationPause() {
  if (typeof NextGoSimulation === "undefined") return;
  const isPaused = NextGoSimulation.togglePause();
  syncSimulationControlButtons();
  if (isPaused) {
    showToast("Simulation Paused");
  } else {
    showToast("Simulation Resumed");
  }
}

function restartSimulation() {
  if (typeof NextGoSimulation === "undefined") return;
  NextGoSimulation.restart();

  const waypoints = activeRoute.waypoints || [];
  const initialPos = waypoints[0] || [6.8402, 79.9654];
  if (busMarker) {
    busMarker.setLatLng(initialPos);
    const busBody = document.getElementById("busBodyIcon");
    if (busBody) busBody.style.transform = "rotate(0deg)";
  }

  syncSimulationControlButtons();
  updateTripDirectionUI();
  showToast("Simulation restarted from origin");
}

function setSimulationSpeed(multiplier) {
  if (typeof NextGoSimulation === "undefined") return;
  NextGoSimulation.setSpeed(multiplier);
  syncSimulationControlButtons();
}

function startBusAnimation() {
  if (typeof NextGoSimulation === "undefined") return;

  NextGoSimulation.startLiveLoop((simState, telem) => {
    if (!map || !busMarker) return;

    busMarker.setLatLng(telem.coord);
    const busBody = document.getElementById("busBodyIcon");
    if (busBody) busBody.style.transform = `rotate(${telem.headingAngle}deg)`;

    const speedEl = document.getElementById("liveSpeed");
    if (speedEl) speedEl.innerHTML = `${telem.speed} <small>km/h</small>`;

    const etaEl = document.getElementById("liveETA");
    if (etaEl) etaEl.innerHTML = `${telem.eta} <small>min</small>`;

    const nextStopEl = document.getElementById("liveNextStop");
    if (nextStopEl) nextStopEl.textContent = telem.nextStop;

    const liveStatus = document.getElementById("liveStatusText");
    if (liveStatus) liveStatus.textContent = telem.statusText;

    const liveDest = document.getElementById("liveDestination");
    if (liveDest) liveDest.textContent = telem.destination;

    const busName = document.getElementById("telemetryBusName");
    if (busName) busName.textContent = telem.busName;

    const busPlate = document.getElementById("telemetryBusPlate");
    if (busPlate) busPlate.textContent = telem.busPlate;

    const driverName = document.getElementById("driverName");
    if (driverName) driverName.textContent = telem.driverName;

    const driverPhone = document.getElementById("driverPhone");
    if (driverPhone) driverPhone.textContent = telem.driverPhone;

    const driverCallBtn = document.getElementById("driverCallBtn");
    if (driverCallBtn) driverCallBtn.setAttribute("href", `tel:${telem.driverPhone}`);
  });
}

function updateTripDirectionUI() {
  const telem = (typeof NextGoSimulation !== "undefined") ? NextGoSimulation.getCurrentTelemetry() : null;
  const isMorning = telem ? (telem.direction === "outbound") : true;
  const bus = isMorning ? activeMorningBus : activeReturnBus;

  const busName = document.getElementById("telemetryBusName");
  if (busName) busName.textContent = telem?.busName || bus?.name || (isMorning ? "Morning Bus" : "Return Bus");

  const busPlate = document.getElementById("telemetryBusPlate");
  if (busPlate) busPlate.textContent = telem?.busPlate || bus?.reg || "NB-1234";

  const liveStatus = document.getElementById("liveStatusText");
  if (liveStatus) {
    liveStatus.textContent = telem?.statusText || (isMorning ? "MORNING RUN" : "RETURN RUN");
  }

  const liveDest = document.getElementById("liveDestination");
  if (liveDest) {
    liveDest.textContent = telem?.destination || (isMorning ? activeRoute.schoolName : (activeRoute.startLocation || activeRoute.originCity + " Terminal"));
  }

  const driverName = document.getElementById("driverName");
  if (driverName) driverName.textContent = telem?.driverName || bus?.driver || "Kasun Fernando";

  const driverPhone = document.getElementById("driverPhone");
  if (driverPhone) driverPhone.textContent = telem?.driverPhone || bus?.phone || "0779876543";

  const driverCallBtn = document.getElementById("driverCallBtn");
  if (driverCallBtn) driverCallBtn.setAttribute("href", `tel:${telem?.driverPhone || bus?.phone || '0779876543'}`);
}

function calculateEta() {
  if (typeof NextGoSimulation !== "undefined") return NextGoSimulation.calculateEta();
  return 6;
}

function calculateNextStop() {
  if (typeof NextGoSimulation !== "undefined") return NextGoSimulation.calculateNextStop();
  return "Approaching Stop";
}

/* Event Handlers */
function handleRouteChange(newRouteId) {
  const found = ALL_ROUTES_DATA.find(r => r.id === newRouteId);
  if (!found) return;

  activeRoute = found;
  activeMorningBus = found.buses[0];
  activeReturnBus = found.buses[1] || found.buses[0];
  activePickupStop = found.stops[Math.min(3, found.stops.length - 1)].name;
  activeDropoffStop = found.stops[Math.min(3, found.stops.length - 1)].name;

  if (typeof NextGoSimulation !== "undefined") {
    NextGoSimulation.setRoute(found.id, activeMorningBus, activeReturnBus, activePickupStop, activeDropoffStop);
  }

  saveAssignmentState();

  updateTrackingControls();
  drawRouteGeometry();
  fitMapToCurrentRoute();
  if (map) {
    map.invalidateSize();
  }
  showToast(`Switched route to ${found.name}`);
}

function getUniqueOrigins() {
  const list = [];
  ALL_ROUTES_DATA.forEach(r => {
    const o = r.originCity || "Suburbs";
    if (!list.includes(o)) list.push(o);
  });
  return list;
}

function renderOriginOptionsHtml() {
  const allOrigins = getUniqueOrigins();
  const matchingRoutes = getRoutesForSchool(assignedSchool);
  const validOrigins = [...new Set(matchingRoutes.map(r => r.originCity))];

  return allOrigins.map(orig => {
    const isValid = validOrigins.includes(orig);
    const isSelected = orig === activeRoute.originCity;
    const disabledAttr = !isValid ? 'disabled' : '';
    const labelExtra = !isValid ? ` (No route to ${escapeHtml(assignedSchool || 'assigned school')})` : '';
    return `
      <option value="${escapeHtml(orig)}" ${isSelected ? 'selected' : ''} ${disabledAttr}>
        ${escapeHtml(orig)}${labelExtra}
      </option>
    `;
  }).join("");
}

function getDestinationsForOrigin(originCity) {
  return ALL_ROUTES_DATA.filter(r => (r.originCity || "Suburbs") === originCity);
}

function handleOriginChange(selectedOrigin) {
  const matchingRoutes = getRoutesForSchool(assignedSchool);
  const routeForOrigin = matchingRoutes.find(r => (r.originCity || "").toLowerCase() === selectedOrigin.toLowerCase());
  if (routeForOrigin) {
    handleRouteChange(routeForOrigin.id);
  } else {
    showToast(`No direct route from ${selectedOrigin} to ${assignedSchool}`);
    const origSelect = document.getElementById("routeOriginSelect");
    if (origSelect) origSelect.value = activeRoute.originCity;
  }
}

function handleDestChange(selectedSchool) {
  showToast(`Destination school is locked to ${assignedSchool}. Change it on My Student page.`);
}

function updateTrackingControls() {
  // Update Origin select
  const origSelect = document.getElementById("routeOriginSelect");
  if (origSelect) {
    origSelect.innerHTML = renderOriginOptionsHtml();
    origSelect.value = activeRoute.originCity;
  }

  // Update Dest select: Strictly display ONLY the assigned school
  const destSelect = document.getElementById("routeDestSelect");
  if (destSelect) {
    destSelect.innerHTML = `
      <option value="${escapeHtml(assignedSchool || activeRoute.schoolName)}" selected>
        ${escapeHtml(assignedSchool || activeRoute.schoolName)} (${activeRoute.stops.length} stops)
      </option>
    `;
    destSelect.disabled = true;
  }

  // Update Bus selects
  const morningBusSelect = document.getElementById("morningBusSelect");
  if (morningBusSelect) {
    morningBusSelect.innerHTML = (activeRoute.buses || []).map(b => `
      <option value="${b.id}" ${b.id === activeMorningBus?.id ? 'selected' : ''}>
        ${b.name} (${b.morningDep} - ${b.driver})
      </option>
    `).join("");
  }

  const returnBusSelect = document.getElementById("returnBusSelect");
  if (returnBusSelect) {
    returnBusSelect.innerHTML = (activeRoute.buses || []).map(b => `
      <option value="${b.id}" ${b.id === activeReturnBus?.id ? 'selected' : ''}>
        ${b.name} (${b.returnDep} - ${b.driver})
      </option>
    `).join("");
  }

  // Update Stop selects
  const pickupSelect = document.getElementById("pickupStopSelect");
  if (pickupSelect) {
    pickupSelect.innerHTML = (activeRoute.stops || []).map(s => `
      <option value="${escapeHtml(s.name)}" ${s.name === activePickupStop ? 'selected' : ''}>
        #${s.seq}. ${escapeHtml(s.name)} (${s.pickupTime})
      </option>
    `).join("");
  }

  const dropoffSelect = document.getElementById("dropoffStopSelect");
  if (dropoffSelect) {
    dropoffSelect.innerHTML = (activeRoute.stops || []).map(s => `
      <option value="${escapeHtml(s.name)}" ${s.name === activeDropoffStop ? 'selected' : ''}>
        #${s.seq}. ${escapeHtml(s.name)} (${s.dropOffTime})
      </option>
    `).join("");
  }

  // Update Telemetry panel
  const mapTitle = document.getElementById("mapRouteTitle");
  if (mapTitle) mapTitle.textContent = activeRoute.name;

  const mapStopsCount = document.getElementById("mapStopsCount");
  if (mapStopsCount) mapStopsCount.textContent = `${activeRoute.stops.length} Stops`;

  const busName = document.getElementById("telemetryBusName");
  if (busName) busName.textContent = activeMorningBus?.name || 'School Bus';

  const busPlate = document.getElementById("telemetryBusPlate");
  if (busPlate) busPlate.textContent = activeMorningBus?.reg || 'NB-1234';

  const liveDest = document.getElementById("liveDestination");
  if (liveDest) liveDest.textContent = assignedSchool || activeRoute.schoolName;

  const driverName = document.getElementById("driverName");
  if (driverName) driverName.textContent = activeMorningBus?.driver || 'Kasun Fernando';

  const driverPhone = document.getElementById("driverPhone");
  if (driverPhone) driverPhone.textContent = activeMorningBus?.phone || '0779876543';

  const driverCallBtn = document.getElementById("driverCallBtn");
  if (driverCallBtn) driverCallBtn.setAttribute("href", `tel:${activeMorningBus?.phone || '0779876543'}`);

  const liveNextStop = document.getElementById("liveNextStop");
  if (liveNextStop) liveNextStop.textContent = calculateNextStop();

  const liveETA = document.getElementById("liveETA");
  if (liveETA) liveETA.innerHTML = `${calculateEta()} <small>min</small>`;
}


function handleBusChange(runType, busId) {
  const buses = activeRoute.buses || [];
  const bus = buses.find(b => b.id === busId);
  if (!bus) return;

  if (runType === "morning") {
    activeMorningBus = bus;
  } else {
    activeReturnBus = bus;
  }

  if (typeof NextGoSimulation !== "undefined") {
    NextGoSimulation.setBus(runType, bus.id);
  }

  saveAssignmentState();

  // Update telemetry cards
  document.getElementById("telemetryBusName").textContent = activeMorningBus.name;
  document.getElementById("telemetryBusPlate").textContent = activeMorningBus.reg;
  document.getElementById("driverName").textContent = activeMorningBus.driver;
  document.getElementById("driverPhone").textContent = activeMorningBus.phone;
  document.getElementById("driverCallBtn").setAttribute("href", `tel:${activeMorningBus.phone}`);

  showToast(`Assigned ${runType === 'morning' ? 'Morning' : 'Return'} bus: ${bus.name} (${bus.reg})`);
}

function handleStopSelect(type, stopName) {
  if (type === "pickup") {
    activePickupStop = stopName;
  } else {
    activeDropoffStop = stopName;
  }
  if (typeof NextGoSimulation !== "undefined") {
    NextGoSimulation.setStops(activePickupStop, activeDropoffStop);
  }
  saveAssignmentState();
  updateStopMarkersUI();
  persistStopChangeToBackend();
  showToast(`Updated ${type === 'pickup' ? 'Morning Pickup' : 'Return Drop-off'} to ${stopName}`);
}

function handleMapStopClick(type, stopName) {
  if (type === "pickup") {
    activePickupStop = stopName;
    const select = document.getElementById("pickupStopSelect");
    if (select) select.value = stopName;
  } else {
    activeDropoffStop = stopName;
    const select = document.getElementById("dropoffStopSelect");
    if (select) select.value = stopName;
  }
  if (typeof NextGoSimulation !== "undefined") {
    NextGoSimulation.setStops(activePickupStop, activeDropoffStop);
  }
  saveAssignmentState();
  updateStopMarkersUI();
  persistStopChangeToBackend();

  // Re-open popup on the clicked marker to display updated status
  const m = stopMarkersMap.get(stopName);
  if (m) {
    setTimeout(() => m.openPopup(), 60);
  }

  showToast(`Assigned ${type === 'pickup' ? 'Morning Pickup' : 'Return Drop-off'}: ${stopName}`);
}

function handleCancelStop(type) {
  if (type === "pickup") {
    activePickupStop = activeRoute.stops[0]?.name || "Route Terminal";
    const select = document.getElementById("pickupStopSelect");
    if (select) select.value = activePickupStop;
    showToast("Cancelled pickup stop (reverted to route terminal)");
  } else {
    activeDropoffStop = activeRoute.stops[activeRoute.stops.length - 1]?.name || "School";
    const select = document.getElementById("dropoffStopSelect");
    if (select) select.value = activeDropoffStop;
    showToast("Cancelled drop-off stop (reverted to school)");
  }
  if (typeof NextGoSimulation !== "undefined") {
    NextGoSimulation.setStops(activePickupStop, activeDropoffStop);
  }
  saveAssignmentState();
  updateStopMarkersUI();
  persistStopChangeToBackend();
}

function getActiveStopTimes() {
  const pObj = (activeRoute?.stops || []).find(s => s.name === activePickupStop);
  const dObj = (activeRoute?.stops || []).find(s => s.name === activeDropoffStop);
  return {
    pickupTime: pObj?.pickupTime || null,
    expectedArrival: dObj?.dropOffTime || null
  };
}

function persistStopChangeToBackend() {
  const times = getActiveStopTimes();
  try {
    Api.updateStudentStops(currentStudentId, {
      pickupStop: activePickupStop,
      dropOffStop: activeDropoffStop,
      pickupTime: times.pickupTime,
      expectedArrival: times.expectedArrival,
      routeId: activeRoute.id,
      routeName: activeRoute.name,
      originCity: activeRoute.originCity || "Suburbs",
      destinationSchool: assignedSchool || activeRoute.schoolName,
      busId: activeMorningBus?.id,
      morningBusId: activeMorningBus?.id,
      morningBusNumber: activeMorningBus?.reg,
      morningBusName: activeMorningBus?.name,
      morningDriver: activeMorningBus?.driver,
      morningPhone: activeMorningBus?.phone,
      returnBusId: activeReturnBus?.id,
      returnBusNumber: activeReturnBus?.reg,
      returnBusName: activeReturnBus?.name,
      returnDriver: activeReturnBus?.driver,
      returnPhone: activeReturnBus?.phone
    }).catch(() => {});
  } catch {}
}

async function saveTransportAssignment() {
  const btn = document.getElementById("saveAssignmentBtn");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Saving Assignment…";
  }

  saveAssignmentState();

  const times = getActiveStopTimes();
  let saveSuccess = true;
  let errorMsg = "";

  try {
    await Api.updateStudentStops(currentStudentId, {
      pickupStop: activePickupStop,
      dropOffStop: activeDropoffStop,
      pickupTime: times.pickupTime,
      expectedArrival: times.expectedArrival,
      routeId: activeRoute.id,
      routeName: activeRoute.name,
      originCity: activeRoute.originCity || "Suburbs",
      destinationSchool: assignedSchool || activeRoute.schoolName,
      busId: activeMorningBus?.id,
      morningBusId: activeMorningBus?.id,
      morningBusNumber: activeMorningBus?.reg,
      morningBusName: activeMorningBus?.name,
      morningDriver: activeMorningBus?.driver,
      morningPhone: activeMorningBus?.phone,
      returnBusId: activeReturnBus?.id,
      returnBusNumber: activeReturnBus?.reg,
      returnBusName: activeReturnBus?.name,
      returnDriver: activeReturnBus?.driver,
      returnPhone: activeReturnBus?.phone
    });
  } catch (err) {
    console.warn("Backend update notice:", err);
    saveSuccess = false;
    errorMsg = err.message || "Failed to save to database";
  }

  if (btn) {
    btn.disabled = false;
    if (saveSuccess) {
      btn.textContent = "Saved Successfully!";
      setTimeout(() => {
        btn.textContent = "Save Transport Assignment";
      }, 2500);
      showToast(`Saved to Database! Route: ${activeRoute.name} &bull; Morning: ${activeMorningBus?.reg} &bull; Return: ${activeReturnBus?.reg}`);
    } else {
      btn.textContent = "Save Failed";
      setTimeout(() => {
        btn.textContent = "Save Transport Assignment";
      }, 2500);
      showToast(`Database Error: ${errorMsg}`);
    }
  }
}

async function deleteTransportAssignment() {
  if (!confirm("Are you sure you want to delete the transport assignment for this student from the database?")) {
    return;
  }

  const btn = document.getElementById("deleteAssignmentBtn");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Deleting…";
  }

  try {
    if (typeof Api !== "undefined" && typeof Api.deleteStudentTransport === "function") {
      await Api.deleteStudentTransport(currentStudentId);
    }
    // Remove locally cached student transport assignment
    localStorage.removeItem(getStorageKey());
    localStorage.removeItem(`nextgo_student_school_${currentStudentId}`);

    showToast("Transport assignment deleted from database.");

    // Reload page after brief delay so UI resets cleanly
    setTimeout(() => {
      window.location.reload();
    }, 1200);
  } catch (err) {
    console.error("Delete assignment error:", err);
    if (btn) {
      btn.disabled = false;
      btn.textContent = "Delete Transport Assignment";
    }
    showToast(`Error deleting assignment: ${err.message || "Failed"}`);
  }
}

function showToast(msg) {
  const old = document.querySelector(".tracking-toast");
  if (old) old.remove();

  const toast = document.createElement("div");
  toast.className = "tracking-toast";
  toast.innerHTML = `<span>${SVG_ICONS.check}</span> <span>${escapeHtml(msg)}</span>`;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.style.transition = "opacity 0.4s ease, transform 0.4s ease";
    toast.style.opacity = "0";
    toast.style.transform = "translateY(10px)";
    setTimeout(() => toast.remove(), 400);
  }, 2600);
}
