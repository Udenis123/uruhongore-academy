package uruhingore.ua.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uruhingore.ua.model.ClassLevel;
import uruhingore.ua.model.Report;

import java.time.LocalDate;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupedReportResponse {
    
    private Integer academicYear;
    private StudentInfo student;
    private AcademicDataInfo academicData;
    private ClassLevel classLevel;
    private String teacherComment;
    private TeacherInfo teacher;
    private LocalDate dateRecorded;
    private List<ModuleMark> modules;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private UUID id;
        private String studentCode;
        private String firstName;
        private String lastName;
        private String fullName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicDataInfo {
        private UUID id;
        private String trimester;
        private Integer academicYear;
        private String period;
        private Boolean published;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleMark {
        private UUID reportId;
        private ModuleInfo module;
        private Integer score;
        private String gradeColor;
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
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherInfo {
        private UUID id;
        private String fullName;
        private String email;
    }
    
    /**
     * Group reports by student and academic data
     * Returns a list of grouped responses (one per student/academicData combination)
     */
    public static List<GroupedReportResponse> fromReports(List<Report> reports) {
        if (reports == null || reports.isEmpty()) {
            return List.of();
        }
        
        // Group reports by student ID and academic data ID
        Map<String, List<Report>> grouped = reports.stream()
                .collect(Collectors.groupingBy(report -> 
                    report.getStudent().getId() + "_" + report.getAcademicData().getId()));
        
        List<GroupedReportResponse> result = new ArrayList<>();
        
        for (List<Report> reportGroup : grouped.values()) {
            if (reportGroup.isEmpty()) {
                continue;
            }
            
            Report firstReport = reportGroup.get(0);
            
            // Build module marks list
            List<ModuleMark> moduleMarks = reportGroup.stream()
                    .map(report -> ModuleMark.builder()
                            .reportId(report.getId())
                            .module(report.getModule() != null ? ModuleInfo.builder()
                                    .id(report.getModule().getId())
                                    .name(report.getModule().getName())
                                    .category(report.getModule().getCategory())
                                    .build() : null)
                            .score(report.getScore())
                            .gradeColor(report.getGradeColor())
                            .build())
                    .collect(Collectors.toList());
            
            GroupedReportResponse groupedResponse = GroupedReportResponse.builder()
                    .academicYear(firstReport.getAcademicData() != null ? firstReport.getAcademicData().getAcademicYear() : null)
                    .student(firstReport.getStudent() != null ? StudentInfo.builder()
                            .id(firstReport.getStudent().getId())
                            .studentCode(firstReport.getStudent().getStudentCode())
                            .firstName(firstReport.getStudent().getFirstName())
                            .lastName(firstReport.getStudent().getLastName())
                            .fullName(firstReport.getStudent().getFirstName() + " " + firstReport.getStudent().getLastName())
                            .build() : null)
                    .academicData(firstReport.getAcademicData() != null ? AcademicDataInfo.builder()
                            .id(firstReport.getAcademicData().getId())
                            .trimester(firstReport.getAcademicData().getTrimester() != null ? 
                                    firstReport.getAcademicData().getTrimester().getDisplayName() : null)
                            .academicYear(firstReport.getAcademicData().getAcademicYear())
                            .period(firstReport.getAcademicData().getPeriod() != null ? 
                                    firstReport.getAcademicData().getPeriod().getDisplayName() : null)
                            .published(firstReport.getAcademicData().getPublished())
                            .build() : null)
                    .classLevel(firstReport.getClassLevel())
                    .teacherComment(firstReport.getTeacherComment())
                    .teacher(firstReport.getTeacher() != null ? TeacherInfo.builder()
                            .id(firstReport.getTeacher().getId())
                            .fullName(firstReport.getTeacher().getFullName())
                            .email(firstReport.getTeacher().getEmail())
                            .build() : null)
                    .dateRecorded(firstReport.getDateRecorded())
                    .modules(moduleMarks)
                    .build();
            
            result.add(groupedResponse);
        }
        
        return result;
    }
    
    /**
     * Create a single grouped response from a list of reports
     * Assumes all reports are for the same student and academic data
     */
    public static GroupedReportResponse fromReportsSingle(List<Report> reports) {
        if (reports == null || reports.isEmpty()) {
            return null;
        }
        
        Report firstReport = reports.get(0);
        
        // Build module marks list
        List<ModuleMark> moduleMarks = reports.stream()
                .map(report -> ModuleMark.builder()
                        .reportId(report.getId())
                        .module(report.getModule() != null ? ModuleInfo.builder()
                                .id(report.getModule().getId())
                                .name(report.getModule().getName())
                                .category(report.getModule().getCategory())
                                .build() : null)
                        .score(report.getScore())
                        .gradeColor(report.getGradeColor())
                        .build())
                .collect(Collectors.toList());
        
        return GroupedReportResponse.builder()
                .academicYear(firstReport.getAcademicData() != null ? firstReport.getAcademicData().getAcademicYear() : null)
                .student(firstReport.getStudent() != null ? StudentInfo.builder()
                        .id(firstReport.getStudent().getId())
                        .studentCode(firstReport.getStudent().getStudentCode())
                        .firstName(firstReport.getStudent().getFirstName())
                        .lastName(firstReport.getStudent().getLastName())
                        .fullName(firstReport.getStudent().getFirstName() + " " + firstReport.getStudent().getLastName())
                        .build() : null)
                .academicData(firstReport.getAcademicData() != null ? AcademicDataInfo.builder()
                        .id(firstReport.getAcademicData().getId())
                        .trimester(firstReport.getAcademicData().getTrimester() != null ? 
                                firstReport.getAcademicData().getTrimester().getDisplayName() : null)
                        .academicYear(firstReport.getAcademicData().getAcademicYear())
                        .period(firstReport.getAcademicData().getPeriod() != null ? 
                                firstReport.getAcademicData().getPeriod().getDisplayName() : null)
                        .published(firstReport.getAcademicData().getPublished())
                        .build() : null)
                .classLevel(firstReport.getClassLevel())
                .teacherComment(firstReport.getTeacherComment())
                .teacher(firstReport.getTeacher() != null ? TeacherInfo.builder()
                        .id(firstReport.getTeacher().getId())
                        .fullName(firstReport.getTeacher().getFullName())
                        .email(firstReport.getTeacher().getEmail())
                        .build() : null)
                .dateRecorded(firstReport.getDateRecorded())
                .modules(moduleMarks)
                .build();
    }
}

