package uruhingore.ua.controller;

import uruhingore.ua.dto.BulletinRequest;
import uruhingore.ua.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * Preview bulletin document
     */
    @GetMapping("/preview/bulletin")
    public ResponseEntity<byte[]> previewBulletin(
            @RequestParam(required = false, defaultValue = "") String studentName,
            @RequestParam(required = false, defaultValue = "") String classe,
            @RequestParam(required = false, defaultValue = "2025/2026") String annee) {

        try {
            BulletinRequest request = new BulletinRequest();
            request.setStudentName(studentName);
            request.setClasse(classe);
            request.setAnnee(annee);

            byte[] pdfBytes = documentService.generateBulletinPdf(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "bulletin_" + studentName + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Download bulletin document
     */
    @PostMapping("/download/bulletin")
    public ResponseEntity<byte[]> downloadBulletin(@RequestBody BulletinRequest request) {

        try {
            byte[] pdfBytes = documentService.generateBulletinPdf(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "bulletin_" + request.getStudentName() + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generate bulletin with grades
     */
    @PostMapping("/generate/bulletin")
    public ResponseEntity<byte[]> generateBulletinWithGrades(@RequestBody BulletinRequest request) {

        try {
            byte[] pdfBytes = documentService.generateBulletinWithGrades(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "bulletin_" + request.getStudentName() + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}