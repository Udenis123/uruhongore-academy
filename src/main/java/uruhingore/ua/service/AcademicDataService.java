package uruhingore.ua.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uruhingore.ua.dto.AcademicDataRequest;
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
     * Ordered by most recently created first
     */
    @Transactional(readOnly = true)
    public List<AcademicData> getAll() {
        return academicDataRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * Create new AcademicData
     */
    @Transactional
    public AcademicData createAcademicData(AcademicDataRequest request) {
        log.info("Creating AcademicData: Trimester={}, Year={}, Period={}", 
                request.getTrimester(), request.getAcademicYear(), request.getPeriod());
        
        // Check if already exists
        Optional<AcademicData> existing = academicDataRepository.findByTrimesterAndAcademicYearAndPeriod(
                request.getTrimester(), request.getAcademicYear(), request.getPeriod());
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "AcademicData already exists for Trimester=" + request.getTrimester() + 
                    ", Year=" + request.getAcademicYear() + ", Period=" + request.getPeriod());
        }
        
        AcademicData academicData = AcademicData.builder()
                .trimester(request.getTrimester())
                .academicYear(request.getAcademicYear())
                .period(request.getPeriod())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .build();
        
        AcademicData saved = academicDataRepository.save(academicData);
        log.info("Created new AcademicData with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Update existing AcademicData
     */
    @Transactional
    public AcademicData updateAcademicData(UUID id, AcademicDataRequest request) {
        log.info("Updating AcademicData: {}", id);
        
        AcademicData academicData = academicDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AcademicData not found with ID: " + id));
        
        // Check if updating to a combination that already exists (excluding current record)
        Optional<AcademicData> existing = academicDataRepository.findByTrimesterAndAcademicYearAndPeriod(
                request.getTrimester(), request.getAcademicYear(), request.getPeriod());
        
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "AcademicData already exists for Trimester=" + request.getTrimester() + 
                    ", Year=" + request.getAcademicYear() + ", Period=" + request.getPeriod());
        }
        
        academicData.setTrimester(request.getTrimester());
        academicData.setAcademicYear(request.getAcademicYear());
        academicData.setPeriod(request.getPeriod());
        
        if (request.getPublished() != null) {
            academicData.setPublished(request.getPublished());
        }
        
        AcademicData saved = academicDataRepository.save(academicData);
        log.info("Updated AcademicData with ID: {}", id);
        return saved;
    }

    /**
     * Delete AcademicData
     */
    @Transactional
    public void deleteAcademicData(UUID id) {
        log.info("Deleting AcademicData: {}", id);
        
        if (!academicDataRepository.existsById(id)) {
            throw new IllegalArgumentException("AcademicData not found with ID: " + id);
        }
        
        academicDataRepository.deleteById(id);
        log.info("Deleted AcademicData with ID: {}", id);
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

