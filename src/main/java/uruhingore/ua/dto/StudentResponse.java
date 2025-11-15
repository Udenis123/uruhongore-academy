package uruhingore.ua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uruhingore.ua.model.ClassLevel;
import uruhingore.ua.model.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    
    private UUID id;
    private String studentCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String profilePhoto;
    
    // Academic information
    private ClassLevel classLevel;
    private String academicYear;
    private Student.StudentStatus status;
    
    private Set<ParentInfo> parents;
    private Set<ModuleInfo> modules;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentInfo {
        private UUID id;
        private String fullName;
        private String phone;
        private String email;
        private String address;
        private String gender;
        private boolean status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleInfo {
        private UUID id;
        private String name;
        private String category;

    }
    
    public static StudentResponse fromStudent(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(student.getFirstName() + " " + student.getLastName())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .profilePhoto(student.getProfilePhoto())
                .classLevel(student.getClassLevel())
                .academicYear(student.getAcademicYear())
                .status(student.getStatus())
                .parents(student.getParents() != null ? student.getParents().stream()
                        .map(parent -> ParentInfo.builder()
                                .id(parent.getId())
                                .fullName(parent.getFullName())
                                .phone(parent.getPhone())
                                .email(parent.getEmail())
                                .address(parent.getAddress())
                                .gender(parent.getGender())
                                .status(parent.isEnabled())
                                .build())
                        .collect(Collectors.toSet()) : Set.of())
                .modules(student.getModules() != null ? student.getModules().stream()
                        .map(module -> ModuleInfo.builder()
                                .id(module.getId())
                                .name(module.getName())
                                .category(module.getCategory())
                                .build())
                        .collect(Collectors.toSet()) : Set.of())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
