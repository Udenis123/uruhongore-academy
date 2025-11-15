package uruhingore.ua.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uruhingore.ua.model.Period;
import uruhingore.ua.model.Trimester;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicDataRequest {
    
    @NotNull(message = "Trimester is required")
    private Trimester trimester;
    
    @NotNull(message = "Academic year is required")
    private Integer academicYear;
    
    @NotNull(message = "Period is required")
    private Period period;
    
    private Boolean published;
}

