package uruhingore.ua.model;


import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    HEAD,
    STUDENT,
    TEACHER,
    PARENTS;

    @Override
    public String getAuthority() {
        return name();
    }
}