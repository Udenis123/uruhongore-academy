package uruhingore.ua.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uruhingore.ua.model.Role;
import uruhingore.ua.model.Users;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private UUID id;
    private String fullName;
    private String phone;
    private String email;
    private Set<Role> role;
    private Boolean enabled;

    public static UserResponse fromUser(Users user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .role(user.getRoles())
                .build();
    }
}