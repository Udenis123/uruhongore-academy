package uruhingore.ua.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uruhingore.ua.model.AcademicData;
import uruhingore.ua.model.Period;
import uruhingore.ua.model.Trimester;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicDataRepository extends JpaRepository<AcademicData, UUID> {

    Optional<AcademicData> findByTrimesterAndAcademicYearAndPeriod(
            Trimester trimester, 
            Integer academicYear, 
            Period period
    );

    List<AcademicData> findByPublishedTrue();

    List<AcademicData> findByPublishedFalse();

    List<AcademicData> findByAcademicYear(Integer academicYear);

    List<AcademicData> findByTrimesterAndAcademicYear(Trimester trimester, Integer academicYear);

    List<AcademicData> findByPeriodAndAcademicYear(Period period, Integer academicYear);

    @Query("SELECT ad FROM AcademicData ad WHERE ad.published = true ORDER BY ad.academicYear DESC, ad.trimester, ad.period")
    List<AcademicData> findAllPublishedOrdered();

    @Query("SELECT ad FROM AcademicData ad ORDER BY ad.createdAt DESC")
    List<AcademicData> findAllOrderByCreatedAtDesc();

    @Query("SELECT ad FROM AcademicData ad ORDER BY ad.createdAt ASC")
    List<AcademicData> findAllOrderByCreatedAtAsc();

    @Query("SELECT ad FROM AcademicData ad WHERE ad.published = false ORDER BY ad.createdAt DESC")
    List<AcademicData> findAllUnpublishedOrderByCreatedAtDesc();
}

