package uruhingore.ua.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uruhingore.ua.model.AcademicData;
import uruhingore.ua.model.Period;
import uruhingore.ua.model.Trimester;
import uruhingore.ua.repository.AcademicDataRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicDataService {

    private final AcademicDataRepository academicDataRepository;

    /**
     * Create or get existing AcademicData
     */
    @Transactional
    public AcademicData getOrCreateAcademicData(Trimester trimester, Integer academicYear, Period period) {
        log.info("Getting or creating AcademicData: Trimester={}, Year={}, Period={}", trimester, academicYear, period);
        
        Optional<AcademicData> existing = academicDataRepository.findByTrimesterAndAcademicYearAndPeriod(
                trimester, academicYear, period);
        
        if (existing.isPresent()) {
            log.info("Found existing AcademicData with ID: {}", existing.get().getId());
            return existing.get();
        }
        
        AcademicData academicData = AcademicData.builder()
                .trimester(trimester)
                .academicYear(academicYear)
                .period(period)
                .published(false)
                .build();
        
        AcademicData saved = academicDataRepository.save(academicData);
        log.info("Created new AcademicData with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Get AcademicData by ID
     */
    @Transactional(readOnly = true)
    public AcademicData getAcademicDataById(UUID id) {
        return academicDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AcademicData not found with ID: " + id));
    }

    /**
     * Get all published AcademicData
     */
    @Transactional(readOnly = true)
    public List<AcademicData> getAllPublished() {
        return academicDataRepository.findByPublishedTrue();
    }

    /**
     * Get all AcademicData (including unpublished) - for admin/head
     */
    @Transactional(readOnly = true)
    public List<AcademicData> getAll() {
        return academicDataRepository.findAllPublishedOrdered();
    }

    /**
     * Publish AcademicData (makes reports visible to students/parents)
     */
    @Transactional
    public AcademicData publishAcademicData(UUID id) {
        log.info("Publishing AcademicData: {}", id);
        AcademicData academicData = academicDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AcademicData not found with ID: " + id));
        
        academicData.setPublished(true);
        AcademicData saved = academicDataRepository.save(academicData);
        log.info("AcademicData published successfully: {}", id);
        return saved;
    }

    /**
     * Unpublish AcademicData (hides reports from students/parents)
     */
    @Transactional
    public AcademicData unpublishAcademicData(UUID id) {
        log.info("Unpublishing AcademicData: {}", id);
        AcademicData academicData = academicDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AcademicData not found with ID: " + id));
        
        academicData.setPublished(false);
        AcademicData saved = academicDataRepository.save(academicData);
        log.info("AcademicData unpublished successfully: {}", id);
        return saved;
    }

    /**
     * Get AcademicData by trimester, year, and period
     */
    @Transactional(readOnly = true)
    public Optional<AcademicData> findByTrimesterAndYearAndPeriod(Trimester trimester, Integer academicYear, Period period) {
        return academicDataRepository.findByTrimesterAndAcademicYearAndPeriod(trimester, academicYear, period);
    }
}

