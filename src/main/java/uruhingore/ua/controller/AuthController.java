package uruhingore.ua.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import uruhingore.ua.dto.Loginbody;
import uruhingore.ua.dto.AuthResponse;
import uruhingore.ua.dto.RegisterBody;
import uruhingore.ua.dto.UserResponse;
import uruhingore.ua.model.Users;
import uruhingore.ua.repository.UserRepository;
import uruhingore.ua.service.AuthServices;
import uruhingore.ua.service.JwtService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthServices authServices;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterBody userBody) {
       return ResponseEntity.ok(authServices.createUser(userBody));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody Loginbody loginbody) {
        return authServices.login(loginbody);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<Users> allUsers = usersRepository.findAll();

        log.info("Total users found in database: {}", allUsers.size());

        if (allUsers.isEmpty()) {
            log.warn("No users found in database!");
            return ResponseEntity.ok(List.of());
        }

        List<UserResponse> users = allUsers.stream()
                .peek(user -> log.debug("Mapping user: ID={}, Phone={}, FullName={}",
                        user.getId(), user.getPhone(), user.getFullName()))
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());

        log.info("Successfully mapped {} users to UserResponse", users.size());
        return ResponseEntity.ok(users);
    }

    
}