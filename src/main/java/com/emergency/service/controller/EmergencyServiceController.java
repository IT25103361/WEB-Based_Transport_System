package com.emergency.service.controller;

import com.emergency.service.model.EmergencyService;
import com.emergency.service.repository.EmergencyServiceRepository;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class EmergencyServiceController {

    private final EmergencyServiceRepository repository;


    public EmergencyServiceController(EmergencyServiceRepository repository) {
        this.repository = repository;
    }


    // =========================
    // READ
    // =========================

    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute(
                "emergencyServices",
                repository.findAll()
        );

        model.addAttribute(
                "emergencyService",
                new EmergencyService()
        );

        return "index";
    }


    // =========================
    // CREATE
    // =========================

    @PostMapping("/emergency/add")
    public String addEmergencyService(

            @Valid
            @ModelAttribute("emergencyService")
            EmergencyService emergencyService,

            BindingResult bindingResult,

            Model model) {

        System.out.println("PHONE = " + emergencyService.getPhoneNumber());
        System.out.println("VALIDATION ERRORS = " + bindingResult.hasErrors());
        // If validation fails
        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "emergencyServices",
                    repository.findAll()
            );

            return "index";
        }


        // If validation succeeds
        repository.save(emergencyService);

        return "redirect:/";
    }


    // =========================
    // UPDATE - Open Edit Page
    // =========================

    @GetMapping("/emergency/edit/{id}")
    public String editEmergencyService(

            @PathVariable Long id,

            Model model) {


        EmergencyService emergencyService =
                repository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid emergency service ID: " + id
                                )
                        );


        model.addAttribute(
                "emergencyService",
                emergencyService
        );


        return "edit-emergency";
    }


    // =========================
    // UPDATE - Save
    // =========================

    @PostMapping("/emergency/update")
    public String updateEmergencyService(

            @Valid
            @ModelAttribute("emergencyService")
            EmergencyService emergencyService,

            BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {

            return "edit-emergency";
        }


        repository.save(emergencyService);

        return "redirect:/";
    }


    // =========================
    // DELETE
    // =========================

    @GetMapping("/emergency/delete/{id}")
    public String deleteEmergencyService(

            @PathVariable Long id) {


        repository.deleteById(id);

        return "redirect:/";
    }
}

