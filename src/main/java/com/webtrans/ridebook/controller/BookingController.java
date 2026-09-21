package com.webtrans.ridebook.controller;

import com.webtrans.ridebook.model.Booking;
import com.webtrans.ridebook.model.Driver;
import com.webtrans.ridebook.repository.BookingRepository;
import com.webtrans.ridebook.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ride")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DriverRepository driverRepository;

    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }

    @GetMapping("/ride-hub")
    public String rideHub() {
        return "ride-hub";
    }

    @GetMapping("/passenger")
    public String passengerView(Model model) {
        model.addAttribute("booking", new Booking());
        List<Booking> bookings = bookingRepository.findAll();
        model.addAttribute("bookings", bookings != null ? bookings : new ArrayList<>());
        return "passenger";
    }

    @PostMapping("/book")
    public String createBooking(@ModelAttribute Booking booking) {
        booking.setStatus("PENDING");
        booking.setDriver(null);
        bookingRepository.save(booking);
        return "redirect:/ride/passenger";
    }

    // Passenger cancels the booking
    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id) {
        try {
            Booking booking = bookingRepository.findById(id).orElse(null);
            if (booking != null && ("PENDING".equalsIgnoreCase(booking.getStatus()) || "ACCEPTED".equalsIgnoreCase(booking.getStatus()))) {
                // Driver kenek already accept karala nam, e driverwa aye AVAILABLE karannoni
                Driver driver = booking.getDriver();
                if (driver != null) {
                    driver.setStatus("AVAILABLE");
                    driverRepository.save(driver);
                }

                // Status eka CANCELLED kiyala update karanawa (Anuwa delete karannath puluwan)
                booking.setStatus("CANCELLED");
                booking.setDriver(null);
                bookingRepository.save(booking);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ride/passenger";
    }

    @GetMapping("/driver")
    public String driverView(Model model) {
        List<Booking> bookings = bookingRepository.findAll();
        List<Driver> drivers = driverRepository.findAll();

        model.addAttribute("bookings", bookings != null ? bookings : new ArrayList<>());
        model.addAttribute("drivers", drivers != null ? drivers : new ArrayList<>());
        return "driver";
    }

    @PostMapping("/driver/register")
    public String registerDriver(@ModelAttribute Driver driver) {
        if (driver.getStatus() == null || driver.getStatus().trim().isEmpty()) {
            driver.setStatus("AVAILABLE");
        }
        driverRepository.save(driver);
        return "redirect:/ride/driver";
    }

    @GetMapping("/driver/toggle/{id}")
    public String toggleDriverStatus(@PathVariable Long id) {
        try {
            Driver driver = driverRepository.findById(id).orElse(null);
            if (driver != null) {
                if ("AVAILABLE".equals(driver.getStatus())) {
                    driver.setStatus("BUSY");
                } else {
                    driver.setStatus("AVAILABLE");
                }
                driverRepository.save(driver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ride/driver";
    }

    @PostMapping("/accept/{id}")
    public String acceptRide(@PathVariable Long id, @RequestParam(name = "driverId", required = false) String driverIdStr) {
        if (driverIdStr == null || driverIdStr.trim().isEmpty()) {
            return "redirect:/ride/driver";
        }

        try {
            Long driverId = Long.parseLong(driverIdStr.trim());
            Booking booking = bookingRepository.findById(id).orElse(null);
            Driver driver = driverRepository.findById(driverId).orElse(null);

            if (booking != null && driver != null && booking.getStatus() != null && "PENDING".equalsIgnoreCase(booking.getStatus().trim())) {
                booking.setDriver(driver);
                booking.setStatus("ACCEPTED");
                bookingRepository.save(booking);

                driver.setStatus("BUSY");
                driverRepository.save(driver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/ride/driver";
    }

    @GetMapping("/complete/{id}")
    public String completeRide(@PathVariable Long id) {
        try {
            Booking booking = bookingRepository.findById(id).orElse(null);
            if (booking != null) {
                booking.setStatus("COMPLETED");
                bookingRepository.save(booking);

                Driver driver = booking.getDriver();
                if (driver != null) {
                    driver.setStatus("AVAILABLE");
                    driverRepository.save(driver);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ride/driver";
    }

    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable Long id) {
        try {
            bookingRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ride/driver";
    }
}