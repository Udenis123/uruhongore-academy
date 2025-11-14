package uruhingore.ua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMarkRequest {
    private Integer score; // Optional - only update if provided
    private String teacherComment; // Optional
    private String classe; // Optional
}
