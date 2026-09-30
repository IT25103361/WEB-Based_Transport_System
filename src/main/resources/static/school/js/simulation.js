/* =============================================================================
   NEXTGO Unified Bus Simulation Engine
   Persistent Real-Time Simulation across all pages (Live Tracking, Schedule,
   Dashboard, Student, Notifications, Profile).
   Maintains state in localStorage with sub-millisecond time-delta catch-up.
   ============================================================================= */

const NextGoSimulation = (() => {
  const STORAGE_PREFIX = "nextgo_bus_sim_state_";
  const BASE_STEP_INTERVAL = 1200; // ms per waypoint at 1x speed
  const BASE_LAYOVER_DURATION = 5000; // 5 seconds at school / terminal

  let activeStudentId = "1";
  let activeRoute = null;
  let activeMorningBus = null;
  let activeReturnBus = null;
  let activePickupStop = "";
  let activeDropoffStop = "";

  let state = {
    studentId: "1",
    routeId: 1,
    currentStepIndex: 0,
    direction: "outbound", // "outbound" = morning run, "return" = afternoon run
    cycleCount: 0,
    speedMultiplier: 1,
    isPaused: false,
    isLayover: false,
    layoverRemainingMs: 0,
    notifiedPickupCycle: -1,
    notifiedDropoffCycle: -1,
    notifiedSchoolCycle: -1,
    notifiedTerminalCycle: -1,
    currentSpeed: 32,
    lastUpdated: Date.now()
  };

  let tickListeners = [];
  let stateChangeListeners = [];
  let liveLoopTimer = null;
  let backgroundLoopTimer = null;

  function getStorageKey(studentId) {
    return `${STORAGE_PREFIX}${studentId || activeStudentId || "1"}`;
  }

  function loadStateFromStorage(studentId, targetRouteId) {
    try {
      const raw = localStorage.getItem(getStorageKey(studentId));
      if (raw) {
        const parsed = JSON.parse(raw);
        if (parsed && typeof parsed === "object") {
          // If the route matches or if targetRouteId is not strictly forced, restore
          if (!targetRouteId || parsed.routeId === targetRouteId) {
            return parsed;
          }
        }
      }
    } catch (e) {
      console.warn("Could not parse simulation state from storage:", e);
    }
    return null;
  }

  function saveStateToStorage() {
    try {
      state.studentId = activeStudentId;
      state.routeId = activeRoute?.id || state.routeId || 1;
      localStorage.setItem(getStorageKey(activeStudentId), JSON.stringify(state));
      // Also save general pointer for current active simulation
      localStorage.setItem(`${STORAGE_PREFIX}latest`, JSON.stringify(state));
    } catch (e) {}
  }

  function resolveActiveRoute(routeId) {
    if (typeof ALL_ROUTES_DATA !== "undefined" && Array.isArray(ALL_ROUTES_DATA)) {
      return ALL_ROUTES_DATA.find(r => r.id === routeId) || ALL_ROUTES_DATA[0];
    }
    return null;
  }

  function resolveStudentAssignment(studentId) {
    try {
      const saved = localStorage.getItem(`nextgo_transport_assignment_${studentId}`);
      if (saved) return JSON.parse(saved);
    } catch {}
    return null;
  }

  function init(studentId = null, targetRoute = null, morningBus = null, returnBus = null, pickupStop = null, dropoffStop = null) {
    activeStudentId = String(studentId || (typeof selectedStudent === "function" ? selectedStudent() : null) || localStorage.getItem("nextgo_selected_student") || "1");

    const savedAssignment = resolveStudentAssignment(activeStudentId);
    const routeId = targetRoute?.id || savedAssignment?.routeId || 1;

    activeRoute = targetRoute || resolveActiveRoute(routeId);
    if (!activeRoute && typeof ALL_ROUTES_DATA !== "undefined") {
      activeRoute = ALL_ROUTES_DATA[0];
    }

    const buses = activeRoute?.buses || [];
    activeMorningBus = morningBus || buses.find(b => b.id === (savedAssignment?.morningBusId)) || buses[0] || null;
    activeReturnBus = returnBus || buses.find(b => b.id === (savedAssignment?.returnBusId)) || buses[1] || buses[0] || null;

    const stops = activeRoute?.stops || [];
    activePickupStop = pickupStop || savedAssignment?.pickupStop || stops[Math.min(3, stops.length - 1)]?.name || stops[0]?.name || "";
    activeDropoffStop = dropoffStop || savedAssignment?.dropoffStop || stops[Math.min(3, stops.length - 1)]?.name || stops[0]?.name || "";

    // Load or initialize state
    const loaded = loadStateFromStorage(activeStudentId, activeRoute?.id);
    if (loaded && loaded.routeId === activeRoute?.id) {
      state = Object.assign({}, state, loaded);
      // Catch up with elapsed time
      const now = Date.now();
      const elapsed = Math.max(0, now - (state.lastUpdated || now));
      if (elapsed > 0 && !state.isPaused) {
        advanceTime(elapsed, true);
      } else {
        state.lastUpdated = now;
      }
    } else {
      // Fresh initialization for this route
      state.studentId = activeStudentId;
      state.routeId = activeRoute?.id || 1;
      state.currentStepIndex = 0;
      state.direction = "outbound";
      state.cycleCount = 0;
      state.speedMultiplier = loaded?.speedMultiplier || 1;
      state.isPaused = false;
      state.isLayover = false;
      state.layoverRemainingMs = 0;
      state.notifiedPickupCycle = -1;
      state.notifiedDropoffCycle = -1;
      state.notifiedSchoolCycle = -1;
      state.notifiedTerminalCycle = -1;
      state.currentSpeed = 32;
      state.lastUpdated = Date.now();
      saveStateToStorage();
    }

    notifyStateChange();
    return state;
  }

  function advanceTime(elapsedMs, isCatchUp = false) {
    if (state.isPaused || elapsedMs <= 0 || !activeRoute) {
      state.lastUpdated = Date.now();
      saveStateToStorage();
      return;
    }

    const waypoints = activeRoute.waypoints || [];
    if (waypoints.length < 2) {
      state.lastUpdated = Date.now();
      return;
    }

    const stepInterval = Math.max(50, Math.round(BASE_STEP_INTERVAL / state.speedMultiplier));
    const layoverDuration = Math.max(800, Math.round(BASE_LAYOVER_DURATION / state.speedMultiplier));

    // Cap max catch-up time to 15 minutes to prevent infinite while loops if tab opened days later
    let remainingMs = Math.min(elapsedMs, 900000);

    while (remainingMs >= stepInterval) {
      if (state.isLayover) {
        if (remainingMs >= state.layoverRemainingMs) {
          remainingMs -= state.layoverRemainingMs;
          state.isLayover = false;
          state.layoverRemainingMs = 0;

          if (state.direction === "outbound") {
            state.direction = "return";
            state.notifiedDropoffCycle = -1;
            if (!isCatchUp && typeof showToast === "function") {
              showToast(`Starting Return Trip towards ${activeRoute.originCity}`);
            }
          } else {
            state.direction = "outbound";
            state.cycleCount++;
            state.notifiedPickupCycle = -1;
            if (!isCatchUp && typeof showToast === "function") {
              showToast(`Starting Morning Run towards ${activeRoute.schoolName}`);
            }
          }
        } else {
          state.layoverRemainingMs -= remainingMs;
          remainingMs = 0;
          break;
        }
      } else {
        remainingMs -= stepInterval;
        stepOnce(isCatchUp, stepInterval, layoverDuration);
      }
    }

    state.lastUpdated = Date.now() - remainingMs;
    saveStateToStorage();
  }

  function stepOnce(isCatchUp = false, stepInterval = 1200, layoverDuration = 5000) {
    const waypoints = activeRoute.waypoints || [];
    if (waypoints.length < 2) return;

    if (state.direction === "outbound") {
      state.currentStepIndex++;

      // Reached School Destination
      if (state.currentStepIndex >= waypoints.length - 1) {
        state.currentStepIndex = waypoints.length - 1;
        state.isLayover = true;
        state.layoverRemainingMs = layoverDuration;

        const savedSt = loadStateFromStorage(activeStudentId, activeRoute?.id);
        if (savedSt && savedSt.notifiedSchoolCycle === state.cycleCount) {
          state.notifiedSchoolCycle = savedSt.notifiedSchoolCycle;
        }

        if (state.notifiedSchoolCycle !== state.cycleCount) {
          state.notifiedSchoolCycle = state.cycleCount;
          saveStateToStorage();
          const busNum = activeMorningBus?.reg || "NB-1234";
          const schoolTitle = "Arrived at School Destination";
          const schoolMsg = `School Bus ${busNum} has safely arrived at ${activeRoute.schoolName}.`;
          if (!isCatchUp && typeof addSystemNotification === "function") {
            addSystemNotification(schoolTitle, schoolMsg, "arrival", activeStudentId);
          }
          if (!isCatchUp && typeof showToast === "function") {
            showToast(`Arrived at School: ${activeRoute.schoolName}`);
          }
          if (typeof Api !== "undefined" && typeof Api.recordArrival === "function" && state.studentId) {
            Api.recordArrival(state.studentId).catch(() => {});
          }
        }
        return;
      }

      const coord = waypoints[state.currentStepIndex];
      checkStopArrivalTrigger(coord, "outbound", isCatchUp);

      // Periodically ping live location to SQL Server
      if (state.currentStepIndex % 20 === 0 && activeMorningBus?.id && typeof Api !== "undefined" && typeof Api.sendLocationPing === "function") {
        const nextCoord = waypoints[Math.min(waypoints.length - 1, state.currentStepIndex + 1)];
        const angle = calculateHeadingAngle(coord, nextCoord);
        Api.sendLocationPing(activeMorningBus.id, {
          latitude: coord[0],
          longitude: coord[1],
          speed: state.currentSpeed,
          heading: angle
        }).catch(() => {});
      }

    } else {
      // RETURN LEG
      state.currentStepIndex--;

      // Reached Terminal Origin
      if (state.currentStepIndex <= 0) {
        state.currentStepIndex = 0;
        state.isLayover = true;
        state.layoverRemainingMs = layoverDuration;

        const savedSt = loadStateFromStorage(activeStudentId, activeRoute?.id);
        if (savedSt && savedSt.notifiedTerminalCycle === state.cycleCount) {
          state.notifiedTerminalCycle = savedSt.notifiedTerminalCycle;
        }

        if (state.notifiedTerminalCycle !== state.cycleCount) {
          state.notifiedTerminalCycle = state.cycleCount;
          saveStateToStorage();
          const busNum = activeReturnBus?.reg || "WP-5678";
          const termTitle = "Return Trip Completed";
          const startName = activeRoute.startLocation || (activeRoute.originCity + " Bus Terminal");
          const termMsg = `School Bus ${busNum} has returned safely to ${startName}.`;
          if (!isCatchUp && typeof addSystemNotification === "function") {
            addSystemNotification(termTitle, termMsg, "arrival", activeStudentId);
          }
          if (!isCatchUp && typeof showToast === "function") {
            showToast(`Trip Complete: Returned to ${startName}`);
          }
        }
        return;
      }

      const coord = waypoints[state.currentStepIndex];
      checkStopArrivalTrigger(coord, "return", isCatchUp);

      // Periodically ping live location to SQL Server
      if (state.currentStepIndex % 20 === 0 && activeReturnBus?.id && typeof Api !== "undefined" && typeof Api.sendLocationPing === "function") {
        const prevCoord = waypoints[Math.max(0, state.currentStepIndex - 1)];
        const angle = calculateHeadingAngle(coord, prevCoord);
        Api.sendLocationPing(activeReturnBus.id, {
          latitude: coord[0],
          longitude: coord[1],
          speed: state.currentSpeed,
          heading: angle
        }).catch(() => {});
      }
    }

    state.currentSpeed = 28 + Math.floor(Math.sin(state.currentStepIndex * 0.2) * 8 + 4);
  }

  function checkStopArrivalTrigger(currentCoord, direction, isCatchUp = false) {
    if (!activeRoute) return;
    const stops = activeRoute.stops || [];

    if (direction === "outbound") {
      const pStop = stops.find(s => s.name === activePickupStop);
      const savedSt = loadStateFromStorage(activeStudentId, activeRoute?.id);
      if (savedSt && savedSt.notifiedPickupCycle === state.cycleCount) {
        state.notifiedPickupCycle = savedSt.notifiedPickupCycle;
      }

      if (pStop && state.notifiedPickupCycle !== state.cycleCount) {
        const stopLat = pStop.lat ?? pStop.coords?.[0];
        const stopLon = pStop.lon ?? pStop.coords?.[1];
        if (stopLat != null && stopLon != null) {
          const dist = Math.hypot(stopLat - currentCoord[0], stopLon - currentCoord[1]);
          if (dist < 0.0018) {
            state.notifiedPickupCycle = state.cycleCount;
            saveStateToStorage();
            const busNum = activeMorningBus?.reg || "NB-1234";
            const title = "Bus Arrived at Pickup Spot";
            const msg = `School Bus ${busNum} has arrived at ${activePickupStop} for your child.`;
            if (!isCatchUp && typeof addSystemNotification === "function") {
              addSystemNotification(title, msg, "arrival", activeStudentId);
            }
            if (!isCatchUp && typeof showToast === "function") {
              showToast(`Arrived at Pickup: ${activePickupStop}`);
            }
          }
        }
      }
    } else {
      const dStop = stops.find(s => s.name === activeDropoffStop);
      const savedSt = loadStateFromStorage(activeStudentId, activeRoute?.id);
      if (savedSt && savedSt.notifiedDropoffCycle === state.cycleCount) {
        state.notifiedDropoffCycle = savedSt.notifiedDropoffCycle;
      }

      if (dStop && state.notifiedDropoffCycle !== state.cycleCount) {
        const stopLat = dStop.lat ?? dStop.coords?.[0];
        const stopLon = dStop.lon ?? dStop.coords?.[1];
        if (stopLat != null && stopLon != null) {
          const dist = Math.hypot(stopLat - currentCoord[0], stopLon - currentCoord[1]);
          if (dist < 0.0018) {
            state.notifiedDropoffCycle = state.cycleCount;
            saveStateToStorage();
            const busNum = activeReturnBus?.reg || activeMorningBus?.reg || "WP-5678";
            const title = "Bus Arrived at Drop-off Spot";
            const msg = `School Bus ${busNum} has arrived at ${activeDropoffStop} for your child.`;
            if (!isCatchUp && typeof addSystemNotification === "function") {
              addSystemNotification(title, msg, "arrival", activeStudentId);
            }
            if (!isCatchUp && typeof showToast === "function") {
              showToast(`Arrived at Drop-off: ${activeDropoffStop}`);
            }
          }
        }
      }
    }
  }

  function calculateHeadingAngle(coord1, coord2) {
    if (!coord1 || !coord2) return 0;
    const dy = coord2[0] - coord1[0];
    const dx = Math.cos((Math.PI / 180) * coord1[0]) * (coord2[1] - coord1[1]);
    return Math.round((Math.atan2(dx, dy) * 180) / Math.PI);
  }

  function getHeadingAngle() {
    if (!activeRoute) return 0;
    const waypoints = activeRoute.waypoints || [];
    if (waypoints.length < 2) return 0;

    const idx = Math.max(0, Math.min(waypoints.length - 1, state.currentStepIndex));
    if (state.direction === "outbound") {
      const nextIdx = Math.min(waypoints.length - 1, idx + 1);
      return calculateHeadingAngle(waypoints[idx], waypoints[nextIdx]);
    } else {
      const prevIdx = Math.max(0, idx - 1);
      return calculateHeadingAngle(waypoints[idx], waypoints[prevIdx]);
    }
  }

  function getLiveCoord() {
    if (!activeRoute) return [6.8402, 79.9654];
    const waypoints = activeRoute.waypoints || [];
    if (!waypoints.length) return [6.8402, 79.9654];
    const idx = Math.max(0, Math.min(waypoints.length - 1, state.currentStepIndex));
    return waypoints[idx];
  }

  function calculateEta() {
    if (!activeRoute) return 6;
    const stops = activeRoute.stops || [];
    const totalWaypoints = (activeRoute.waypoints || []).length;
    if (!totalWaypoints) return 6;

    if (state.direction === "outbound") {
      const pickupIdx = stops.findIndex(s => s.name === activePickupStop);
      const targetWaypointIdx = Math.floor(((pickupIdx >= 0 ? pickupIdx : 4) / stops.length) * totalWaypoints);
      if (state.currentStepIndex <= targetWaypointIdx) {
        const remainingSteps = targetWaypointIdx - state.currentStepIndex;
        return Math.max(1, Math.round((remainingSteps / totalWaypoints) * 22));
      } else {
        const remainingSteps = totalWaypoints - 1 - state.currentStepIndex;
        return Math.max(1, Math.round((remainingSteps / totalWaypoints) * 22));
      }
    } else {
      const dropoffIdx = stops.findIndex(s => s.name === activeDropoffStop);
      const targetWaypointIdx = Math.floor(((dropoffIdx >= 0 ? dropoffIdx : 4) / stops.length) * totalWaypoints);
      if (state.currentStepIndex >= targetWaypointIdx) {
        const remainingSteps = state.currentStepIndex - targetWaypointIdx;
        return Math.max(1, Math.round((remainingSteps / totalWaypoints) * 22));
      } else {
        const remainingSteps = state.currentStepIndex;
        return Math.max(1, Math.round((remainingSteps / totalWaypoints) * 22));
      }
    }
  }

  function calculateNextStop() {
    if (!activeRoute) return "Approaching Stop";
    const stops = activeRoute.stops || [];
    const totalWaypoints = (activeRoute.waypoints || []).length;
    if (!totalWaypoints || !stops.length) return "Approaching Stop";

    const currentRatio = state.currentStepIndex / totalWaypoints;
    if (state.direction === "outbound") {
      const nextStopIdx = Math.min(stops.length - 1, Math.floor(currentRatio * stops.length) + 1);
      return stops[nextStopIdx]?.name || stops[stops.length - 1].name;
    } else {
      const nextStopIdx = Math.max(0, Math.floor(currentRatio * stops.length) - 1);
      return stops[nextStopIdx]?.name || stops[0].name;
    }
  }

  function getStatusText() {
    if (state.isLayover) {
      return state.direction === "outbound" ? "AT SCHOOL (Layover)" : "AT TERMINAL (Layover)";
    }
    return state.direction === "outbound" ? "MORNING RUN" : "RETURN RUN";
  }

  function getCurrentTelemetry() {
    const isMorning = state.direction === "outbound";
    const currentBus = isMorning ? activeMorningBus : activeReturnBus;
    return {
      coord: getLiveCoord(),
      headingAngle: getHeadingAngle(),
      speed: state.currentSpeed,
      eta: calculateEta(),
      nextStop: calculateNextStop(),
      statusText: getStatusText(),
      direction: state.direction,
      isPaused: state.isPaused,
      isLayover: state.isLayover,
      speedMultiplier: state.speedMultiplier,
      busName: currentBus?.name || (isMorning ? "Morning Bus" : "Return Bus"),
      busPlate: currentBus?.reg || (isMorning ? "NB-1234" : "WP-5678"),
      driverName: currentBus?.driver || "Kasun Fernando",
      driverPhone: currentBus?.phone || "0779876543",
      destination: isMorning ? activeRoute?.schoolName : (activeRoute?.startLocation || (activeRoute?.originCity + " Terminal"))
    };
  }

  function startLiveLoop(onTickCallback) {
    stopLiveLoop();
    stopBackgroundLoop();

    if (typeof onTickCallback === "function" && !tickListeners.includes(onTickCallback)) {
      tickListeners.push(onTickCallback);
    }

    const intervalMs = Math.max(50, Math.round(BASE_STEP_INTERVAL / state.speedMultiplier));

    // Immediate tick for current state
    notifyTick();

    liveLoopTimer = setInterval(() => {
      if (state.isPaused) return;

      const now = Date.now();
      const elapsed = now - (state.lastUpdated || now);
      state.lastUpdated = now;

      // Handle layover countdown
      if (state.isLayover) {
        state.layoverRemainingMs -= elapsed;
        if (state.layoverRemainingMs <= 0) {
          state.isLayover = false;
          state.layoverRemainingMs = 0;
          if (state.direction === "outbound") {
            state.direction = "return";
            state.notifiedDropoffCycle = -1;
            if (typeof showToast === "function") {
              showToast(`Starting Return Trip towards ${activeRoute?.originCity || 'Origin'}`);
            }
          } else {
            state.direction = "outbound";
            state.cycleCount++;
            state.notifiedPickupCycle = -1;
            if (typeof showToast === "function") {
              showToast(`Starting Morning Run towards ${activeRoute?.schoolName || 'School'}`);
            }
          }
        }
      } else {
        stepOnce(false, intervalMs, Math.max(800, Math.round(BASE_LAYOVER_DURATION / state.speedMultiplier)));
      }

      saveStateToStorage();
      notifyTick();
    }, intervalMs);
  }

  function stopLiveLoop() {
    if (liveLoopTimer) {
      clearInterval(liveLoopTimer);
      liveLoopTimer = null;
    }
  }

  function startBackgroundLoop() {
    stopLiveLoop();
    stopBackgroundLoop();

    backgroundLoopTimer = setInterval(() => {
      if (state.isPaused) return;
      const now = Date.now();
      const elapsed = Math.max(0, now - (state.lastUpdated || now));
      if (elapsed >= 1000) {
        advanceTime(elapsed, false);
      }
    }, 1000);
  }

  function stopBackgroundLoop() {
    if (backgroundLoopTimer) {
      clearInterval(backgroundLoopTimer);
      backgroundLoopTimer = null;
    }
  }

  function togglePause() {
    state.isPaused = !state.isPaused;
    state.lastUpdated = Date.now();
    saveStateToStorage();
    notifyStateChange();
    return state.isPaused;
  }

  function restart() {
    state.currentStepIndex = 0;
    state.direction = "outbound";
    state.cycleCount = 0;
    state.isPaused = false;
    state.isLayover = false;
    state.layoverRemainingMs = 0;
    state.notifiedPickupCycle = -1;
    state.notifiedDropoffCycle = -1;
    state.notifiedSchoolCycle = -1;
    state.notifiedTerminalCycle = -1;
    state.lastUpdated = Date.now();
    saveStateToStorage();
    notifyStateChange();
    notifyTick();
  }

  function setSpeed(mult) {
    state.speedMultiplier = Number(mult) || 1;
    state.lastUpdated = Date.now();
    saveStateToStorage();
    notifyStateChange();
    if (liveLoopTimer) {
      startLiveLoop();
    }
  }

  function setRoute(newRouteId, newMorningBus = null, newReturnBus = null, newPickupStop = null, newDropoffStop = null) {
    const found = resolveActiveRoute(newRouteId);
    if (!found) return;

    activeRoute = found;
    activeMorningBus = newMorningBus || found.buses[0];
    activeReturnBus = newReturnBus || found.buses[1] || found.buses[0];
    activePickupStop = newPickupStop || found.stops[Math.min(3, found.stops.length - 1)].name;
    activeDropoffStop = newDropoffStop || found.stops[Math.min(3, found.stops.length - 1)].name;

    // Reset simulation for the new route
    state.routeId = found.id;
    state.currentStepIndex = 0;
    state.direction = "outbound";
    state.cycleCount = 0;
    state.isLayover = false;
    state.layoverRemainingMs = 0;
    state.notifiedPickupCycle = -1;
    state.notifiedDropoffCycle = -1;
    state.notifiedSchoolCycle = -1;
    state.notifiedTerminalCycle = -1;
    state.lastUpdated = Date.now();

    saveStateToStorage();
    notifyStateChange();
    notifyTick();
  }

  function setBus(runType, busId) {
    if (!activeRoute) return;
    const buses = activeRoute.buses || [];
    const bus = buses.find(b => b.id === busId);
    if (!bus) return;
    if (runType === "morning") activeMorningBus = bus;
    else activeReturnBus = bus;
    notifyTick();
  }

  function setStops(pickupName, dropoffName) {
    if (pickupName) activePickupStop = pickupName;
    if (dropoffName) activeDropoffStop = dropoffName;
    notifyTick();
  }

  function onTick(cb) {
    if (typeof cb === "function" && !tickListeners.includes(cb)) {
      tickListeners.push(cb);
    }
  }

  function onStateChange(cb) {
    if (typeof cb === "function" && !stateChangeListeners.includes(cb)) {
      stateChangeListeners.push(cb);
    }
  }

  function notifyTick() {
    const telem = getCurrentTelemetry();
    for (let i = 0; i < tickListeners.length; i++) {
      try { tickListeners[i](state, telem); } catch (e) { console.error(e); }
    }
  }

  function notifyStateChange() {
    for (let i = 0; i < stateChangeListeners.length; i++) {
      try { stateChangeListeners[i](state); } catch (e) { console.error(e); }
    }
  }

  // Auto-save on page leave
  window.addEventListener("beforeunload", () => {
    state.lastUpdated = Date.now();
    saveStateToStorage();
  });
  window.addEventListener("pagehide", () => {
    state.lastUpdated = Date.now();
    saveStateToStorage();
  });

  return {
    init,
    advanceTime,
    startLiveLoop,
    stopLiveLoop,
    startBackgroundLoop,
    stopBackgroundLoop,
    togglePause,
    restart,
    setSpeed,
    setRoute,
    setBus,
    setStops,
    getLiveCoord,
    getHeadingAngle,
    calculateEta,
    calculateNextStop,
    getCurrentTelemetry,
    saveState: saveStateToStorage,
    getState: () => state,
    getActiveRoute: () => activeRoute,
    getActiveMorningBus: () => activeMorningBus,
    getActiveReturnBus: () => activeReturnBus,
    getActivePickupStop: () => activePickupStop,
    getActiveDropoffStop: () => activeDropoffStop,
    onTick,
    onStateChange
  };
})();

if (typeof window !== "undefined") {
  window.NextGoSimulation = NextGoSimulation;
}
