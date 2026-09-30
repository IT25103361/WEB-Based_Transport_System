package com.webtrans.school.controller;

import com.webtrans.school.dto.*;
import com.webtrans.school.exception.NotFoundException;
import com.webtrans.school.model.*;
import com.webtrans.school.repository.*;
import com.webtrans.school.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final TransportService svc;
    private final ParentRepository parents;
    private final StudentRepository students;
    private final SchoolBusRepository buses;
    private final TransportScheduleRepository schedules;
    private final BusLocationRepository locations;
    private final NotificationRepository notifications;
    private final StudentArrivalRepository arrivals;
    private final RouteStopRepository routeStops;
    private final BusRouteRepository routes;
    private final MockGpsService mockGps;

    public ApiController(
            TransportService s,
            ParentRepository p,
            StudentRepository st,
            SchoolBusRepository b,
            TransportScheduleRepository sc,
            BusLocationRepository l,
            NotificationRepository n,
            StudentArrivalRepository a,
            RouteStopRepository rs,
            BusRouteRepository rts,
            MockGpsService mg
    ) {
        this.svc = s;
        this.parents = p;
        this.students = st;
        this.buses = b;
        this.schedules = sc;
        this.locations = l;
        this.notifications = n;
        this.arrivals = a;
        this.routeStops = rs;
        this.routes = rts;
        this.mockGps = mg;
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest r) {
        Parent p = svc.login(r);
        return Map.of("token", "demo-session-" + p.id, "parentId", p.id, "parentName", p.fullName);
    }

    @GetMapping("/parents/{id}")
    public Parent parent(@PathVariable Long id) {
        return parents.findById(id).orElseThrow(() -> new NotFoundException("Parent not found"));
    }

    @GetMapping("/parents/{id}/students")
    public List<Student> students(@PathVariable Long id) {
        return students.findByParentId(id);
    }

    @GetMapping("/students/{id}")
    public Student student(@PathVariable Long id) {
        return svc.student(id);
    }

    @GetMapping("/students/{id}/transport")
    public Map<String, Object> transport(@PathVariable Long id) {
        return svc.transport(id);
    }

    @GetMapping("/buses")
    public List<SchoolBus> buses() {
        return buses.findAll();
    }

    @GetMapping("/buses/{id}")
    public SchoolBus bus(@PathVariable Long id) {
        return buses.findById(id).orElseThrow(() -> new NotFoundException("Bus not found"));
    }

    @GetMapping("/buses/{id}/location")
    public BusLocation loc(@PathVariable Long id) {
        return locations.findTopByBusIdOrderByRecordedAtDesc(id)
                .orElseThrow(() -> new NotFoundException("No GPS location available"));
    }

    @GetMapping("/buses/{id}/location/history")
    public List<BusLocation> hist(@PathVariable Long id) {
        return locations.findByBusIdOrderByRecordedAtDesc(id);
    }

    @PostMapping("/buses/{id}/location")
    public ResponseEntity<BusLocation> location(@PathVariable Long id, @Valid @RequestBody LocationRequest r) {
        return ResponseEntity.status(201).body(svc.addLocation(id, r));
    }

    @PostMapping("/buses/{id}/step")
    public ResponseEntity<BusLocation> step(@PathVariable Long id) {
        BusLocation loc = mockGps.advanceBus(id);
        return ResponseEntity.ok(loc);
    }

    @PostMapping("/buses/{id}/trip-mode")
    public ResponseEntity<Map<String, Object>> tripMode(@PathVariable Long id, @RequestParam(defaultValue = "AUTO") String direction) {
        mockGps.setTripMode(id, direction);
        return ResponseEntity.ok(Map.of("busId", id, "tripMode", mockGps.getTripMode(id)));
    }

    @PutMapping("/students/{id}/transport-stops")
    public ResponseEntity<TransportSchedule> updateStops(@PathVariable Long id, @RequestBody StopUpdateRequest req) {
        TransportSchedule updated = svc.updateStudentStops(id, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = {"/students/{id}/transport", "/students/{id}/transport-stops", "/students/{id}/transport-schedule"})
    public ResponseEntity<Void> deleteStudentTransport(@PathVariable Long id) {
        svc.deleteStudentSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/routes")
    public List<BusRoute> routes() {
        return routes.findAll();
    }

    @GetMapping("/routes/{id}/stops")
    public List<RouteStop> routeStops(@PathVariable Long id) {
        return routeStops.findByRouteIdOrderByStopSequenceAsc(id);
    }

    @GetMapping("/transport/track/{id}")
    public Map<String, Object> track(@PathVariable Long id) {
        return svc.tracking(id);
    }

    @GetMapping("/transport/eta/{id}")
    public Map<String, Object> eta(@PathVariable Long id) {
        return svc.eta(id);
    }

    @GetMapping("/schedules")
    public List<TransportSchedule> allSchedules() {
        return schedules.findAll();
    }

    @GetMapping("/schedules/{id}")
    public TransportSchedule schedule(@PathVariable Long id) {
        return schedules.findById(id).orElseThrow(() -> new NotFoundException("Schedule not found"));
    }

    @PostMapping("/schedules")
    public ResponseEntity<TransportSchedule> create(@RequestBody TransportSchedule s) {
        return ResponseEntity.status(201).body(schedules.save(s));
    }

    @PutMapping("/schedules/{id}")
    public TransportSchedule update(@PathVariable Long id, @RequestBody TransportSchedule s) {
        s.id = id;
        return schedules.save(s);
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        schedules.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parents/{id}/notifications")
    public List<Notification> ns(@PathVariable Long id) {
        return notifications.findByParentIdOrderByCreatedAtDesc(id);
    }

    @PostMapping("/parents/{id}/notifications")
    public ResponseEntity<Notification> createNotification(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        Long studentId = req.get("studentId") != null ? Long.valueOf(req.get("studentId").toString()) : 1L;
        String type = req.getOrDefault("type", "STOP_ARRIVAL").toString();
        String title = req.getOrDefault("title", "Bus Arrived").toString();
        String message = req.getOrDefault("message", "").toString();

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(2);
        List<Notification> recent = notifications.findByParentIdOrderByCreatedAtDesc(id);
        for (Notification existing : recent) {
            if (existing.createdAt != null && existing.createdAt.isAfter(cutoff)) {
                if (Objects.equals(existing.studentId, studentId)
                        && Objects.equals(existing.title, title)
                        && Objects.equals(existing.message, message)) {
                    return ResponseEntity.ok(existing);
                }
            } else {
                break;
            }
        }

        Notification n = new Notification();
        n.parentId = id;
        n.studentId = studentId;
        n.type = type;
        n.title = title;
        n.message = message;
        n.createdAt = LocalDateTime.now();
        n.readStatus = false;
        return ResponseEntity.status(201).body(notifications.save(n));
    }

    @GetMapping("/notifications/{id}")
    public Notification n(@PathVariable Long id) {
        return notifications.findById(id).orElseThrow(() -> new NotFoundException("Notification not found"));
    }

    @PutMapping("/notifications/{id}/read")
    public Notification read(@PathVariable Long id) {
        Notification n = n(id);
        n.readStatus = true;
        return notifications.save(n);
    }

    @PutMapping("/parents/{id}/notifications/read-all")
    public ResponseEntity<Void> readAll(@PathVariable Long id) {
        notifications.findByParentIdOrderByCreatedAtDesc(id).forEach(n -> {
            n.readStatus = true;
            notifications.save(n);
        });
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/parents/{id}/notifications")
    public ResponseEntity<Void> deleteAllForParent(@PathVariable Long id) {
        notifications.deleteByParentId(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notifications.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/students/{id}/arrival")
    public StudentArrival arrival(@PathVariable Long id) {
        return svc.arrival(id);
    }

    @PostMapping("/students/{id}/arrival")
    public StudentArrival arrive(@PathVariable Long id) {
        TransportSchedule s = schedules.findByStudentId(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));
        StudentArrival a = new StudentArrival();
        a.studentId = id;
        a.busId = s.busId;
        a.arrivalTime = LocalDateTime.now();
        a.eventType = "SAFE_ARRIVAL";
        a.confirmed = true;
        a.createdAt = LocalDateTime.now();
        arrivals.save(a);

        Student st = svc.student(id);
        svc.notify(st.parent.id, id, "SAFE_ARRIVAL", "Safe Arrival", st.fullName + " has arrived at " + st.schoolName + ".");
        return a;
    }
}