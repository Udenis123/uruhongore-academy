package uruhingore.ua.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uruhingore.ua.model.AcademicData;
import uruhingore.ua.model.Period;
import uruhingore.ua.model.Report;
import uruhingore.ua.model.Trimester;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    // Find all reports for a student (only published)
    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.academicData.published = true")
    List<Report> findPublishedByStudentId(@Param("studentId") UUID studentId);

    // Find reports by student and academic data (only published)
    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.academicData.id = :academicDataId AND r.academicData.published = true")
    List<Report> findPublishedByStudentIdAndAcademicDataId(@Param("studentId") UUID studentId, @Param("academicDataId") UUID academicDataId);

    // Find reports by student, trimester, and year (only published)
    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.academicData.trimester = :trimester AND r.academicData.academicYear = :academicYear AND r.academicData.published = true")
    List<Report> findPublishedByStudentIdAndTrimesterAndAcademicYear(
            @Param("studentId") UUID studentId,
            @Param("trimester") Trimester trimester,
            @Param("academicYear") Integer academicYear
    );

    // Find reports for bulletin (only published)
    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.academicData.trimester = :trimester AND r.academicData.academicYear = :academicYear AND r.academicData.published = true ORDER BY r.module.indexOrder")
    List<Report> findPublishedReportsForBulletin(
            @Param("studentId") UUID studentId,
            @Param("trimester") Trimester trimester,
            @Param("academicYear") Integer academicYear
    );

    // Find reports by student and year (only published)
    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.academicData.academicYear = :academicYear AND r.academicData.published = true")
    List<Report> findPublishedByStudentIdAndAcademicYear(@Param("studentId") UUID studentId, @Param("academicYear") Integer academicYear);

    // Check if report already exists for student, module, and academic data
    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.module.id = :moduleId AND r.academicData.id = :academicDataId")
    List<Report> findByStudentAndModuleAndAcademicData(
            @Param("studentId") UUID studentId,
            @Param("moduleId") UUID moduleId,
            @Param("academicDataId") UUID academicDataId
    );

    // Admin/Teacher methods (all reports, including unpublished)
    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId")
    List<Report> findAllByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.academicData.trimester = :trimester AND r.academicData.academicYear = :academicYear")
    List<Report> findAllByStudentIdAndTrimesterAndAcademicYear(
            @Param("studentId") UUID studentId,
            @Param("trimester") Trimester trimester,
            @Param("academicYear") Integer academicYear
    );

    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.academicData.academicYear = :academicYear")
    List<Report> findAllByStudentIdAndAcademicYear(@Param("studentId") UUID studentId, @Param("academicYear") Integer academicYear);
}
