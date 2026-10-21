package com.webtrans.vehicle_rental.controller;

import com.webtrans.auth.model.User;
import com.webtrans.vehicle_rental.dto.RentalExtensionQuoteDto;
import com.webtrans.vehicle_rental.dto.RentalQuoteDto;
import com.webtrans.vehicle_rental.model.RentalBooking;
import com.webtrans.vehicle_rental.model.RentalVehicle;
import com.webtrans.vehicle_rental.repository.RentalBookingRepository;
import com.webtrans.vehicle_rental.repository.RentalVehicleRepository;
import com.webtrans.vehicle_rental.service.RentalAvailabilityService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/rental")
public class RentalController {

    @Autowired
    private RentalVehicleRepository vehicleRepository;

    @Autowired
    private RentalBookingRepository bookingRepository;

    @Autowired
    private RentalAvailabilityService availabilityService;

    @GetMapping("/index")
    public String index(Model model, HttpSession session, HttpServletResponse response,
                        @RequestParam(name = "filterStart", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate filterStart,
                        @RequestParam(name = "filterEnd", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate filterEnd) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return "redirect:/";
        }

        // Prevent browser caching across logout/account switches
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        List<RentalVehicle> allVehicles = vehicleRepository.findAllByOrderByIdAsc();
        Map<Long, Boolean> availabilityMap = new HashMap<>();

        LocalDate checkStart = (filterStart != null) ? filterStart : LocalDate.now();
        LocalDate checkEnd = (filterEnd != null && !filterEnd.isBefore(checkStart)) ? filterEnd : checkStart;

        for (RentalVehicle v : allVehicles) {
            RentalAvailabilityService.AvailabilityResult res = availabilityService.checkVehicleAvailability(
                    v.getId(), checkStart, checkEnd, null);
            availabilityMap.put(v.getId(), res.isAvailable());
        }

        RentalBooking booking = new RentalBooking();
        booking.setCustomerName(loggedUser.getFullName());

        // Strictly isolate bookings to authenticated customer
        List<RentalBooking> myBookings = bookingRepository.findByCustomerUser_IdOrderByIdDesc(loggedUser.getId());

        model.addAttribute("vehicles", allVehicles);
        model.addAttribute("availabilityMap", availabilityMap);
        model.addAttribute("filterStart", filterStart);
        model.addAttribute("filterEnd", filterEnd);
        model.addAttribute("bookings", myBookings != null ? myBookings : new ArrayList<>());
        model.addAttribute("booking", booking);

        return "rental/index";
    }

    @GetMapping("/api/quote")
    @ResponseBody
    public ResponseEntity<RentalQuoteDto> getQuote(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        RentalQuoteDto quote = availabilityService.calculateQuote(vehicleId, startDate, endDate);
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/api/vehicle/{id}/booked-dates")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> getBookedDates(@PathVariable("id") Long id) {
        List<Map<String, String>> ranges = availabilityService.getBookedRangesForVehicle(id);
        return ResponseEntity.ok(ranges);
    }

    @GetMapping("/api/booking/{id}/extend-preview")
    @ResponseBody
    public ResponseEntity<?> previewExtension(
            @PathVariable("id") Long id,
            @RequestParam("newEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate,
            HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required."));
        }
        try {
            RentalExtensionQuoteDto preview = availabilityService.previewExtension(id, loggedUser, newEndDate);
            return ResponseEntity.ok(preview);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/book")
    public String bookVehicle(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam(name = "customerName", required = false) String customerName,
            @RequestParam(name = "customerPhone", required = false) String customerPhone,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return "redirect:/";
        }

        try {
            RentalBooking saved = availabilityService.createBookingAtomic(
                    vehicleId, loggedUser, customerName, customerPhone, startDate, endDate);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Vehicle \"" + saved.getVehicle().getName() + "\" reserved successfully! (" +
                    saved.getDays() + " days, LKR " + String.format("%.2f", saved.getTotalCost()) + ").");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            // Retain entered details so customer doesn't lose their form state
            redirectAttributes.addFlashAttribute("retainedVehicleId", vehicleId);
            redirectAttributes.addFlashAttribute("retainedStartDate", startDate);
            redirectAttributes.addFlashAttribute("retainedEndDate", endDate);
            redirectAttributes.addFlashAttribute("retainedPhone", customerPhone);
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to complete reservation. Please try again.");
        }
        return "redirect:/rental/index";
    }

    @PostMapping("/booking/{id}/extend")
    public String extendBooking(
            @PathVariable("id") Long id,
            @RequestParam("newEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return "redirect:/";
        }

        try {
            RentalBooking extended = availabilityService.extendBookingAtomic(id, loggedUser, newEndDate);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Rental #" + extended.getId() + " extended successfully until " + newEndDate +
                    " (Additional: LKR " + String.format("%.2f", extended.getExtensionCost()) + ").");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to extend rental.");
        }
        return "redirect:/rental/index";
    }

    @GetMapping("/admin")
    public String adminView(Model model) {
        model.addAttribute("vehicles", vehicleRepository.findAllByOrderByIdAsc());
        model.addAttribute("bookings", bookingRepository.findAllByOrderByIdDesc());
        return "rental/admin";
    }

    @PostMapping("/vehicle/add")
    public String addVehicle(@ModelAttribute RentalVehicle vehicle) {
        if (vehicle.getStatus() == null || vehicle.getStatus().isEmpty()) {
            vehicle.setStatus("AVAILABLE");
        }
        if (vehicle.getImageUrl() == null || vehicle.getImageUrl().trim().isEmpty()) {
            String type = vehicle.getType() != null ? vehicle.getType().trim().toLowerCase() : "car";
            vehicle.setImageUrl("/rental/images/" + type + ".svg");
        }
        vehicleRepository.save(vehicle);
        return "redirect:/rental/admin";
    }

    @GetMapping("/vehicle/delete/{id}")
    public String deleteVehicle(@PathVariable Long id) {
        vehicleRepository.deleteById(id);
        return "redirect:/rental/admin";
    }

    @GetMapping("/booking/status/{id}")
    public String updateBookingStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(name = "src", defaultValue = "admin") String src,
            HttpSession session,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) throws IOException {
        User loggedUser = (User) session.getAttribute("loggedUser");
        RentalBooking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            if ("index".equals(src)) {
                redirectAttributes.addFlashAttribute("errorMsg", "Booking not found.");
                return "redirect:/rental/index";
            }
            return "redirect:/rental/admin";
        }

        if ("index".equals(src)) {
            // Customer cancellation
            if (loggedUser == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
                return null;
            }
            if (booking.getCustomerUser() == null || !booking.getCustomerUser().getId().equals(loggedUser.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: You do not own this booking.");
                return null;
            }
            // Customer can only cancel PENDING or CONFIRMED bookings
            if ("CANCELLED".equalsIgnoreCase(status)) {
                if ("PENDING".equalsIgnoreCase(booking.getStatus()) || "CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                    booking.setStatus("CANCELLED");
                    bookingRepository.save(booking);
                    redirectAttributes.addFlashAttribute("successMsg", "Booking #" + id + " has been cancelled.");
                } else {
                    redirectAttributes.addFlashAttribute("errorMsg", "This booking cannot be cancelled in status: " + booking.getStatus());
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized status change.");
                return null;
            }
            return "redirect:/rental/index";
        }

        // Admin status update
        booking.setStatus(status);
        bookingRepository.save(booking);

        return "redirect:/rental/admin";
    }
}
