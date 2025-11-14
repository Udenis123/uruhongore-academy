package uruhingore.ua.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    // Constructor for creating references with just ID
    public Module(UUID id) {
        this.id = id;
    }

    @Column(nullable = false)
    private String name;

    private String category;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @Column(nullable = true)
    private Integer indexOrder = 0;
}
