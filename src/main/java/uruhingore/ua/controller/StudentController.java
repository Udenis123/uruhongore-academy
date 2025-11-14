package uruhingore.ua.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uruhingore.ua.dto.StudentRequest;
import uruhingore.ua.dto.StudentResponse;
import uruhingore.ua.service.StudentService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    
    /**
     * Create a new student with parents and modules
     */
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@RequestBody @Valid StudentRequest request) {
        log.info("Received request to create student: {} {}", request.getFirstName(), request.getLastName());
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<List<StudentResponse>> getStudentsByClassLevel(@PathVariable String classLevel) {
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
}
