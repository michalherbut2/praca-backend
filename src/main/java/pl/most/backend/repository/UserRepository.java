package pl.most.backend.repository;

import pl.most.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRole(User.Role role);
    List<User> findByPrzesloId(String przesloId);
    // ============================================
    // 6. Dodaj do UserRepository.java
    List<User> findTop20ByRoleInOrderByPointsDesc(List<User.Role> roles);

}