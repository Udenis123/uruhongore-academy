package uruhingore.ua.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uruhingore.ua.dto.AuthResponse;
import uruhingore.ua.dto.Loginbody;
import uruhingore.ua.dto.RegisterBody;
import uruhingore.ua.dto.UserResponse;
import uruhingore.ua.model.Users;
import uruhingore.ua.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServices {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @Transactional
    public Users createUser(@Valid RegisterBody userBody) {
        // Check if user with this phone number already exists
        Users userExist = userRepository.findByPhone(userBody.getPhone()).orElse(null);
        if (userExist != null) {
            throw new IllegalArgumentException("User with phone number " + userBody.getPhone() + " already exists");
        }

        Users user = Users.builder()
                .email(userBody.getEmail())
                .password(passwordEncoder.encode(userBody.getPassword()))
                .fullName(userBody.getFullName())
                .gender(userBody.getGender())
                .phone(userBody.getPhone())
                .address(userBody.getAddress())
                .roles(Set.of(userBody.getRole()))
                .enabled(true)
                .active(true)
                .build();
        
        log.debug("User object built: id={}, phone={}, fullName={}", 
                user.getId(), user.getPhone(), user.getFullName());
        log.debug("Saving user to database...");
        
        Users savedUser = userRepository.save(user);
        
        log.info("User saved successfully with ID: {}", savedUser.getId());
        log.info("=== AuthServices.createUser END ===");
        
        return savedUser;
    }

    public ResponseEntity<AuthResponse> login(Loginbody loginbody) {
        Optional<Users> optionalUser = userRepository.findByPhone(loginbody.getPhone());

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("Invalid phone or password"));
        }

        Users user = optionalUser.get();
        if (!passwordEncoder.matches(loginbody.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("Invalid phone or password"));
        }

        // Generate JWT token
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.success(
                "Login successful",
                token,
                UserResponse.fromUser(user)
        ));
    }
}
