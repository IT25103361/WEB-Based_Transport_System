package com.webtrans.school.service;

import com.webtrans.school.model.*;
import com.webtrans.school.repository.*;
import com.webtrans.school.util.GeoUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real-time GPS simulation engine for Kottawa <-> SLIIT school runs.
 * Persists live telemetry into the SQL Server database on every tick,
 * stops at assigned student pickup/drop-off spots, and generates live notifications.
 */
@Service
public class MockGpsService {

    private final BusLocationRepository locations;
    private final SchoolBusRepository buses;
    private final RouteStopRepository routeStops;
    private final NotificationRepository notifications;
    private final StudentArrivalRepository arrivals;
    private final RoutePathService pathService;
    private final TransportScheduleRepository schedules;
    private final StudentRepository students;

    // Bus state
    private final Map<Long, Integer> busStepMap = new ConcurrentHashMap<>();
    private final Map<Long, String> busDirectionMap = new ConcurrentHashMap<>(); // "FORWARD", "RETURN", or "AUTO"
    private final Map<Long, Integer> pauseCountMap = new ConcurrentHashMap<>();
    private final Set<String> notifiedStops = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public MockGpsService(
            BusLocationRepository locations,
            SchoolBusRepository buses,
            RouteStopRepository routeStops,
            NotificationRepository notifications,
            StudentArrivalRepository arrivals,
            RoutePathService pathService,
            TransportScheduleRepository schedules,
            StudentRepository students
    ) {
        this.locations = locations;
        this.buses = buses;
        this.routeStops = routeStops;
        this.notifications = notifications;
        this.arrivals = arrivals;
        this.pathService = pathService;
        this.schedules = schedules;
        this.students = students;
    }

    public void setTripMode(Long busId, String mode) {
        String upper = mode != null ? mode.toUpperCase() : "AUTO";
        busDirectionMap.put(busId, upper);
        pauseCountMap.put(busId, 0);
        notifiedStops.clear();

        if ("FORWARD".equals(upper)) {
            busStepMap.put(busId, 0); // Start from Kottawa
        } else if ("RETURN".equals(upper)) {
            busStepMap.put(busId, 0); // Start from SLIIT
        }
        advanceBus(busId);
    }

    public String getTripMode(Long busId) {
        return busDirectionMap.getOrDefault(busId, "AUTO");
    }

    @Scheduled(fixedDelay = 3500, initialDelay = 3000)
    public void scheduledTick() {
        advanceBus(1L);
        advanceBus(2L);
    }

    public BusLocation advanceBus(Long busId) {
        Optional<SchoolBus> busOpt = buses.findById(busId);
        if (busOpt.isEmpty()) return null;
        SchoolBus bus = busOpt.get();
        if (Boolean.FALSE.equals(bus.active)) return null;

        String mode = busDirectionMap.getOrDefault(busId, "AUTO");
        Long activeRouteId = "RETURN".equalsIgnoreCase(mode) ? 2L : 1L;

        List<double[]> path = pathService.getPathForRoute(activeRouteId);
        if (path.isEmpty()) return null;

        int step = busStepMap.getOrDefault(busId, 0);

        // Check if currently pausing at a stop
        int pause = pauseCountMap.getOrDefault(busId, 0);
        if (pause > 0) {
            pauseCountMap.put(busId, pause - 1);
            return locations.findTopByBusIdOrderByRecordedAtDesc(busId).orElse(null);
        }

        // Terminal handling (end of route)
        if (step >= path.size() - 1) {
            pauseCountMap.put(busId, 2);
            bus.currentStatus = "ARRIVED AT TERMINAL";
            buses.save(bus);

            // In AUTO mode, alternate direction
            if ("AUTO".equals(mode)) {
                activeRouteId = (activeRouteId == 1L) ? 2L : 1L;
                busDirectionMap.put(busId, "AUTO");
            }
            notifiedStops.clear();
            step = 0;
            busStepMap.put(busId, 0);
        }

        double[] currentCoord = path.get(step);
        int nextIndex = Math.min(step + 1, path.size() - 1);
        double[] nextCoord = path.get(nextIndex);

        double heading = GeoUtil.bearing(currentCoord[0], currentCoord[1], nextCoord[0], nextCoord[1]);
        double speed = 26.0 + (Math.sin(step * 0.8) * 8.0); // 26 - 34 km/h

        BusLocation loc = new BusLocation();
        loc.busId = busId;
        loc.latitude = currentCoord[0];
        loc.longitude = currentCoord[1];
        loc.speed = Math.round(speed * 10.0) / 10.0;
        loc.heading = Math.round(heading * 10.0) / 10.0;
        loc.recordedAt = LocalDateTime.now();
        loc.gpsStatus = "LIVE GPS";
        locations.save(loc);

        // Check proximity to stops and student assigned stops
        List<RouteStop> stops = routeStops.findByRouteIdOrderByStopSequenceAsc(activeRouteId);
        List<TransportSchedule> busSchedules = schedules.findByBusId(busId);

        String calculatedStatus = (activeRouteId == 1L) ? "KOTTAWA ➔ SLIIT" : "SLIIT ➔ KOTTAWA";

        for (RouteStop stop : stops) {
            double dist = GeoUtil.metres(currentCoord[0], currentCoord[1], stop.latitude, stop.longitude);
            if (dist <= 120.0) {
                // Check if this is an assigned stop for any student
                for (TransportSchedule s : busSchedules) {
                    String targetStop = (activeRouteId == 1L) ? s.pickupStop : s.dropOffStop;
                    if (targetStop != null && targetStop.equalsIgnoreCase(stop.stopName)) {
                        calculatedStatus = "STOPPED: " + stop.stopName.toUpperCase();
                        pauseCountMap.put(busId, 2); // Stop at this spot!

                        // Send arrival notification to parent
                        String notifyKey = busId + "_" + s.studentId + "_" + stop.stopName + "_" + (activeRouteId == 1L ? "AM" : "PM");
                        if (!notifiedStops.contains(notifyKey)) {
                            notifiedStops.add(notifyKey);
                            Student st = students.findById(s.studentId).orElse(null);
                            if (st != null && st.parent != null) {
                                Notification n = new Notification();
                                n.parentId = st.parent.id;
                                n.studentId = st.id;
                                n.type = (activeRouteId == 1L) ? "BUS_AT_PICKUP" : "BUS_AT_DROPOFF";
                                n.title = (activeRouteId == 1L) ? "Bus Arrived at Pickup" : "Bus Arrived at Drop-off";
                                n.message = "School Bus " + bus.registrationNumber + " has arrived at " + stop.stopName + " for " + st.fullName + ".";
                                n.createdAt = LocalDateTime.now();
                                n.readStatus = false;
                                notifications.save(n);
                            }
                        }
                        break;
                    }
                }

                if (!calculatedStatus.startsWith("STOPPED")) {
                    if (stop.stopSequence == stops.size()) {
                        calculatedStatus = "ARRIVED AT " + stop.stopName.toUpperCase();
                    } else {
                        calculatedStatus = "AT " + stop.stopName.toUpperCase();
                    }
                }
                break;
            } else if (dist <= 250.0) {
                calculatedStatus = "APPROACHING " + stop.stopName.toUpperCase();
                break;
            }
        }

        bus.currentStatus = calculatedStatus;
        buses.save(bus);

        busStepMap.put(busId, step + 1);
        return loc;
    }
}