package uruhingore.ua.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import uruhingore.ua.dto.BulletinRequest;
import uruhingore.ua.dto.ModuleGradeDto;
import uruhingore.ua.dto.SubjectGrade;
import uruhingore.ua.model.Trimester;
import uruhingore.ua.repository.ModuleRepository;
import uruhingore.ua.repository.ReportRepository;
import uruhingore.ua.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final ReportRepository reportRepository;
    private final ModuleRepository moduleRepository;
    private final StudentRepository studentRepository;

    private static final Font FONT_BOLD_12 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_9 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font FONT_NORMAL_10 = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font FONT_NORMAL_9 = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);

    /**
     * Generate bulletin PDF from database (student reports with modules)
     */
    public byte[] generateBulletinFromDatabase(java.util.UUID studentId, Trimester trimester, 
            Integer academicYear) throws DocumentException, IOException {
        
        // Fetch reports from database (only published)
        List<uruhingore.ua.model.Report> reports = reportRepository.findPublishedReportsForBulletin(
                studentId, trimester, academicYear);
        
        if (reports.isEmpty()) {
            throw new IllegalArgumentException("No reports found for the given student, trimester, and academic year");
        }
        
        // Build bulletin request from reports
        BulletinRequest request = buildBulletinRequestFromReports(reports);
        
        return generateBulletinPdf(request);
    }

    /**
     * Generate bulletin PDF from database using academic data ID
     */
    public byte[] generateBulletinFromAcademicData(java.util.UUID studentId, java.util.UUID academicDataId) 
            throws DocumentException, IOException {
        
        // Fetch reports from database (only published) for the given student and academic data
        List<uruhingore.ua.model.Report> reports = reportRepository.findPublishedByStudentIdAndAcademicDataId(
                studentId, academicDataId);
        
        if (reports.isEmpty()) {
            throw new IllegalArgumentException("No reports found for the given student and academic data");
        }
        
        // Build bulletin request from reports
        BulletinRequest request = buildBulletinRequestFromReports(reports);
        
        return generateBulletinPdf(request);
    }

    /**
     * Generate grid-based bulletin with color-filled cells for all trimesters (matching the image design)
     */
    public byte[] generateGridBulletin(java.util.UUID studentId, Integer academicYear, 
            String classe) throws DocumentException, IOException {
        
        // Fetch student information
        uruhingore.ua.model.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
        
        // Get all active modules (these are the ATELIERS)
        List<uruhingore.ua.model.Module> modules = moduleRepository.findByActiveOrderByIndexOrder(true);
        
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("No active modules found. Please add modules first.");
        }
        
        // Trimesters are FIRST, SECOND, THIRD
        List<Trimester> trimesters = java.util.Arrays.asList(Trimester.FIRST, Trimester.SECOND, Trimester.THIRD);
        
        // Fetch all published reports for this student and academic year
        List<uruhingore.ua.model.Report> allReports = reportRepository.findPublishedByStudentIdAndAcademicYear(
                studentId, academicYear);
        
        // Generate PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Build the grid bulletin
        buildGridBulletinDocument(document, student, academicYear, classe, modules, trimesters, allReports);
        
        document.close();
        
        return baos.toByteArray();
    }

    /**
     * Build grid-based bulletin document (ATELIERS x TRIMESTERS grid with colored cells)
     */
    private void buildGridBulletinDocument(Document document, uruhingore.ua.model.Student student,
            Integer academicYear, String classe,
            List<uruhingore.ua.model.Module> modules,
            List<Trimester> trimesters,
            List<uruhingore.ua.model.Report> reports) throws DocumentException {
        
        addHeader(document);
        addContactInfo(document);
        addLocationInfo(document);
        addClassInfo(document, classe != null ? classe : "N/A", String.valueOf(academicYear));
        
        // Student name
        String studentFullName = student.getFirstName() + " " + student.getLastName();
        Paragraph studentName = new Paragraph("NOM DE L'ELEVE: " + studentFullName, FONT_BOLD_10);
        studentName.setSpacingAfter(15f);
        document.add(studentName);
        
        // Add the ATELIERS grid
        addAteliersGrid(document, modules, trimesters, reports);
        
        // Add grade color legend
        addColorLegend(document);
        
        // Signature section
        addSignatureSection(document);
    }

    /**
     * Add ATELIERS grid with color-coded cells for each trimester
     */
    private void addAteliersGrid(Document document, List<uruhingore.ua.model.Module> modules,
            List<Trimester> trimesters,
            List<uruhingore.ua.model.Report> reports) throws DocumentException {
        
        // Number of columns = 1 (ATELIERS label) + number of trimesters
        int numCols = 1 + trimesters.size();
        PdfPTable table = new PdfPTable(numCols);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15f);
        
        // Set column widths: ATELIERS column wider, trimester columns equal
        float[] widths = new float[numCols];
        widths[0] = 4f; // ATELIERS column
        for (int i = 1; i < numCols; i++) {
            widths[i] = 2f; // Each trimester column
        }
        table.setWidths(widths);
        
        // Header row
        PdfPCell ateliersHeader = new PdfPCell(new Phrase("ATELIERS", FONT_BOLD_9));
        ateliersHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        ateliersHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        ateliersHeader.setBackgroundColor(new BaseColor(200, 200, 200)); // Light gray
        ateliersHeader.setPadding(5f);
        table.addCell(ateliersHeader);
        
        // Trimester headers
        for (Trimester trimester : trimesters) {
            String trimesterName = "TRIMESTRE " + getRomanNumeral(trimester.getValue());
            PdfPCell trimesterHeader = new PdfPCell(new Phrase(trimesterName, FONT_BOLD_9));
            trimesterHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            trimesterHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
            trimesterHeader.setBackgroundColor(new BaseColor(144, 238, 144)); // Light green
            trimesterHeader.setPadding(5f);
            table.addCell(trimesterHeader);
        }
        
        // Create a map for quick report lookup: moduleId -> trimester -> report
        Map<java.util.UUID, Map<Trimester, uruhingore.ua.model.Report>> reportMap = new java.util.HashMap<>();
        for (uruhingore.ua.model.Report report : reports) {
            if (report.getAcademicData() != null) {
                reportMap.computeIfAbsent(report.getModule().getId(), k -> new java.util.HashMap<>())
                        .put(report.getAcademicData().getTrimester(), report);
            }
        }
        
        // Module rows (ATELIERS)
        for (uruhingore.ua.model.Module module : modules) {
            // ATELIER name cell
            String atelierName = module.getName();
            
            PdfPCell atelierCell = new PdfPCell(new Phrase(atelierName, FONT_NORMAL_9));
            atelierCell.setPadding(5f);
            atelierCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(atelierCell);
            
            // Trimester cells (colored based on score)
            for (Trimester trimester : trimesters) {
                PdfPCell scoreCell = new PdfPCell();
                scoreCell.setFixedHeight(25f);
                scoreCell.setPadding(5f);
                
                // Find report for this module and trimester
                uruhingore.ua.model.Report report = reportMap.getOrDefault(module.getId(), new java.util.HashMap<>())
                        .get(trimester);
                
                if (report != null) {
                    // Fill cell with color based on score
                    BaseColor color = getGradeColor(report.getScore());
                    scoreCell.setBackgroundColor(color);
                    
                    // Optionally add score text in white
                    Phrase scoreText = new Phrase(String.valueOf(report.getScore()), 
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE));
                    scoreCell.setPhrase(scoreText);
                    scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    scoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                } else {
                    // Empty cell (no report yet)
                    scoreCell.setBackgroundColor(BaseColor.WHITE);
                }
                
                table.addCell(scoreCell);
            }
        }
        
        document.add(table);
    }
    
    /**
     * Convert integer to Roman numeral (1 -> I, 2 -> II, 3 -> III)
     */
    private String getRomanNumeral(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            default: return String.valueOf(number);
        }
    }

    /**
     * Add color legend (SYSTEME DE GRADE)
     */
    private void addColorLegend(Document document) throws DocumentException {
        Paragraph legendTitle = new Paragraph("SYSTEME DE GRADE", FONT_BOLD_9);
        legendTitle.setSpacingBefore(10f);
        legendTitle.setSpacingAfter(5f);
        document.add(legendTitle);
        
        PdfPTable legendTable = new PdfPTable(4);
        legendTable.setWidthPercentage(80);
        legendTable.setSpacingAfter(15f);
        
        addGradingCell(legendTable, "80-100", new BaseColor(0, 176, 80));    // Green
        addGradingCell(legendTable, "70-79", new BaseColor(0, 112, 192));    // Blue
        addGradingCell(legendTable, "50-69", new BaseColor(255, 192, 0));    // Yellow/Orange
        addGradingCell(legendTable, "0-49", new BaseColor(255, 0, 0));       // Red
        
        document.add(legendTable);
    }

    /**
     * Generate blank bulletin template with all modules (for teachers to fill in marks)
     */
    public byte[] generateBulletinTemplate(java.util.UUID studentId, Trimester trimester, 
            Integer academicYear, String classe) throws DocumentException, IOException {
        
        // Fetch student information
        uruhingore.ua.model.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
        
        // Get all active modules
        List<uruhingore.ua.model.Module> modules = moduleRepository.findByActiveOrderByIndexOrder(true);
        
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("No active modules found. Please add modules first.");
        }
        
        // Build module grades with empty scores
        List<ModuleGradeDto> moduleGrades = new ArrayList<>();
        for (uruhingore.ua.model.Module module : modules) {
            ModuleGradeDto gradeDto = ModuleGradeDto.builder()
                    .moduleName(module.getName())
                    .score(null) // Empty score for template
                    .gradeColor(null)
                    .build();
            moduleGrades.add(gradeDto);
        }
        
        // Build bulletin request
        String studentFullName = student.getFirstName() + " " + student.getLastName();
        String trimesterName = "TRIMESTRE " + getRomanNumeral(trimester.getValue());
        BulletinRequest request = BulletinRequest.builder()
                .studentId(studentId)
                .studentName(studentFullName)
                .classe(classe != null ? classe : "N/A")
                .annee(String.valueOf(academicYear))
                .trimester(trimesterName)
                .comment("") // Empty comment for template
                .moduleGrades(moduleGrades)
                .build();
        
        return generateBulletinPdf(request);
    }

    /**
     * Build BulletinRequest from database reports
     */
    private BulletinRequest buildBulletinRequestFromReports(List<uruhingore.ua.model.Report> reports) {
        if (reports.isEmpty()) {
            throw new IllegalArgumentException("Reports list cannot be empty");
        }
        
        uruhingore.ua.model.Report firstReport = reports.get(0);
        uruhingore.ua.model.Student student = firstReport.getStudent();
        
        if (firstReport.getAcademicData() == null) {
            throw new IllegalArgumentException("Report must have AcademicData");
        }
        
        Trimester trimester = firstReport.getAcademicData().getTrimester();
        Integer academicYear = firstReport.getAcademicData().getAcademicYear();
        
        // Create ModuleGradeDto list from reports
        List<ModuleGradeDto> moduleGrades = new ArrayList<>();
        
        for (uruhingore.ua.model.Report report : reports) {
            uruhingore.ua.model.Module module = report.getModule();
            String moduleName = module.getName();
            
            ModuleGradeDto gradeDto = ModuleGradeDto.builder()
                    .moduleName(moduleName)
                    .score(report.getScore())
                    .gradeColor(report.getGradeColor())
                    .build();
            
            moduleGrades.add(gradeDto);
        }
        
        // Get teacher comment from the first report (or combine them if needed)
        String comment = firstReport.getTeacherComment();
        String classe = firstReport.getClassLevel() != null ? firstReport.getClassLevel().getDisplayName() : "N/A";
        String studentFullName = student.getFirstName() + " " + student.getLastName();
        String trimesterName = "TRIMESTRE " + getRomanNumeral(trimester.getValue());
        
        return BulletinRequest.builder()
                .studentId(student.getId())
                .studentName(studentFullName)
                .classe(classe)
                .annee(String.valueOf(academicYear))
                .trimester(trimesterName)
                .comment(comment)
                .moduleGrades(moduleGrades)
                .build();
    }

    /**
     * Generate bulletin PDF without grades (backward compatibility)
     */
    public byte[] generateBulletinPdf(BulletinRequest request) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        buildBulletinDocument(document, request, null);

        document.close();

        return baos.toByteArray();
    }

    /**
     * Generate bulletin PDF with grades (backward compatibility)
     */
    public byte[] generateBulletinWithGrades(BulletinRequest request) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        buildBulletinDocument(document, request, request.getGrades());

        document.close();

        return baos.toByteArray();
    }

    /**
     * Build the complete bulletin document
     */
    private void buildBulletinDocument(Document document, BulletinRequest request,
            Map<String, SubjectGrade> grades) throws DocumentException {
        addHeader(document);
        addContactInfo(document);
        addLocationInfo(document);
        addClassInfo(document, request.getClasse(), request.getAnnee());
        addBulletinTitle(document, request.getTrimester());
        addStudentName(document, request.getStudentName());
        
        // Use module grades if available, otherwise fall back to legacy grades
        if (request.getModuleGrades() != null && !request.getModuleGrades().isEmpty()) {
            addDynamicAcademicTable(document, request.getModuleGrades(), request.getTrimester());
        } else {
            addAcademicTable(document, grades);
        }
        
        addCommentSection(document, request.getComment());
        addSignatureSection(document);
    }

    private void addHeader(Document document) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(10f);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        Paragraph logoPara = new Paragraph("URUHONGORE ACADEMY", FONT_BOLD_12);
        logoCell.addElement(logoPara);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);

        headerTable.addCell(logoCell);
        headerTable.addCell(emptyCell);

        document.add(headerTable);
    }

    private void addContactInfo(Document document) throws DocumentException {
        Paragraph contact = new Paragraph("TEL: 0784696074/0786064017", FONT_NORMAL_10);
        contact.setAlignment(Element.ALIGN_CENTER);
        contact.setSpacingAfter(15f);
        document.add(contact);
    }

    private void addLocationInfo(Document document) throws DocumentException {
        PdfPTable locationTable = new PdfPTable(2);
        locationTable.setWidthPercentage(100);
        locationTable.setSpacingAfter(10f);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("DISTRICT: KICUKIRO", FONT_BOLD_10));
        leftCell.addElement(new Paragraph("VILLAGE: NYANZA", FONT_BOLD_10));

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.addElement(new Paragraph("SECTEUR: GATENGA", FONT_BOLD_10));
        rightCell.addElement(new Paragraph("VILLAGE: JURU", FONT_BOLD_10));

        locationTable.addCell(leftCell);
        locationTable.addCell(rightCell);

        document.add(locationTable);
    }

    private void addClassInfo(Document document, String classe, String annee) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(10f);

        PdfPCell classeCell = new PdfPCell(new Phrase("CLASSE: " + classe, FONT_BOLD_10));
        classeCell.setBorder(Rectangle.NO_BORDER);

        PdfPCell anneeCell = new PdfPCell(new Phrase("ANNEE SCOLAIRE: " + annee, FONT_BOLD_10));
        anneeCell.setBorder(Rectangle.NO_BORDER);
        anneeCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        infoTable.addCell(classeCell);
        infoTable.addCell(anneeCell);

        document.add(infoTable);
    }

    private void addBulletinTitle(Document document, String trimester) throws DocumentException {
        String trimesterText = trimester != null ? trimester : "MI-TRIMESTRE";
        Paragraph title = new Paragraph("BULLETIN DU    " + trimesterText.toUpperCase(), FONT_BOLD_12);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15f);
        document.add(title);
    }

    private void addStudentName(Document document, String studentName) throws DocumentException {
        PdfPTable nameTable = new PdfPTable(2);
        nameTable.setWidthPercentage(100);
        nameTable.setSpacingAfter(10f);
        nameTable.setWidths(new float[] { 1f, 2f });

        PdfPCell labelCell = new PdfPCell(new Phrase("NOM DE L'ELEVE", FONT_BOLD_10));
        labelCell.setPadding(5f);

        PdfPCell nameCell = new PdfPCell(new Phrase(studentName, FONT_NORMAL_10));
        nameCell.setPadding(5f);

        nameTable.addCell(labelCell);
        nameTable.addCell(nameCell);

        document.add(nameTable);
    }

    private void addAcademicTable(Document document, Map<String, SubjectGrade> grades) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15f);
        table.setWidths(new float[] { 4f, 3f, 3f });

        // Header Row
        PdfPCell domainHeader = new PdfPCell(new Phrase("DOMAINE D'APPRENTISSAGE", FONT_BOLD_9));
        domainHeader.setColspan(2);
        domainHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(domainHeader);

        PdfPCell trimesterHeader = new PdfPCell(new Phrase("I TRIMESTRE", FONT_BOLD_9));
        trimesterHeader.setBackgroundColor(new BaseColor(0, 176, 80)); // Green
        trimesterHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(trimesterHeader);

        // Subjects
        addSubjectRow(table, "Pré- Mathématiques", "", getGrade(grades, "math"), 1);

        addSubjectRowWithRowspan(table, "Langage", "Pré- lecture", getGrade(grades, "lecture"), 2);
        addSubjectRow(table, null, "Pré- écriture", getGrade(grades, "ecriture"), 0);

        addSubjectRow(table, "Exploratrice découverte", "Comportement", getGrade(grades, "decouverte"), 1);
        addSubjectRow(table, "Développement social et emotionnel", "", getGrade(grades, "comportement"), 1);

        addSubjectRowWithRowspan(table, "Développement physiques et sanitaire", "Gymnastiques",
                getGrade(grades, "gymnastiques"), 3);
        addSubjectRow(table, null, "Structuration spatiale", getGrade(grades, "spatiale"), 0);
        addSubjectRow(table, null, "Vie pratique", getGrade(grades, "viePratique"), 0);

        addSubjectRowWithRowspan(table, "Art et Culture", "Dessin", getGrade(grades, "dessin"), 4);
        addSubjectRow(table, null, "Coloriage", getGrade(grades, "coloriage"), 0);
        addSubjectRow(table, null, "Modelage", getGrade(grades, "modelage"), 0);
        addSubjectRow(table, null, "Musique", getGrade(grades, "musique"), 0);

        addGradingRow(table);

        document.add(table);
    }

    /**
     * Add dynamic academic table based on database modules and reports
     */
    private void addDynamicAcademicTable(Document document, List<ModuleGradeDto> moduleGrades, String trimester) 
            throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15f);
        table.setWidths(new float[] { 4f, 3f, 3f });

        // Header Row
        PdfPCell domainHeader = new PdfPCell(new Phrase("DOMAINE D'APPRENTISSAGE", FONT_BOLD_9));
        domainHeader.setColspan(2);
        domainHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(domainHeader);

        String trimesterText = trimester != null ? trimester.toUpperCase() : "I TRIMESTRE";
        PdfPCell trimesterHeader = new PdfPCell(new Phrase(trimesterText, FONT_BOLD_9));
        trimesterHeader.setBackgroundColor(new BaseColor(0, 176, 80)); // Green
        trimesterHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(trimesterHeader);

        // Add rows for each module
        for (ModuleGradeDto moduleGrade : moduleGrades) {
            addSubjectRow(table, moduleGrade.getModuleName(), "", getScoreString(moduleGrade.getScore()), 1);
        }

        addGradingRow(table);
        document.add(table);
    }

    /**
     * Convert score to string, handling null values
     */
    private String getScoreString(Integer score) {
        return score != null ? String.valueOf(score) : "";
    }

    private String getGrade(Map<String, SubjectGrade> grades, String subject) {
        if (grades != null && grades.containsKey(subject)) {
            return String.valueOf(grades.get(subject).getScore());
        }
        return "";
    }

    private void addSubjectRow(PdfPTable table, String domain, String subject, String grade, int domainRowspan) {
        if (domain != null) {
            PdfPCell domainCell = new PdfPCell(new Phrase(domain, FONT_NORMAL_9));
            if (domainRowspan > 1) {
                domainCell.setRowspan(domainRowspan);
            }
            domainCell.setPadding(5f);
            domainCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(domainCell);
        }

        PdfPCell subjectCell = new PdfPCell(new Phrase(subject, FONT_NORMAL_9));
        subjectCell.setPadding(5f);
        table.addCell(subjectCell);

        PdfPCell gradeCell = new PdfPCell(new Phrase(grade, FONT_NORMAL_9));
        gradeCell.setPadding(5f);
        gradeCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        if (!grade.isEmpty()) {
            try {
                double score = Double.parseDouble(grade);
                gradeCell.setBackgroundColor(getGradeColor(score));
            } catch (NumberFormatException e) {
                // Keep default background
            }
        }
        table.addCell(gradeCell);
    }

    private void addSubjectRowWithRowspan(PdfPTable table, String domain, String subject, String grade, int rowspan) {
        PdfPCell domainCell = new PdfPCell(new Phrase(domain, FONT_NORMAL_9));
        domainCell.setRowspan(rowspan);
        domainCell.setPadding(5f);
        domainCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(domainCell);

        addSubjectRow(table, null, subject, grade, 0);
    }

    private BaseColor getGradeColor(double score) {
        if (score >= 80) {
            return new BaseColor(0, 176, 80); // Green - #00B050
        } else if (score >= 70) {
            return new BaseColor(0, 112, 192); // Blue - #0070C0
        } else if (score >= 50) {
            return new BaseColor(255, 192, 0); // Yellow - #FFC000
        } else {
            return new BaseColor(255, 0, 0); // Red - #FF0000
        }
    }

    private void addGradingRow(PdfPTable table) throws DocumentException {
        PdfPCell systemCell = new PdfPCell(new Phrase("SYSTEME DE GRADE", FONT_BOLD_9));
        systemCell.setPadding(5f);
        systemCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(systemCell);

        PdfPTable gradingTable = new PdfPTable(4);
        gradingTable.setWidthPercentage(100);

        addGradingCell(gradingTable, "80-100", new BaseColor(0, 176, 80));
        addGradingCell(gradingTable, "70-79", new BaseColor(0, 112, 192));
        addGradingCell(gradingTable, "50-69", new BaseColor(255, 192, 0));
        addGradingCell(gradingTable, "0-49", new BaseColor(255, 0, 0));

        PdfPCell nestedCell = new PdfPCell(gradingTable);
        nestedCell.setColspan(2);
        nestedCell.setPadding(0f);

        table.addCell(nestedCell);
    }

    private void addGradingCell(PdfPTable table, String text, BaseColor color) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_NORMAL_8));
        cell.setBackgroundColor(color);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private void addCommentSection(Document document, String comment) throws DocumentException {
        PdfPTable commentTable = new PdfPTable(1);
        commentTable.setWidthPercentage(100);
        commentTable.setSpacingBefore(10f);
        commentTable.setSpacingAfter(5f);

        PdfPCell headerCell = new PdfPCell(new Phrase("Commentaire", FONT_BOLD_9));
        headerCell.setPadding(5f);
        commentTable.addCell(headerCell);

        PdfPCell contentCell = new PdfPCell(new Phrase(comment != null ? comment : "", FONT_NORMAL_9));
        contentCell.setFixedHeight(30f);
        contentCell.setPadding(5f);
        commentTable.addCell(contentCell);

        document.add(commentTable);
    }

    private void addSignatureSection(Document document) throws DocumentException {
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setSpacingBefore(10f);
        signatureTable.setSpacingAfter(20f);
        signatureTable.setWidths(new float[] { 1f, 1f });

        PdfPCell parentsCell = new PdfPCell(
                new Phrase("Signature des parents: .................................", FONT_NORMAL_9));
        parentsCell.setBorder(Rectangle.NO_BORDER);
        parentsCell.setFixedHeight(30f);
        signatureTable.addCell(parentsCell);

        PdfPCell titulaireCell = new PdfPCell(
                new Phrase("Signature de Titulaire: .................................", FONT_NORMAL_9));
        titulaireCell.setBorder(Rectangle.NO_BORDER);
        titulaireCell.setFixedHeight(30f);
        signatureTable.addCell(titulaireCell);

        document.add(signatureTable);

        Paragraph director = new Paragraph(
                "NOM DE LA DIRECTRICE: .................................\nSIGNATURE ET CACHET DE L'ECOLE",
                FONT_BOLD_10);
        director.setAlignment(Element.ALIGN_RIGHT);
        document.add(director);
    }
}
