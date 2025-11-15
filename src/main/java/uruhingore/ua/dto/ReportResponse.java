package uruhingore.ua.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uruhingore.ua.model.ClassLevel;
import uruhingore.ua.model.Report;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {
    
    private UUID id;
    private StudentInfo student;
    private ModuleInfo module;
    private AcademicDataInfo academicData;
    private ClassLevel classLevel;
    private Integer score;
    private String gradeColor;
    private String teacherComment;
    private TeacherInfo teacher;
    private ApprovedByInfo approvedBy;
    private LocalDate dateRecorded;
    
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
    public static class ModuleInfo {
        private UUID id;
        private String name;
        private String category;
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
    public static class TeacherInfo {
        private UUID id;
        private String fullName;
        private String email;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovedByInfo {
        private UUID id;
        private String fullName;
        private String email;
    }
    
    public static ReportResponse fromReport(Report report) {
        if (report == null) {
            return null;
        }
        
        return ReportResponse.builder()
                .id(report.getId())
                .student(report.getStudent() != null ? StudentInfo.builder()
                        .id(report.getStudent().getId())
                        .studentCode(report.getStudent().getStudentCode())
                        .firstName(report.getStudent().getFirstName())
                        .lastName(report.getStudent().getLastName())
                        .fullName(report.getStudent().getFirstName() + " " + report.getStudent().getLastName())
                        .build() : null)
                .module(report.getModule() != null ? ModuleInfo.builder()
                        .id(report.getModule().getId())
                        .name(report.getModule().getName())
                        .category(report.getModule().getCategory())
                        .build() : null)
                .academicData(report.getAcademicData() != null ? AcademicDataInfo.builder()
                        .id(report.getAcademicData().getId())
                        .trimester(report.getAcademicData().getTrimester() != null ? 
                                report.getAcademicData().getTrimester().getDisplayName() : null)
                        .academicYear(report.getAcademicData().getAcademicYear())
                        .period(report.getAcademicData().getPeriod() != null ? 
                                report.getAcademicData().getPeriod().getDisplayName() : null)
                        .published(report.getAcademicData().getPublished())
                        .build() : null)
                .classLevel(report.getClassLevel())
                .score(report.getScore())
                .gradeColor(report.getGradeColor())
                .teacherComment(report.getTeacherComment())
                .teacher(report.getTeacher() != null ? TeacherInfo.builder()
                        .id(report.getTeacher().getId())
                        .fullName(report.getTeacher().getFullName())
                        .email(report.getTeacher().getEmail())
                        .build() : null)
                .approvedBy(report.getApprovedBy() != null ? ApprovedByInfo.builder()
                        .id(report.getApprovedBy().getId())
                        .fullName(report.getApprovedBy().getFullName())
                        .email(report.getApprovedBy().getEmail())
                        .build() : null)
                .dateRecorded(report.getDateRecorded())
                .build();
    }
    
    public static List<ReportResponse> fromReports(List<Report> reports) {
        if (reports == null) {
            return List.of();
        }
        return reports.stream()
                .map(ReportResponse::fromReport)
                .collect(Collectors.toList());
    }
}

