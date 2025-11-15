package uruhingore.ua.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uruhingore.ua.dto.AddBulkMarksRequest;
import uruhingore.ua.dto.AddMarkRequest;
import uruhingore.ua.dto.UpdateMarkRequest;
import uruhingore.ua.model.AcademicData;
import uruhingore.ua.model.Module;
import uruhingore.ua.model.Report;
import uruhingore.ua.model.Student;
import uruhingore.ua.model.Users;
import uruhingore.ua.repository.AcademicDataRepository;
import uruhingore.ua.repository.ModuleRepository;
import uruhingore.ua.repository.ReportRepository;
import uruhingore.ua.repository.StudentRepository;
import uruhingore.ua.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;
    private final AcademicDataRepository academicDataRepository;
    private final UserRepository userRepository;

    /**
     * Calculate grade color based on score
     */
    private String calculateGradeColor(int score) {
        if (score >= 80) return "green";
        if (score >= 70) return "blue";
        if (score >= 50) return "yellow";
        return "red";
    }

    /**
     * Add or update a mark for a student in a module for a specific academic data
     */
    @Transactional
    public Report addOrUpdateMark(AddMarkRequest request) {
        log.info("Adding/updating mark for student: {}, module: {}, academicData: {}", 
                request.getStudentId(), request.getModuleId(), request.getAcademicDataId());

        // Fetch student
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + request.getStudentId()));

        // Fetch module
        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + request.getModuleId()));

        // Fetch academic data
        AcademicData academicData = academicDataRepository.findById(request.getAcademicDataId())
                .orElseThrow(() -> new IllegalArgumentException("AcademicData not found with id: " + request.getAcademicDataId()));

        // Check if student is enrolled in the module
        if (!student.getModules().contains(module)) {
            throw new IllegalArgumentException("Student is not enrolled in module: " + module.getName());
        }

        // Check if report already exists for this student, module, and academic data
        List<Report> existingReports = reportRepository.findByStudentAndModuleAndAcademicData(
                request.getStudentId(),
                request.getModuleId(),
                request.getAcademicDataId());

        Report report;
        if (!existingReports.isEmpty()) {
            // Update existing report
            log.info("Updating existing report");
            report = existingReports.get(0);
            report.setScore(request.getScore());
            report.setGradeColor(calculateGradeColor(request.getScore()));
            if (request.getTeacherComment() != null) {
                report.setTeacherComment(request.getTeacherComment());
            }
            if (request.getClassLevel() != null) {
                report.setClassLevel(request.getClassLevel());
            }
        } else {
            // Create new report
            log.info("Creating new report");
            Users teacher = null;
            if (request.getTeacherId() != null) {
                teacher = userRepository.findById(request.getTeacherId())
                        .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + request.getTeacherId()));
            }

            report = Report.builder()
                    .student(student)
                    .module(module)
                    .academicData(academicData)
                    .classLevel(request.getClassLevel())
                    .score(request.getScore())
                    .gradeColor(calculateGradeColor(request.getScore()))
                    .teacherComment(request.getTeacherComment())
                    .teacher(teacher)
                    .build();
        }

        Report savedReport = reportRepository.save(report);
        log.info("Mark saved successfully. Report ID: {}", savedReport.getId());
        return savedReport;
    }

    /**
     * Update an existing mark
     */
    @Transactional
    public Report updateMark(UUID reportId, UpdateMarkRequest request) {
        log.info("Updating mark for report: {}", reportId);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

        if (request.getScore() != null) {
            report.setScore(request.getScore());
            report.setGradeColor(calculateGradeColor(request.getScore()));
        }
        if (request.getTeacherComment() != null) {
            report.setTeacherComment(request.getTeacherComment());
        }
        if (request.getClassLevel() != null) {
            report.setClassLevel(request.getClassLevel());
        }

        Report savedReport = reportRepository.save(report);
        log.info("Mark updated successfully. Report ID: {}", savedReport.getId());
        return savedReport;
    }

    /**
     * Delete a mark
     */
    @Transactional
    public void deleteMark(UUID reportId) {
        log.info("Deleting mark for report: {}", reportId);
        
        if (!reportRepository.existsById(reportId)) {
            throw new IllegalArgumentException("Report not found with id: " + reportId);
        }
        
        reportRepository.deleteById(reportId);
        log.info("Mark deleted successfully. Report ID: {}", reportId);
    }

    /**
     * Get all reports for a student (only published)
     */
    @Transactional(readOnly = true)
    public List<Report> getPublishedReportsByStudent(UUID studentId) {
        return reportRepository.findPublishedByStudentId(studentId);
    }

    /**
     * Get all reports for a student and academic data (only published)
     */
    @Transactional(readOnly = true)
    public List<Report> getPublishedReportsByStudentAndAcademicData(UUID studentId, UUID academicDataId) {
        return reportRepository.findPublishedByStudentIdAndAcademicDataId(studentId, academicDataId);
    }

    /**
     * Add or update marks for multiple modules at once
     */
    @Transactional
    public List<Report> addOrUpdateBulkMarks(AddBulkMarksRequest request) {
        log.info("Adding/updating bulk marks for student: {}, academicData: {}, modules: {}", 
                request.getStudentId(), request.getAcademicDataId(), request.getModuleMarks().size());

        // Fetch student
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + request.getStudentId()));

        // Fetch academic data
        AcademicData academicData = academicDataRepository.findById(request.getAcademicDataId())
                .orElseThrow(() -> new IllegalArgumentException("AcademicData not found with id: " + request.getAcademicDataId()));

        // Fetch teacher if provided
        Users teacher = null;
        if (request.getTeacherId() != null) {
            teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + request.getTeacherId()));
        }

        List<Report> savedReports = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (AddBulkMarksRequest.ModuleMark moduleMark : request.getModuleMarks()) {
            try {
                // Fetch module
                Module module = moduleRepository.findById(moduleMark.getModuleId())
                        .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + moduleMark.getModuleId()));

                // Check if student is enrolled in the module
                if (!student.getModules().contains(module)) {
                    errors.add("Student is not enrolled in module: " + module.getName());
                    continue;
                }

                // Validate score
                if (moduleMark.getScore() < 0 || moduleMark.getScore() > 100) {
                    errors.add("Invalid score for module " + module.getName() + ": " + moduleMark.getScore() + " (must be 0-100)");
                    continue;
                }

                // Check if report already exists
                List<Report> existingReports = reportRepository.findByStudentAndModuleAndAcademicData(
                        request.getStudentId(),
                        moduleMark.getModuleId(),
                        request.getAcademicDataId());

                Report report;
                if (!existingReports.isEmpty()) {
                    // Update existing report
                    log.info("Updating existing report for module: {}", module.getName());
                    report = existingReports.get(0);
                    report.setScore(moduleMark.getScore());
                    report.setGradeColor(calculateGradeColor(moduleMark.getScore()));
                    report.setClassLevel(request.getClassLevel());
                    if (request.getTeacherComment() != null) {
                        report.setTeacherComment(request.getTeacherComment());
                    }
                    if (teacher != null) {
                        report.setTeacher(teacher);
                    }
                } else {
                    // Create new report
                    log.info("Creating new report for module: {}", module.getName());
                    report = Report.builder()
                            .student(student)
                            .module(module)
                            .academicData(academicData)
                            .classLevel(request.getClassLevel())
                            .score(moduleMark.getScore())
                            .gradeColor(calculateGradeColor(moduleMark.getScore()))
                            .teacherComment(request.getTeacherComment())
                            .teacher(teacher)
                            .build();
                }

                Report savedReport = reportRepository.save(report);
                savedReports.add(savedReport);
                log.info("Mark saved successfully for module: {}. Report ID: {}", module.getName(), savedReport.getId());
            } catch (IllegalArgumentException e) {
                errors.add("Error processing module " + moduleMark.getModuleId() + ": " + e.getMessage());
                log.error("Error processing module mark: {}", e.getMessage());
            } catch (Exception e) {
                errors.add("Unexpected error processing module " + moduleMark.getModuleId() + ": " + e.getMessage());
                log.error("Unexpected error processing module mark: {}", e.getMessage(), e);
            }
        }

        if (!errors.isEmpty() && savedReports.isEmpty()) {
            throw new IllegalArgumentException("Failed to add any marks. Errors: " + String.join("; ", errors));
        }

        if (!errors.isEmpty()) {
            log.warn("Some marks were not processed. Errors: {}", String.join("; ", errors));
        }

        log.info("Bulk marks operation completed. Successfully saved: {}, Errors: {}", savedReports.size(), errors.size());
        return savedReports;
    }
}

