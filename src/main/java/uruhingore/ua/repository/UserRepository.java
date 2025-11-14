package uruhingore.ua.repository;

import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uruhingore.ua.model.Users;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByPhone(String phone);
    
    Optional<Users> findByEmail(String email);

    boolean existsByPhone(@Pattern(
            regexp = "^\\+?[1-9]\\d{9,14}$",
            message = "Phone number must be between 10-15 digits"
    ) String phone);
    
    boolean existsByEmail(String email);
}
