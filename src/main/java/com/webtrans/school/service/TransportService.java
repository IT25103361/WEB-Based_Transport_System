package com.webtrans.school.service;

import com.webtrans.school.dto.*;
import com.webtrans.school.exception.NotFoundException;
import com.webtrans.school.model.*;
import com.webtrans.school.repository.*;
import com.webtrans.school.util.GeoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TransportService {

    private final ParentRepository parents;
    private final StudentRepository students;
    private final SchoolBusRepository buses;
    private final TransportScheduleRepository schedules;
    private final BusLocationRepository locations;
    private final NotificationRepository notifications;
    private final StudentArrivalRepository arrivals;
    private final RouteStopRepository routeStops;
    private final BusRouteRepository routes;
    private final RoutePathService pathService;
    private final MockGpsService mockGps;

    public TransportService(
            ParentRepository parents,
            StudentRepository students,
            SchoolBusRepository buses,
            TransportScheduleRepository schedules,
            BusLocationRepository locations,
            NotificationRepository notifications,
            StudentArrivalRepository arrivals,
            RouteStopRepository routeStops,
            BusRouteRepository routes,
            RoutePathService pathService,
            MockGpsService mockGps
    ) {
        this.parents = parents;
        this.students = students;
        this.buses = buses;
        this.schedules = schedules;
        this.locations = locations;
        this.notifications = notifications;
        this.arrivals = arrivals;
        this.routeStops = routeStops;
        this.routes = routes;
        this.pathService = pathService;
        this.mockGps = mockGps;
    }

    public Parent login(LoginRequest r) {
        Parent p = parents.findByEmail(r.email())
                .orElseThrow(() -> new NotFoundException("Invalid email or password"));
        if (!hash(r.password()).equals(p.passwordHash)) {
            throw new NotFoundException("Invalid email or password");
        }
        return p;
    }

    public String hash(String v) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Student student(Long id) {
        return students.findById(id).orElseThrow(() -> new NotFoundException("Student not found"));
    }

    public Map<String, Object> transport(Long studentId) {
        Student st = students.findById(studentId).orElse(null);
        TransportSchedule s = schedules.findByStudentId(studentId).orElse(null);

        if (s == null) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("schedule", null);
            res.put("bus", null);
            res.put("morningBus", null);
            res.put("returnBus", null);
            res.put("routeName", null);
            res.put("originCity", null);
            res.put("destinationSchool", st != null ? st.schoolName : null);
            res.put("pickupStopName", null);
            res.put("dropOffStopName", null);
            res.put("etaMinutes", 10);
            res.put("nextStopName", "Pickup Stop");
            res.put("status", "NOT ASSIGNED");
            return res;
        }

        Long morningId = s.morningBusId != null ? s.morningBusId : (s.busId != null ? s.busId : 1L);
        SchoolBus b = buses.findById(morningId).orElse(null);
        SchoolBus retBus = s.returnBusId != null ? buses.findById(s.returnBusId).orElse(null) : null;

        Map<String, Object> etaInfo;
        try {
            etaInfo = eta(studentId);
        } catch (Exception ignored) {
            etaInfo = Map.of("etaMinutes", 12, "nextStop", s.pickupStop != null ? s.pickupStop : "Approaching Stop");
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("schedule", s);
        res.put("bus", b);
        res.put("morningBus", b);
        res.put("returnBus", retBus);
        res.put("routeName", s.routeName);
        res.put("originCity", s.originCity);
        res.put("destinationSchool", s.destinationSchool);
        res.put("pickupStopName", s.pickupStop);
        res.put("dropOffStopName", s.dropOffStop);
        res.put("etaMinutes", etaInfo.get("etaMinutes"));
        res.put("nextStopName", etaInfo.get("nextStop"));
        res.put("status", b != null && b.currentStatus != null ? b.currentStatus : "ON ROUTE");
        return res;
    }

    public TransportSchedule updateStudentStops(Long studentId, StopUpdateRequest req) {
        Student st = students.findById(studentId).orElse(null);

        TransportSchedule s = schedules.findByStudentId(studentId)
                .orElseGet(() -> {
                    TransportSchedule ns = new TransportSchedule();
                    ns.studentId = studentId;
                    ns.active = true;
                    ns.daysOfOperation = "Monday - Friday";
                    return ns;
                });

        if (req.routeId() != null) {
            s.routeId = req.routeId();
        }
        if (req.routeName() != null && !req.routeName().isBlank()) {
            s.routeName = req.routeName().trim();
        }
        if (req.originCity() != null && !req.originCity().isBlank()) {
            s.originCity = req.originCity().trim();
        }
        if (req.destinationSchool() != null && !req.destinationSchool().isBlank()) {
            s.destinationSchool = req.destinationSchool().trim();
        } else if (s.destinationSchool == null && st != null && st.schoolName != null) {
            s.destinationSchool = st.schoolName.trim();
        }

        if (req.pickupStop() != null && !req.pickupStop().isBlank()) {
            s.pickupStop = cleanStopName(req.pickupStop());
        }
        if (req.dropOffStop() != null && !req.dropOffStop().isBlank()) {
            s.dropOffStop = cleanStopName(req.dropOffStop());
        }

        // Morning Bus
        if (req.busId() != null) {
            s.busId = req.busId();
            s.morningBusId = req.busId();
        } else if (req.morningBusId() != null) {
            s.busId = req.morningBusId();
            s.morningBusId = req.morningBusId();
        }
        if (req.morningBusNumber() != null && !req.morningBusNumber().isBlank()) {
            s.morningBusNumber = req.morningBusNumber().trim();
        }
        if (req.morningBusName() != null && !req.morningBusName().isBlank()) {
            s.morningBusName = req.morningBusName().trim();
        }
        if (req.morningDriver() != null && !req.morningDriver().isBlank()) {
            s.morningDriver = req.morningDriver().trim();
        }
        if (req.morningPhone() != null && !req.morningPhone().isBlank()) {
            s.morningPhone = req.morningPhone().trim();
        }

        // Return Bus
        if (req.returnBusId() != null) {
            s.returnBusId = req.returnBusId();
        }
        if (req.returnBusNumber() != null && !req.returnBusNumber().isBlank()) {
            s.returnBusNumber = req.returnBusNumber().trim();
        }
        if (req.returnBusName() != null && !req.returnBusName().isBlank()) {
            s.returnBusName = req.returnBusName().trim();
        }
        if (req.returnDriver() != null && !req.returnDriver().isBlank()) {
            s.returnDriver = req.returnDriver().trim();
        }
        if (req.returnPhone() != null && !req.returnPhone().isBlank()) {
            s.returnPhone = req.returnPhone().trim();
        }

        // Ensure default IDs if still missing
        if (s.routeId == null) {
            s.routeId = 1L;
        }
        if (s.busId == null) {
            s.busId = s.morningBusId != null ? s.morningBusId : 1L;
        }
        if (s.morningBusId == null) {
            s.morningBusId = s.busId;
        }

        // Parse or resolve stop times
        if (req.pickupTime() != null && !req.pickupTime().isBlank()) {
            s.pickupTime = parseTimeSafe(req.pickupTime());
        }
        if (req.expectedArrival() != null && !req.expectedArrival().isBlank()) {
            s.expectedArrival = parseTimeSafe(req.expectedArrival());
        }

        if (s.routeId != null) {
            List<RouteStop> stops = routeStops.findByRouteIdOrderByStopSequenceAsc(s.routeId);
            if (s.pickupTime == null && s.pickupStop != null) {
                for (RouteStop rs : stops) {
                    if (s.pickupStop.equalsIgnoreCase(rs.stopName) || cleanStopName(s.pickupStop).equalsIgnoreCase(rs.stopName)) {
                        s.pickupTime = rs.pickupTime;
                        break;
                    }
                }
            }
            if (s.expectedArrival == null && s.dropOffStop != null) {
                for (RouteStop rs : stops) {
                    if (s.dropOffStop.equalsIgnoreCase(rs.stopName) || cleanStopName(s.dropOffStop).equalsIgnoreCase(rs.stopName)) {
                        s.expectedArrival = rs.dropOffTime;
                        break;
                    }
                }
            }
            if (s.pickupTime == null && !stops.isEmpty()) {
                s.pickupTime = stops.get(0).pickupTime;
            }
            if (s.expectedArrival == null && !stops.isEmpty()) {
                s.expectedArrival = stops.get(stops.size() - 1).dropOffTime;
            }
        }

        TransportSchedule updated = schedules.save(s);

        // Update student school if provided
        if (st != null) {
            if (s.destinationSchool != null && !s.destinationSchool.isBlank()) {
                st.schoolName = s.destinationSchool.trim();
                students.save(st);
            }
            if (st.parent != null) {
                notify(st.parent.id, studentId, "SCHEDULE_UPDATED", "Transport Stops Updated",
                        "Assigned route: " + (s.routeName != null ? s.routeName : "") + ", destination: " + s.destinationSchool + ", pickup: " + s.pickupStop + ", drop-off: " + s.dropOffStop);
            }
        }

        return updated;
    }

    @Transactional
    public void deleteStudentSchedule(Long studentId) {
        schedules.findByStudentId(studentId).ifPresent(s -> {
            schedules.delete(s);
            Student st = students.findById(studentId).orElse(null);
            if (st != null && st.parent != null) {
                notify(st.parent.id, studentId, "SCHEDULE_DELETED", "Transport Assignment Removed",
                        "Transport assignment for " + st.fullName + " has been deleted.");
            }
        });
    }

    public Map<String, Object> eta(Long studentId) {
        TransportSchedule s = schedules.findByStudentId(studentId).orElse(null);
        if (s == null || s.busId == null) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("etaMinutes", 10);
            r.put("nextStop", "Approaching Stop");
            r.put("lastUpdated", LocalDateTime.now());
            r.put("tripMode", "MORNING");
            return r;
        }

        BusLocation l = locations.findTopByBusIdOrderByRecordedAtDesc(s.busId).orElse(null);
        if (l == null) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("etaMinutes", 10);
            r.put("nextStop", s.pickupStop != null ? s.pickupStop : "Approaching Stop");
            r.put("lastUpdated", LocalDateTime.now());
            r.put("tripMode", "MORNING");
            return r;
        }

        String mode = mockGps.getTripMode(s.busId);
        Long activeRouteId = "RETURN".equalsIgnoreCase(mode) ? 2L : 1L;

        List<RouteStop> stops = routeStops.findByRouteIdOrderByStopSequenceAsc(activeRouteId);
        RouteStop targetStop = null;
        String desiredStop = "RETURN".equalsIgnoreCase(mode) ? s.dropOffStop : s.pickupStop;

        if (desiredStop != null) {
            for (RouteStop st : stops) {
                if (desiredStop.equalsIgnoreCase(st.stopName)) {
                    targetStop = st;
                    break;
                }
            }
        }
        if (targetStop == null && !stops.isEmpty()) {
            targetStop = stops.get(stops.size() - 1);
        }

        String nextStopName = targetStop != null ? targetStop.stopName : "SLIIT Campus Main Gate";
        int etaMinutes = 8;
        if (targetStop != null) {
            double distMeters = GeoUtil.metres(l.latitude, l.longitude, targetStop.latitude, targetStop.longitude);
            etaMinutes = Math.max(1, (int) Math.round(distMeters / 500.0));
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("etaMinutes", etaMinutes);
        r.put("nextStop", nextStopName);
        r.put("lastUpdated", l.recordedAt);
        r.put("tripMode", mode);
        return r;
    }

    public Map<String, Object> tracking(Long studentId) {
        Student st = students.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        TransportSchedule s = schedules.findByStudentId(studentId).orElse(null);
        if (s == null || s.busId == null) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("studentId", st.id);
            r.put("studentName", st.fullName);
            r.put("transportStatus", st.transportStatus);
            r.put("busId", 1L);
            r.put("busRegistration", "NB-1234");
            r.put("busName", "School Bus 01");
            r.put("driverName", "Kasun Fernando");
            r.put("driverPhone", "0779876543");
            r.put("busStatus", "IDLE");
            r.put("latitude", 6.9147);
            r.put("longitude", 79.9729);
            r.put("speed", 0);
            r.put("heading", 0);
            r.put("gpsStatus", "ACTIVE");
            r.put("recordedAt", LocalDateTime.now());
            r.put("tripMode", "MORNING");
            r.put("direction", "Kottawa ➔ SLIIT (Pickup)");
            r.put("routeId", 1L);
            r.put("routeName", "Kottawa to SLIIT (Morning Pickup)");
            r.put("pickupStopName", null);
            r.put("dropOffStopName", null);
            r.put("etaMinutes", 10);
            r.put("nextStopName", "Pickup Stop");
            r.put("stops", Collections.emptyList());
            return r;
        }

        SchoolBus b = buses.findById(s.busId).orElse(null);
        BusLocation l = locations.findTopByBusIdOrderByRecordedAtDesc(s.busId).orElse(null);
        if (b == null || l == null) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("studentId", st.id);
            r.put("studentName", st.fullName);
            r.put("transportStatus", st.transportStatus);
            r.put("busId", s.busId);
            r.put("busRegistration", s.morningBusNumber != null ? s.morningBusNumber : "NB-1234");
            r.put("busName", s.morningBusName != null ? s.morningBusName : "School Bus");
            r.put("driverName", s.morningDriver != null ? s.morningDriver : "Driver");
            r.put("driverPhone", s.morningPhone != null ? s.morningPhone : "0770000000");
            r.put("busStatus", "ON ROUTE");
            r.put("latitude", 6.9147);
            r.put("longitude", 79.9729);
            r.put("speed", 0);
            r.put("heading", 0);
            r.put("gpsStatus", "ACTIVE");
            r.put("recordedAt", LocalDateTime.now());
            r.put("tripMode", "MORNING");
            r.put("direction", "Kottawa ➔ SLIIT (Pickup)");
            r.put("routeId", s.routeId != null ? s.routeId : 1L);
            r.put("routeName", s.routeName != null ? s.routeName : "School Bus Route");
            r.put("pickupStopName", s.pickupStop);
            r.put("dropOffStopName", s.dropOffStop);
            r.put("etaMinutes", 10);
            r.put("nextStopName", s.pickupStop != null ? s.pickupStop : "Pickup Stop");
            r.put("stops", Collections.emptyList());
            return r;
        }

        String mode = mockGps.getTripMode(s.busId);
        Long activeRouteId = "RETURN".equalsIgnoreCase(mode) ? 2L : 1L;

        List<RouteStop> stopEntities = routeStops.findByRouteIdOrderByStopSequenceAsc(activeRouteId);
        List<Map<String, Object>> formattedStops = new ArrayList<>();

        RouteStop nearestNextStop = null;
        double minDistance = Double.MAX_VALUE;

        for (RouteStop rs : stopEntities) {
            Map<String, Object> stopMap = new LinkedHashMap<>();
            stopMap.put("id", rs.id);
            stopMap.put("stopName", rs.stopName);
            stopMap.put("latitude", rs.latitude);
            stopMap.put("longitude", rs.longitude);
            stopMap.put("stopSequence", rs.stopSequence);
            stopMap.put("pickupTime", rs.pickupTime != null ? rs.pickupTime.toString() : "");
            stopMap.put("dropOffTime", rs.dropOffTime != null ? rs.dropOffTime.toString() : "");

            boolean isPickup = s.pickupStop != null && s.pickupStop.equalsIgnoreCase(rs.stopName);
            boolean isDropOff = s.dropOffStop != null && s.dropOffStop.equalsIgnoreCase(rs.stopName);
            boolean isTerminal = rs.stopSequence == stopEntities.size();

            stopMap.put("isPickupStop", isPickup);
            stopMap.put("isDropOffStop", isDropOff);
            stopMap.put("isTerminal", isTerminal);

            double dist = GeoUtil.metres(l.latitude, l.longitude, rs.latitude, rs.longitude);
            stopMap.put("distanceMetres", Math.round(dist));

            if (dist < minDistance) {
                minDistance = dist;
                nearestNextStop = rs;
            }

            formattedStops.add(stopMap);
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("studentId", st.id);
        r.put("studentName", st.fullName);
        r.put("transportStatus", st.transportStatus);

        r.put("busId", b.id);
        r.put("busRegistration", b.registrationNumber);
        r.put("busName", b.busName);
        r.put("driverName", b.driverName);
        r.put("driverPhone", b.driverPhone);
        r.put("busStatus", b.currentStatus);

        r.put("latitude", l.latitude);
        r.put("longitude", l.longitude);
        r.put("speed", l.speed);
        r.put("heading", l.heading);
        r.put("gpsStatus", l.gpsStatus);
        r.put("recordedAt", l.recordedAt);

        r.put("tripMode", mode);
        r.put("direction", "RETURN".equalsIgnoreCase(mode) ? "SLIIT ➔ Kottawa (Drop-off)" : "Kottawa ➔ SLIIT (Pickup)");
        r.put("routeId", activeRouteId);
        r.put("routeName", activeRouteId == 1L ? "Kottawa to SLIIT (Morning Pickup)" : "SLIIT to Kottawa (Afternoon Drop-off)");
        r.put("pickupStopName", s.pickupStop);
        r.put("dropOffStopName", s.dropOffStop);

        r.put("stops", formattedStops);
        r.put("waypoints", pathService.getPathForRoute(activeRouteId));

        Map<String, Object> etaData = eta(studentId);
        r.put("etaMinutes", etaData.get("etaMinutes"));
        r.put("nextStop", nearestNextStop != null ? nearestNextStop.stopName : etaData.get("nextStop"));

        return r;
    }

    public BusLocation addLocation(Long busId, LocationRequest r) {
        SchoolBus b = buses.findById(busId).orElseThrow(() -> new NotFoundException("Bus not found"));
        BusLocation l = new BusLocation();
        l.busId = busId;
        l.latitude = r.latitude();
        l.longitude = r.longitude();
        l.speed = r.speed() == null ? 0 : r.speed();
        l.heading = r.heading() == null ? 0 : r.heading();
        l.gpsStatus = r.gpsStatus() == null ? "DEMO GPS" : r.gpsStatus();
        l.recordedAt = LocalDateTime.now();
        b.currentStatus = "ON ROUTE";
        buses.save(b);
        locations.save(l);
        return l;
    }

    public void notify(Long parent, Long student, String type, String title, String msg) {
        Notification n = new Notification();
        n.parentId = parent;
        n.studentId = student;
        n.type = type;
        n.title = title;
        n.message = msg;
        n.createdAt = LocalDateTime.now();
        n.readStatus = false;
        notifications.save(n);
    }

    public StudentArrival arrival(Long studentId) {
        return arrivals.findTopByStudentIdOrderByArrivalTimeDesc(studentId)
                .orElseThrow(() -> new NotFoundException("Arrival not yet confirmed"));
    }

    private String cleanStopName(String s) {
        if (s == null) return null;
        String cleaned = s.trim();
        cleaned = cleaned.replaceFirst("^#\\d+\\.\\s*", "");
        cleaned = cleaned.replaceFirst("\\s*\\(\\d{1,2}:\\d{2}.*?\\)$", "");
        return cleaned.trim();
    }

    private LocalTime parseTimeSafe(String t) {
        if (t == null || t.isBlank()) return null;
        String trimmed = t.trim();
        try {
            if (trimmed.length() == 5 && trimmed.charAt(2) == ':') {
                return LocalTime.parse(trimmed);
            }
            if (trimmed.length() == 8 && trimmed.charAt(2) == ':' && trimmed.charAt(5) == ':') {
                return LocalTime.parse(trimmed);
            }
            if (trimmed.toUpperCase().contains("AM") || trimmed.toUpperCase().contains("PM")) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
                return LocalTime.parse(trimmed.toUpperCase(), fmt);
            }
            return LocalTime.parse(trimmed);
        } catch (Exception e) {
            return null;
        }
    }
}