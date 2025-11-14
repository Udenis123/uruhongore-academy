package uruhingore.ua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMarkRequest {
    private UUID studentId;
    private UUID moduleId;
    private Integer trimester; // 1, 2, or 3
    private Integer academicYear; // e.g., 2024, 2025
    private String classe; // e.g., "Maternelle 1", "Primaire 2"
    private int score; // 0-100
    private String teacherComment; // Optional
    private UUID teacherId; // Optional
}
