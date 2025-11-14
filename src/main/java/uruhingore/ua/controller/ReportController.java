package uruhingore.ua.controller;

import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uruhingore.ua.dto.AddMarkRequest;
import uruhingore.ua.dto.UpdateMarkRequest;
import uruhingore.ua.model.Module;
import uruhingore.ua.model.Report;
import uruhingore.ua.model.Student;
import uruhingore.ua.model.Users;
import uruhingore.ua.repository.ReportRepository;
import uruhingore.ua.repository.StudentRepository;
import uruhingore.ua.service.DocumentService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportRepository reportRepository;
    private final DocumentService documentService;
    private final uruhingore.ua.repository.UserRepository userRepository;
    private final uruhingore.ua.repository.ModuleRepository moduleRepository;
    private final StudentRepository studentRepository;

    // CREATE OR UPDATE REPORT
    @PostMapping
    public ResponseEntity<?> createOrUpdateReport(@RequestBody Report report) {
        // Auto-calculate grade color based on score
        report.setGradeColor(calculateGradeColor(report.getScore()));

        reportRepository.save(report);
        return ResponseEntity.ok("Report saved successfully");
    }

    // ADD SINGLE MARK (Simplified endpoint)
    @PostMapping("/add-mark")
    public ResponseEntity<?> addMark(@RequestBody AddMarkRequest request) {
        try {
            // Validate trimester (1, 2, or 3)
            if (request.getTrimester() == null || request.getTrimester() < 1 || request.getTrimester() > 3) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: Trimester must be 1, 2, or 3");
            }

            // Validate academic year
            if (request.getAcademicYear() == null || request.getAcademicYear() < 2000 || request.getAcademicYear() > 2100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: Academic year must be a valid year");
            }

            // Fetch student
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + request.getStudentId()));

            // Fetch module
            Module module = moduleRepository.findById(request.getModuleId())
                    .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + request.getModuleId()));

            // Check if student is enrolled in the module
            if (!student.getModules().contains(module)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: Student is not enrolled in module: " + module.getName());
            }

            // Check if report already exists for this student, module, trimester, and year
            List<Report> existingReports = reportRepository.findByStudentAndModuleAndTrimesterAndYear(
                    request.getStudentId(),
                    request.getModuleId(),
                    request.getTrimester(),
                    request.getAcademicYear());

            Report report;
            if (!existingReports.isEmpty()) {
                // Update existing report
                report = existingReports.get(0);
                report.setScore(request.getScore());
                report.setGradeColor(calculateGradeColor(request.getScore()));
                if (request.getTeacherComment() != null) {
                    report.setTeacherComment(request.getTeacherComment());
                }
                if (request.getClasse() != null) {
                    report.setClasse(request.getClasse());
                }
            } else {
                // Create new report
                Users teacher = null;
                if (request.getTeacherId() != null) {
                    teacher = userRepository.findById(request.getTeacherId())
                            .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + request.getTeacherId()));
                }

                report = Report.builder()
                        .student(student)
                        .module(module)
                        .trimester(request.getTrimester())
                        .academicYear(request.getAcademicYear())
                        .classe(request.getClasse())
                        .score(request.getScore())
                        .gradeColor(calculateGradeColor(request.getScore()))
                        .teacherComment(request.getTeacherComment())
                        .teacher(teacher)
                        .build();
            }

            Report savedReport = reportRepository.save(report);
            return ResponseEntity.ok(savedReport);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error adding mark: " + e.getMessage());
        }
    }

    // ADD MULTIPLE MARKS AT ONCE (Bulk)
    @PostMapping("/add-marks/bulk")
    public ResponseEntity<?> addMarksBulk(@RequestBody List<AddMarkRequest> requests) {
        try {
            List<Report> reports = new java.util.ArrayList<>();
            List<String> errors = new java.util.ArrayList<>();

            for (AddMarkRequest request : requests) {
                try {
                    // Validate trimester
                    if (request.getTrimester() == null || request.getTrimester() < 1 || request.getTrimester() > 3) {
                        errors.add("Request for student " + request.getStudentId() + ": Trimester must be 1, 2, or 3");
                        continue;
                    }

                    // Validate academic year
                    if (request.getAcademicYear() == null || request.getAcademicYear() < 2000 || request.getAcademicYear() > 2100) {
                        errors.add("Request for student " + request.getStudentId() + ": Academic year must be a valid year");
                        continue;
                    }

                    // Fetch student
                    Student student = studentRepository.findById(request.getStudentId())
                            .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + request.getStudentId()));

                    // Fetch module
                    Module module = moduleRepository.findById(request.getModuleId())
                            .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + request.getModuleId()));

                    // Check if student is enrolled in the module
                    if (!student.getModules().contains(module)) {
                        errors.add("Student " + student.getFirstName() + " " + student.getLastName() + " is not enrolled in module: " + module.getName());
                        continue;
                    }

                    // Check if report already exists
                    List<Report> existingReports = reportRepository.findByStudentAndModuleAndTrimesterAndYear(
                            request.getStudentId(),
                            request.getModuleId(),
                            request.getTrimester(),
                            request.getAcademicYear());

                    Report report;
                    if (!existingReports.isEmpty()) {
                        // Update existing report
                        report = existingReports.get(0);
                        report.setScore(request.getScore());
                        report.setGradeColor(calculateGradeColor(request.getScore()));
                        if (request.getTeacherComment() != null) {
                            report.setTeacherComment(request.getTeacherComment());
                        }
                        if (request.getClasse() != null) {
                            report.setClasse(request.getClasse());
                        }
                    } else {
                        // Create new report
                        Users teacher = null;
                        if (request.getTeacherId() != null) {
                            teacher = userRepository.findById(request.getTeacherId())
                                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + request.getTeacherId()));
                        }

                        report = Report.builder()
                                .student(student)
                                .module(module)
                                .trimester(request.getTrimester())
                                .academicYear(request.getAcademicYear())
                                .classe(request.getClasse())
                                .score(request.getScore())
                                .gradeColor(calculateGradeColor(request.getScore()))
                                .teacherComment(request.getTeacherComment())
                                .teacher(teacher)
                                .build();
                    }

                    reports.add(report);
                } catch (Exception e) {
                    errors.add("Error processing request for student " + request.getStudentId() + ": " + e.getMessage());
                }
            }

            if (!reports.isEmpty()) {
                reportRepository.saveAll(reports);
            }

            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .body(reports.size() + " marks processed. Errors: " + String.join("; ", errors));
            }

            return ResponseEntity.ok(reports.size() + " marks added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error adding marks: " + e.getMessage());
        }
    }

    // UPDATE MARK (Change score, comment, etc.)
    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateMark(@PathVariable UUID reportId, @RequestBody UpdateMarkRequest request) {
        try {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

            if (request.getScore() != null) {
                report.setScore(request.getScore());
                report.setGradeColor(calculateGradeColor(request.getScore()));
            }
            if (request.getTeacherComment() != null) {
                report.setTeacherComment(request.getTeacherComment());
            }
            if (request.getClasse() != null) {
                report.setClasse(request.getClasse());
            }

            reportRepository.save(report);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating mark: " + e.getMessage());
        }
    }

    // DELETE MARK
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteMark(@PathVariable UUID reportId) {
        try {
            if (!reportRepository.existsById(reportId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Report not found with id: " + reportId);
            }
            reportRepository.deleteById(reportId);
            return ResponseEntity.ok("Mark deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting mark: " + e.getMessage());
        }
    }

    // Helper method to calculate grade color
    private String calculateGradeColor(int score) {
        if (score >= 80) return "green";
        if (score >= 70) return "blue";
        if (score >= 50) return "yellow";
        return "red";
    }

    // VIEW REPORTS BY STUDENT
    @GetMapping("/student/{studentId}")
    public List<Report> getReportsByStudent(@PathVariable UUID studentId) {
        return reportRepository.findByStudentId(studentId);
    }

    // VIEW REPORTS BY TRIMESTER
    @GetMapping("/student/{studentId}/trimester/{trimester}")
    public List<Report> getReportsByTrimester(
            @PathVariable UUID studentId,
            @PathVariable Integer trimester) {
        return reportRepository.findByStudentIdAndTrimester(studentId, trimester);
    }

    // VIEW REPORTS BY TRIMESTER AND YEAR
    @GetMapping("/student/{studentId}/trimester/{trimester}/year/{academicYear}")
    public List<Report> getReportsByTrimesterAndYear(
            @PathVariable UUID studentId,
            @PathVariable Integer trimester,
            @PathVariable Integer academicYear) {
        return reportRepository.findByStudentIdAndTrimesterAndAcademicYear(studentId, trimester, academicYear);
    }

    // VIEW REPORTS BY YEAR
    @GetMapping("/student/{studentId}/year/{academicYear}")
    public List<Report> getReportsByYear(
            @PathVariable UUID studentId,
            @PathVariable Integer academicYear) {
        return reportRepository.findByStudentIdAndAcademicYear(studentId, academicYear);
    }

    // GENERATE BULLETIN PDF FROM DATABASE REPORTS
    @GetMapping("/bulletin/{studentId}/trimester/{trimester}/year/{academicYear}")
    public ResponseEntity<byte[]> generateBulletin(
            @PathVariable UUID studentId,
            @PathVariable Integer trimester,
            @PathVariable Integer academicYear) {
        try {
            byte[] pdfBytes = documentService.generateBulletinFromDatabase(studentId, trimester, academicYear);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "bulletin_" + studentId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (DocumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating bulletin: " + e.getMessage()).getBytes());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }

    // GENERATE BLANK BULLETIN TEMPLATE FOR STUDENT (for teachers to fill in marks)
    @GetMapping("/template/{studentId}/trimester/{trimester}/year/{academicYear}")
    public ResponseEntity<byte[]> generateBulletinTemplate(
            @PathVariable UUID studentId,
            @PathVariable Integer trimester,
            @PathVariable Integer academicYear,
            @RequestParam(required = false) String classe) {
        try {
            byte[] pdfBytes = documentService.generateBulletinTemplate(
                    studentId, trimester, academicYear, classe);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "bulletin_template_" + studentId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (DocumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating bulletin template: " + e.getMessage()).getBytes());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }

    // GENERATE GRID BULLETIN WITH COLOR-CODED CELLS (matching the image design)
    @GetMapping("/grid/{studentId}/year/{academicYear}")
    public ResponseEntity<byte[]> generateGridBulletin(
            @PathVariable UUID studentId,
            @PathVariable Integer academicYear,
            @RequestParam(required = false) String classe) {
        try {
            byte[] pdfBytes = documentService.generateGridBulletin(studentId, academicYear, classe);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "bulletin_grid_" + studentId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (DocumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating grid bulletin: " + e.getMessage()).getBytes());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }
}
