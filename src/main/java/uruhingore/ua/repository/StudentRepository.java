package uruhingore.ua.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uruhingore.ua.model.ClassLevel;
import uruhingore.ua.model.Student;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    
    Optional<Student> findByStudentCode(String studentCode);
    
    List<Student> findByClassLevel(ClassLevel classLevel);
    
    List<Student> findByAcademicYear(String academicYear);
    
    List<Student> findByStatus(Student.StudentStatus status);
    
    @Query("SELECT s FROM Student s JOIN s.parents p WHERE p.id = :parentId")
    List<Student> findByParentId(@Param("parentId") UUID parentId);
    
    @Query("SELECT s FROM Student s WHERE s.classLevel = :classLevel AND s.academicYear = :academicYear")
    List<Student> findByClassLevelAndAcademicYear(
            @Param("classLevel") ClassLevel classLevel, 
            @Param("academicYear") String academicYear
    );
    
    boolean existsByStudentCode(String studentCode);
}
