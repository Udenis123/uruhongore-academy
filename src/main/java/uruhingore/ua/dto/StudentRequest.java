package uruhingore.ua.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import uruhingore.ua.model.ClassLevel;
import uruhingore.ua.model.Student;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class StudentRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "MALE|FEMALE", message = "Gender must be MALE or FEMALE")
    private String gender;

    // Academic information
    @NotNull(message = "Class level is required")
    private ClassLevel classLevel;

    @NotBlank(message = "Academic year is required")
    private String academicYear; // e.g., "2024-2025"

    private Student.StudentStatus status;

    // Parent IDs (Users with role PARENTS)
    @NotNull(message = "At least one parent is required")
    private Set<UUID> parentIds;

    // Module IDs the student is enrolled in
    private Set<UUID> moduleIds;
}
