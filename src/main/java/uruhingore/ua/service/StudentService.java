package uruhingore.ua.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uruhingore.ua.dto.StudentRequest;
import uruhingore.ua.dto.StudentResponse;
import uruhingore.ua.model.Module;
import uruhingore.ua.model.Role;
import uruhingore.ua.model.Student;
import uruhingore.ua.model.Users;
import uruhingore.ua.repository.ModuleRepository;
import uruhingore.ua.repository.StudentRepository;
import uruhingore.ua.repository.UserRepository;

import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    
    @Transactional
    public StudentResponse createStudent(@Valid StudentRequest request) {
        log.info("Creating student: {} {}", request.getFirstName(), request.getLastName());
        
        // Validate parents exist and have PARENTS role
        Set<Users> parents = new HashSet<>();
        if (request.getParentIds() != null && !request.getParentIds().isEmpty()) {
            for (UUID parentId : request.getParentIds()) {
                Users parent = userRepository.findById(parentId)
                        .orElseThrow(() -> new IllegalArgumentException("Parent not found with ID: " + parentId));
                
                if (!parent.getRoles().contains(Role.PARENTS)) {
                    throw new IllegalArgumentException("User " + parent.getFullName() + " is not a parent");
                }
                parents.add(parent);
            }
        }
        
        // Validate modules exist
        Set<Module> modules = new HashSet<>();
        if (request.getModuleIds() != null && !request.getModuleIds().isEmpty()) {
            for (UUID moduleId : request.getModuleIds()) {
                Module module = moduleRepository.findById(moduleId)
                        .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
                modules.add(module);
            }
        }
        
        // Generate student code
        String studentCode = generateStudentCode();
        
        // Create student
        Student student = Student.builder()
                .studentCode(studentCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .classLevel(request.getClassLevel())
                .academicYear(request.getAcademicYear())
                .status(request.getStatus() != null ? request.getStatus() : Student.StudentStatus.ACTIVE)
                .parents(parents)
                .modules(modules)
                .build();
        
        Student savedStudent = studentRepository.save(student);
        log.info("Student created successfully with code: {}", savedStudent.getStudentCode());
        
        return StudentResponse.fromStudent(savedStudent);
    }
    
    @Transactional
    public StudentResponse assignParent(UUID studentId, UUID parentId) {
        log.info("Assigning parent {} to student {}", parentId, studentId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
        
        Users parent = userRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found with ID: " + parentId));
        
        if (!parent.getRoles().contains(Role.PARENTS)) {
            throw new IllegalArgumentException("User " + parent.getFullName() + " is not a parent");
        }
        
        student.getParents().add(parent);
        Student updatedStudent = studentRepository.save(student);
        
        log.info("Parent assigned successfully");
        return StudentResponse.fromStudent(updatedStudent);
    }
    
    @Transactional
    public StudentResponse enrollModule(UUID studentId, UUID moduleId) {
        log.info("Enrolling student {} in module {}", studentId, moduleId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
        
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
        
        student.getModules().add(module);
        Student updatedStudent = studentRepository.save(student);
        
        log.info("Student enrolled in module successfully");
        return StudentResponse.fromStudent(updatedStudent);
    }
    
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        log.info("Fetching all students");
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .map(StudentResponse::fromStudent)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(UUID studentId) {
        log.info("Fetching student with ID: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
        return StudentResponse.fromStudent(student);
    }
    
    @Transactional(readOnly = true)
    public List<StudentResponse.ParentInfo> getStudentParents(UUID studentId) {
        log.info("Fetching parents for student: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
        
        return student.getParents().stream()
                .map(parent -> StudentResponse.ParentInfo.builder()
                        .id(parent.getId())
                        .fullName(parent.getFullName())
                        .phone(parent.getPhone())
                        .email(parent.getEmail())
                        .address(parent.getAddress())
                        .gender(parent.getGender())
                        .status(parent.isEnabled())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByParent(UUID parentId) {
        log.info("Fetching students for parent: {}", parentId);
        
        // Verify parent exists
        Users parent = userRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found with ID: " + parentId));
        
        if (!parent.getRoles().contains(Role.PARENTS)) {
            throw new IllegalArgumentException("User is not a parent");
        }
        
        List<Student> students = studentRepository.findByParentId(parentId);
        return students.stream()
                .map(StudentResponse::fromStudent)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByClassLevel(String classLevel) {
        log.info("Fetching students for class level: {}", classLevel);
        List<Student> students = studentRepository.findByClassLevel(classLevel);
        return students.stream()
                .map(StudentResponse::fromStudent)
                .collect(Collectors.toList());
    }
    
    private String generateStudentCode() {
        String year = String.valueOf(Year.now().getValue());
        long count = studentRepository.count() + 1;
        String code = String.format("STD%s%04d", year, count);
        
        // Ensure uniqueness
        while (studentRepository.existsByStudentCode(code)) {
            count++;
            code = String.format("STD%s%04d", year, count);
        }
        
        return code;
    }
}
