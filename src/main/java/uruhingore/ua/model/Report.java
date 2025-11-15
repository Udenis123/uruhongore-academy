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

    @ManyToOne
    @JoinColumn(name = "academic_data_id", nullable = false)
    private AcademicData academicData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ClassLevel classLevel; // Student's class level

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
