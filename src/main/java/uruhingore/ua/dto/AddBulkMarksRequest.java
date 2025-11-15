package uruhingore.ua.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uruhingore.ua.model.ClassLevel;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddBulkMarksRequest {
    
    @NotNull(message = "Student ID is required")
    private UUID studentId;
    
    @NotNull(message = "Academic Data ID is required")
    private UUID academicDataId;
    
    @NotNull(message = "Class level is required")
    private ClassLevel classLevel;
    
    private String teacherComment; // Optional - applies to all marks
    private UUID teacherId; // Optional
    
    @NotNull(message = "Module marks are required")
    @Valid
    private List<ModuleMark> moduleMarks;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleMark {
        @NotNull(message = "Module ID is required")
        private UUID moduleId;
        
        @NotNull(message = "Score is required")
        private Integer score; // 0-100
    }
}

