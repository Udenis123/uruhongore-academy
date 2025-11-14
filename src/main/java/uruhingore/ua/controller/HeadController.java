package uruhingore.ua.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uruhingore.ua.model.Module;
import uruhingore.ua.model.Student;
import uruhingore.ua.model.Users;
import uruhingore.ua.repository.ModuleRepository;
import uruhingore.ua.repository.StudentRepository;
import uruhingore.ua.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/head")
@RequiredArgsConstructor
public class HeadController {

    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    // MODULES
//    @PostMapping("/modules")
//    public ResponseEntity<?> createModule(@RequestBody Module module) {
//        moduleRepository.save(module);
//        return ResponseEntity.ok("Module saved successfully");
//    }
//
//    @GetMapping("/modules")
//    public List<Module> getAllModules() {
//        return moduleRepository.findAll();
//    }
//
//
//    // (parent-student linking is handled by StudentController/StudentService)
//    // NOTE: Link/unlink endpoints were removed to centralize parent-student management
//
//    // GET PARENT'S CHILDREN (Students)
//    @GetMapping("/parent/{parentId}/children")
//    public ResponseEntity<?> getParentChildren(@PathVariable UUID parentId) {
//        Users parent = userRepository.findById(parentId)
//                .orElseThrow(() -> new IllegalArgumentException("Parent not found"));
//
//        if (!parent.isParent()) {
//            return ResponseEntity.badRequest().body("User is not a parent");
//        }
//
//        // Get students linked to this parent
//        List<Student> children = studentRepository.findByParentId(parentId);
//        return ResponseEntity.ok(children);
//    }
//
//    // GET ALL PARENTS
//    @GetMapping("/parents")
//    public ResponseEntity<?> getAllParents() {
//        List<Users> parents = userRepository.findAll().stream()
//                .filter(Users::isParent)
//                .toList();
//        return ResponseEntity.ok(parents);
//    }
//
//    // GET ALL STUDENTS
//    @GetMapping("/students")
//    public ResponseEntity<?> getAllStudents() {
//        List<Student> students = studentRepository.findAll();
//        return ResponseEntity.ok(students);
//    }
//
//    // GET ALL USERS (with roles)
//    @GetMapping("/users")
//    public ResponseEntity<?> getAllUsers() {
//        return ResponseEntity.ok(userRepository.findAll());
//    }
//
//    // GET USER BY ID
//    @GetMapping("/users/{userId}")
//    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
//        Users user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//        return ResponseEntity.ok(user);
//    }
}
