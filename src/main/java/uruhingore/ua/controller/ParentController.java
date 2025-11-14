package uruhingore.ua.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uruhingore.ua.model.Users;
import uruhingore.ua.model.Report;
import uruhingore.ua.repository.UserRepository;
import uruhingore.ua.repository.ReportRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final UserRepository usersRepository;
    private final ReportRepository reportRepository;

//    @GetMapping("/{parentId}/children")
//    public ResponseEntity<?> getChildren(@PathVariable UUID parentId) {
//        Users parent = usersRepository.findById(parentId)
//                .orElseThrow(() -> new RuntimeException("Parent not found"));
//        return ResponseEntity.ok(parent.getChildren());
//    }
//
//    @GetMapping("/{parentId}/child/{studentId}/reports")
//    public ResponseEntity<?> getChildReports(@PathVariable UUID parentId, @PathVariable UUID studentId) {
//        Users parent = usersRepository.findById(parentId)
//                .orElseThrow(() -> new RuntimeException("Parent not found"));
//
//        boolean canAccess = parent.getChildren()
//                .stream().anyMatch(s -> s.getId().equals(studentId));
//
//        if (!canAccess) return ResponseEntity.status(403).body("Access denied");
//
//        List<Report> reports = reportRepository.findByStudentId(studentId);
//        return ResponseEntity.ok(reports);
//    }
}
