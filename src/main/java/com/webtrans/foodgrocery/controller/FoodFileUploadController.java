package com.webtrans.foodgrocery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/food/upload")
public class FoodFileUploadController {

    private static final String PRIMARY_UPLOAD_DIR = "src/main/resources/static/foodgrocery/uploads/";
    private static final String FALLBACK_UPLOAD_DIR = "uploads/food/";

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
            File primaryDir = new File(PRIMARY_UPLOAD_DIR);
            if (!primaryDir.exists()) {
                primaryDir.mkdirs();
            }

            File fallbackDir = new File(FALLBACK_UPLOAD_DIR);
            if (!fallbackDir.exists()) {
                fallbackDir.mkdirs();
            }

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null && originalFileName.contains(".") 
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")) 
                    : "";
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            // Save to primary directory (static folder)
            Path primaryPath = Paths.get(PRIMARY_UPLOAD_DIR + uniqueFileName);
            Files.write(primaryPath, file.getBytes());

            // Also save to fallback location for runtime stability
            Path fallbackPath = Paths.get(FALLBACK_UPLOAD_DIR + uniqueFileName);
            Files.write(fallbackPath, file.getBytes());

            // Return Food uploads URL
            Map<String, String> response = new HashMap<>();
            response.put("url", "/food/uploads/" + uniqueFileName);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload file: " + e.getMessage());
        }
    }
}
