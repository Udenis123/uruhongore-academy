package uruhingore.ua.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.DocumentException;
import java.io.IOException;
import uruhingore.ua.dto.AddBulkMarksRequest;
import uruhingore.ua.dto.AddMarkRequest;
import uruhingore.ua.dto.GroupedReportResponse;
import uruhingore.ua.dto.ReportResponse;
import uruhingore.ua.dto.UpdateMarkRequest;
import uruhingore.ua.model.Report;
import uruhingore.ua.repository.ReportRepository;
import uruhingore.ua.service.DocumentService;
import uruhingore.ua.service.ReportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;
    private final DocumentService documentService;

    /**
     * Add or update marks for multiple modules at once
     */
    @PostMapping("/add-marks/bulk")
    public ResponseEntity<?> addBulkMarks(@RequestBody @Valid AddBulkMarksRequest request) {
        try {
            log.info("Received request to add/update bulk marks for student: {}, academicData: {}, modules: {}", 
                    request.getStudentId(), request.getAcademicDataId(), 
                    request.getModuleMarks() != null ? request.getModuleMarks().size() : 0);
            List<Report> reports = reportService.addOrUpdateBulkMarks(request);
            
            // Group reports by student and academic data
            GroupedReportResponse groupedResponse = GroupedReportResponse.fromReportsSingle(reports);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Marks added/updated successfully");
            response.put("count", reports.size());
            response.put("data", groupedResponse);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.BAD_REQUEST.value());
            errorBody.put("error", "Bad Request");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorBody);
        } catch (Exception e) {
            log.error("Error adding bulk marks: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred while adding the marks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    /**
     * Add or update a mark for a student (single module)
     */
    @PostMapping("/add-mark")
    public ResponseEntity<?> addMark(@RequestBody @Valid AddMarkRequest request) {
        try {
            log.info("Received request to add/update mark for student: {}, module: {}, academicData: {}", 
                    request.getStudentId(), request.getModuleId(), request.getAcademicDataId());
            Report report = reportService.addOrUpdateMark(request);
            return ResponseEntity.ok(ReportResponse.fromReport(report));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.BAD_REQUEST.value());
            errorBody.put("error", "Bad Request");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorBody);
        } catch (Exception e) {
            log.error("Error adding mark: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred while adding the mark: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    /**
     * Update an existing mark
     */
    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateMark(@PathVariable UUID reportId, @RequestBody @Valid UpdateMarkRequest request) {
        try {
            log.info("Received request to update mark for report: {}", reportId);
            Report report = reportService.updateMark(reportId, request);
            return ResponseEntity.ok(ReportResponse.fromReport(report));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.NOT_FOUND.value());
            errorBody.put("error", "Not Found");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        } catch (Exception e) {
            log.error("Error updating mark: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred while updating the mark: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    /**
     * Delete a mark
     */
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteMark(@PathVariable UUID reportId) {
        try {
            log.info("Received request to delete mark for report: {}", reportId);
            reportService.deleteMark(reportId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mark deleted successfully");
            response.put("reportId", reportId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.NOT_FOUND.value());
            errorBody.put("error", "Not Found");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        } catch (Exception e) {
            log.error("Error deleting mark: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred while deleting the mark: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    /**
     * Get all published reports for a student
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ReportResponse>> getReportsByStudent(@PathVariable UUID studentId) {
        log.info("Received request to get published reports for student: {}", studentId);
        List<Report> reports = reportService.getPublishedReportsByStudent(studentId);
        return ResponseEntity.ok(ReportResponse.fromReports(reports));
    }

    /**
     * Get all published reports for a student and academic data
     */
    @GetMapping("/student/{studentId}/academic-data/{academicDataId}")
    public ResponseEntity<GroupedReportResponse> getReportsByStudentAndAcademicData(
            @PathVariable UUID studentId,
            @PathVariable UUID academicDataId) {
        log.info("Received request to get published reports for student: {} and academicData: {}", 
                studentId, academicDataId);
        List<Report> reports = reportService.getPublishedReportsByStudentAndAcademicData(studentId, academicDataId);
        GroupedReportResponse groupedResponse = GroupedReportResponse.fromReportsSingle(reports);
        return ResponseEntity.ok(groupedResponse);
    }

    /**
     * Generate bulletin PDF for a student based on their marks for a given academic data
     */
    @GetMapping("/student/{studentId}/academic-data/{academicDataId}/bulletin")
    public ResponseEntity<?> generateBulletin(
            @PathVariable UUID studentId,
            @PathVariable UUID academicDataId) {
        try {
            log.info("Received request to generate bulletin for student: {} and academicData: {}", 
                    studentId, academicDataId);
            
            // Generate PDF using DocumentService
            byte[] pdfBytes = documentService.generateBulletinFromAcademicData(studentId, academicDataId);
            
            // Set response headers for PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "bulletin_" + studentId + "_" + academicDataId + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            log.info("Bulletin generated successfully for student: {} and academicData: {}", studentId, academicDataId);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.NOT_FOUND.value());
            errorBody.put("error", "Not Found");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        } catch (DocumentException | IOException e) {
            log.error("Error generating bulletin: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An error occurred while generating the bulletin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        } catch (Exception e) {
            log.error("Unexpected error generating bulletin: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", java.time.LocalDateTime.now());
            errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorBody.put("error", "Internal Server Error");
            errorBody.put("message", "An unexpected error occurred while generating the bulletin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }
}
