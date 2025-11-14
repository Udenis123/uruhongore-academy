package uruhingore.ua.dto;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulletinRequest {
    private UUID studentId;
    private String studentName;
    private String classe;
    private String annee;
    private String trimester;
    private String comment;
    
    // For backward compatibility
    private Map<String, SubjectGrade> grades;
    
    // New: Module-based grades from database
    private List<ModuleGradeDto> moduleGrades;
}
