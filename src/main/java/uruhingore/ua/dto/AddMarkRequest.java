package uruhingore.ua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uruhingore.ua.model.Period;
import uruhingore.ua.model.Trimester;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMarkRequest {
    private UUID studentId;
    private UUID moduleId;
    private Trimester trimester; // FIRST, SECOND, THIRD
    private Integer academicYear; // e.g., 2024, 2025
    private Period period; // PERIOD_1, PERIOD_2, PERIOD_3, FINAL_SEMESTER
    private String classe; // e.g., "Maternelle 1", "Primaire 2"
    private int score; // 0-100
    private String teacherComment; // Optional
    private UUID teacherId; // Optional
}
