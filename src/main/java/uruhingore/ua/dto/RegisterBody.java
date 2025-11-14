package uruhingore.ua.dto;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import uruhingore.ua.model.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Getter
@Setter
public class RegisterBody {
    @NotBlank(message = "Full name is Required")
    private String fullName;
    @NotBlank(message = "please specify your gender")
    private String gender;
    @Email(message = "Provide valid Email")
    private String email;
    @Size(min = 10 ,max = 15 ,message = "Please input number between 10 to 15.")
    private String phone;
    private String address;
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "Password must be at least 8 characters long and include at least one letter and one number"
    )
    private String password;
    @NotNull(message = "Role is required")
    private Role role;
}
