package uruhingore.ua.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(nullable = false)
    private Integer trimester; // 1, 2, or 3

    @Column(nullable = false)
    private Integer academicYear; // e.g., 2024, 2025

    @Column(length = 50)
    private String classe; // Student's class (e.g., "Maternelle 1", "Primaire 3")

    private int score; // e.g., 80–100
    private String gradeColor; // optional (green, blue, yellow, red)

    @Column(length = 500)
    private String teacherComment;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Users teacher;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Users approvedBy; // Head approval

    @Builder.Default
    private LocalDate dateRecorded = LocalDate.now();
}
