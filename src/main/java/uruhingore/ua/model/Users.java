package uruhingore.ua.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_phone", columnList = "phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String fullName;
    private String gender;
    @Column(unique = true)
    private String email;
    @Column(nullable = false, unique = true)
    private String phone;
    private String address;
    @Column(nullable = false)
    private String password;

    @Builder.Default
    private boolean enabled = true;
    @Builder.Default
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relationship with Students (for parents)
    @ManyToMany(mappedBy = "parents")
    @Builder.Default
    private Set<Student> children = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getUsername() {
        return phone; // parents log in using phone
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


    public boolean isParent() {
        return roles.contains(Role.PARENTS);
    }

    public boolean isStudent() {
        return roles.contains(Role.STUDENT);
    }

    public boolean isHead() {
        return roles.contains(Role.HEAD);
    }

    public boolean canAccessStudent(UUID studentId) {
        if (isHead()) return true;
        if (isParent()) {
            return children != null && children.stream()
                    .anyMatch(student -> student.getId().equals(studentId));
        }
        return false;
    }

    // Helper method to add a child/student to parent
    public void addChild(Student student) {
        if (this.children == null) {
            this.children = new HashSet<>();
        }
        this.children.add(student);
        student.getParents().add(this);
    }

    // Helper method to remove a child/student from parent
    public void removeChild(Student student) {
        if (this.children != null) {
            this.children.remove(student);
            student.getParents().remove(this);
        }
    }
}