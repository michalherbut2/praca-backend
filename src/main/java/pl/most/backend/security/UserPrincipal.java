package pl.most.backend.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.most.backend.model.entity.User;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder // <--- To jest ta "lepsza" część (wygodne tworzenie)
public class UserPrincipal implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L; // Wymagane przy Redis/Session clustering

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    // Metoda statyczna (Factory Method) - czysto i elegancko
    public static UserPrincipal create(User user) {
        // Konwersja Roli na Authority (np. ADMIN -> ROLE_ADMIN)
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return UserPrincipal.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .active(user.getIsActive())
                .build();
    }

    // --- Implementacja UserDetails (wymagane przez Springa) ---

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}