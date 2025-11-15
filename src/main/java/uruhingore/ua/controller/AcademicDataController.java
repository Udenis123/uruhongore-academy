package uruhingore.ua.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uruhingore.ua.model.AcademicData;
import uruhingore.ua.model.Period;
import uruhingore.ua.model.Trimester;
import uruhingore.ua.service.AcademicDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/academic-data")
@RequiredArgsConstructor
public class AcademicDataController {

    private final AcademicDataService academicDataService;

    /**
     * Get all published academic data (visible to students/parents)
     */
    @GetMapping("/published")
    public ResponseEntity<List<AcademicData>> getPublishedAcademicData() {
        log.info("Received request to get all published academic data");
        List<AcademicData> academicData = academicDataService.getAllPublished();
        return ResponseEntity.ok(academicData);
    }

    /**
     * Get all academic data (including unpublished) - for admin/head
     */
    @GetMapping
    public ResponseEntity<List<AcademicData>> getAllAcademicData() {
        log.info("Received request to get all academic data");
        List<AcademicData> academicData = academicDataService.getAll();
        return ResponseEntity.ok(academicData);
    }

    /**
     * Get academic data by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAcademicDataById(@PathVariable UUID id) {
        try {
            log.info("Received request to get academic data with ID: {}", id);
            AcademicData academicData = academicDataService.getAcademicDataById(id);
            return ResponseEntity.ok(academicData);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.NOT_FOUND.value());
            errorBody.put("error", "Not Found");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        }
    }

    /**
     * Create or get academic data
     */
    @PostMapping
    public ResponseEntity<?> createOrGetAcademicData(
            @RequestParam Trimester trimester,
            @RequestParam Integer academicYear,
            @RequestParam Period period) {
        try {
            log.info("Received request to create/get academic data: Trimester={}, Year={}, Period={}", 
                    trimester, academicYear, period);
            AcademicData academicData = academicDataService.getOrCreateAcademicData(trimester, academicYear, period);
            return ResponseEntity.status(HttpStatus.CREATED).body(academicData);
        } catch (Exception e) {
            log.error("Error creating/getting academic data: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    /**
     * Publish academic data (makes reports visible)
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<?> publishAcademicData(@PathVariable UUID id) {
        try {
            log.info("Received request to publish academic data: {}", id);
            AcademicData academicData = academicDataService.publishAcademicData(id);
            return ResponseEntity.ok(academicData);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.NOT_FOUND.value());
            errorBody.put("error", "Not Found");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        } catch (Exception e) {
            log.error("Error publishing academic data: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    /**
     * Unpublish academic data (hides reports)
     */
    @PutMapping("/{id}/unpublish")
    public ResponseEntity<?> unpublishAcademicData(@PathVariable UUID id) {
        try {
            log.info("Received request to unpublish academic data: {}", id);
            AcademicData academicData = academicDataService.unpublishAcademicData(id);
            return ResponseEntity.ok(academicData);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.NOT_FOUND.value());
            errorBody.put("error", "Not Found");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        } catch (Exception e) {
            log.error("Error unpublishing academic data: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }
}

