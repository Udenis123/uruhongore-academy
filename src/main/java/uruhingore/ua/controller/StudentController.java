package uruhingore.ua.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uruhingore.ua.dto.StudentRequest;
import uruhingore.ua.dto.StudentResponse;
import uruhingore.ua.model.ClassLevel;
import uruhingore.ua.service.StudentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    private final ObjectMapper objectMapper;
    private final Validator validator;


    /**
     * Create a new student with parents and modules
     * Accepts multipart/form-data with optional profile photo
     * Form data should contain:
     * - request: JSON string containing StudentRequest data
     * - file: (optional) profile photo image file
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createStudentWithPhoto(
            @RequestParam(value = "request") String requestJson,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            log.info("Received multipart request to create student with photo: {}", file != null && !file.isEmpty());
            
            // Parse JSON string to StudentRequest
            StudentRequest request;
            try {
                request = objectMapper.readValue(requestJson, StudentRequest.class);
            } catch (Exception e) {
                log.error("Failed to parse request JSON: {}", e.getMessage());
                Map<String, Object> errorBody = new HashMap<>();
                errorBody.put("timestamp", java.time.LocalDateTime.now());
                errorBody.put("status", HttpStatus.BAD_REQUEST.value());
                errorBody.put("error", "Bad Request");
                errorBody.put("message", "Invalid JSON format in request field");
                errorBody.put("details", e.getMessage());
                return ResponseEntity.badRequest().body(errorBody);
            }
            
            // Validate the request using Spring Validator
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "studentRequest");
            validator.validate(request, bindingResult);
            if (bindingResult.hasErrors()) {
                log.error("Validation errors: {}", bindingResult.getAllErrors());
                Map<String, Object> errorBody = new HashMap<>();
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error -> {
                    errors.put(error.getField(), error.getDefaultMessage());
                });
                errorBody.put("timestamp", java.time.LocalDateTime.now());
                errorBody.put("status", HttpStatus.BAD_REQUEST.value());
                errorBody.put("error", "Validation Failed");
                errorBody.put("message", "Request validation failed. Please check the errors below.");
                errorBody.put("errors", errors);
                return ResponseEntity.badRequest().body(errorBody);
            }
            
            log.info("Creating student: {} {} with photo: {}", 
                    request.getFirstName(), request.getLastName(), file != null && !file.isEmpty());
            
            StudentResponse response = studentService.createStudent(request, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.BAD_REQUEST.value());
            errorBody.put("error", "Bad Request");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorBody);
        } catch (Exception e) {
            log.error("Error creating student: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred while creating the student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }
    
    /**
     * Get all students
     */
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        log.info("Received request to get all students");
        List<StudentResponse> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }
    
    /**
     * Get student by ID
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable UUID studentId) {
        log.info("Received request to get student with ID: {}", studentId);
        StudentResponse student = studentService.getStudentById(studentId);
        return ResponseEntity.ok(student);
    }
    
    /**
     * Get all parents of a student by student ID
     */
    @GetMapping("/{studentId}/parents")
    public ResponseEntity<List<StudentResponse.ParentInfo>> getStudentParents(@PathVariable UUID studentId) {
        log.info("Received request to get parents for student: {}", studentId);
        List<StudentResponse.ParentInfo> parents = studentService.getStudentParents(studentId);
        return ResponseEntity.ok(parents);
    }
    
    /**
     * Get all students by parent ID
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<StudentResponse>> getStudentsByParent(@PathVariable UUID parentId) {
        log.info("Received request to get students for parent: {}", parentId);
        List<StudentResponse> students = studentService.getStudentsByParent(parentId);
        return ResponseEntity.ok(students);
    }
    
    /**
     * Get students by class level
     */
    @GetMapping("/class/{classLevel}")
    public ResponseEntity<List<StudentResponse>> getStudentsByClassLevel(@PathVariable ClassLevel classLevel) {
        log.info("Received request to get students for class level: {}", classLevel);
        List<StudentResponse> students = studentService.getStudentsByClassLevel(classLevel);
        return ResponseEntity.ok(students);
    }
    
    /**
     * Assign a parent to a student
     */
    @PostMapping("/{studentId}/parents/{parentId}")
    public ResponseEntity<StudentResponse> assignParent(
            @PathVariable UUID studentId,
            @PathVariable UUID parentId) {
        log.info("Received request to assign parent {} to student {}", parentId, studentId);
        StudentResponse response = studentService.assignParent(studentId, parentId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enroll a student in a module
     */
    @PostMapping("/{studentId}/modules/{moduleId}")
    public ResponseEntity<StudentResponse> enrollModule(
            @PathVariable UUID studentId,
            @PathVariable UUID moduleId) {
        log.info("Received request to enroll student {} in module {}", studentId, moduleId);
        StudentResponse response = studentService.enrollModule(studentId, moduleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload profile photo for a student
     */
    @PostMapping(value = "/{studentId}/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable UUID studentId,
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Received request to upload profile photo for student: {}", studentId);
            StudentResponse response = studentService.uploadProfilePhoto(studentId, file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.BAD_REQUEST.value());
            errorBody.put("error", "Bad Request");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorBody);
        } catch (Exception e) {
            log.error("Error uploading profile photo: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred while uploading the profile photo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    /**
     * Delete profile photo for a student
     */
    @DeleteMapping("/{studentId}/profile-photo")
    public ResponseEntity<?> deleteProfilePhoto(@PathVariable UUID studentId) {
        try {
            log.info("Received request to delete profile photo for student: {}", studentId);
            StudentResponse response = studentService.deleteProfilePhoto(studentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.BAD_REQUEST.value());
            errorBody.put("error", "Bad Request");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorBody);
        } catch (Exception e) {
            log.error("Error deleting profile photo: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred while deleting the profile photo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }
}
