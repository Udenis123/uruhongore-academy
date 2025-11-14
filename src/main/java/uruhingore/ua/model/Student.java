package uruhingore.ua.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_student_code", columnList = "studentCode"),
        @Index(name = "idx_class_level", columnList = "classLevel")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String studentCode; // e.g., "STD2025001"

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String gender; // MALE, FEMALE

    // Academic information
    @Column(nullable = false)
    private String classLevel; // e.g., "Nursery 1", "Nursery 2", "Nursery 3", "Pre-Primary"

    @Column(nullable = false)
    private String academicYear; // e.g., "2024-2025"

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE; // ACTIVE, INACTIVE, GRADUATED, TRANSFERRED, SUSPENDED

    // Relationship with Parents (from Users model with role PARENTS)
    @ManyToMany
    @JoinTable(
            name = "parent_students",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id")
    )
    @Builder.Default
    private Set<Users> parents = new HashSet<>();

    // Relationship with Modules (subjects the student is enrolled in)
    @ManyToMany
    @JoinTable(
            name = "student_modules",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    @Builder.Default
    private Set<Module> modules = new HashSet<>();

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum for Student Status
    public enum StudentStatus {
        ACTIVE,
        INACTIVE,
        GRADUATED,
        TRANSFERRED,
        SUSPENDED
    }
}
