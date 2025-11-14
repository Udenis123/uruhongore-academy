package uruhingore.ua.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleGradeDto {
    private String moduleName;
    private String subModuleName;
    private Integer score;
    private String gradeColor;
    
    // For grouping modules with multiple sub-modules
    @Builder.Default
    private List<SubModuleGrade> subModules = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubModuleGrade {
        private String subModuleName;
        private Integer score;
        private String gradeColor;
    }
}
