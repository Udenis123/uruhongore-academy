package uruhingore.ua.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uruhingore.ua.model.Report;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByStudentId(UUID studentId);

    List<Report> findByStudentIdAndTrimester(UUID studentId, Integer trimester);
    
    List<Report> findByStudentIdAndTrimesterAndAcademicYear(UUID studentId, Integer trimester, Integer academicYear);
    
    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.trimester = :trimester AND r.academicYear = :academicYear ORDER BY r.module.indexOrder")
    List<Report> findReportsForBulletin(@Param("studentId") UUID studentId, 
                                       @Param("trimester") Integer trimester,
                                       @Param("academicYear") Integer academicYear);
    
    List<Report> findByStudentIdAndAcademicYear(UUID studentId, Integer academicYear);
    
    // Check if report already exists for student, module, trimester, and year
    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.module.id = :moduleId AND r.trimester = :trimester AND r.academicYear = :academicYear")
    List<Report> findByStudentAndModuleAndTrimesterAndYear(@Param("studentId") UUID studentId,
                                                           @Param("moduleId") UUID moduleId,
                                                           @Param("trimester") Integer trimester,
                                                           @Param("academicYear") Integer academicYear);
}
