package uruhingore.ua.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "academic_data", uniqueConstraints = {
    @UniqueConstraint(name = "uk_trimester_year_period", columnNames = {"trimester", "academic_year", "period"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Trimester trimester;

    @Column(nullable = false, name = "academic_year")
    private Integer academicYear; // e.g., 2024, 2025

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Period period;

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false; // Only published reports are visible

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

