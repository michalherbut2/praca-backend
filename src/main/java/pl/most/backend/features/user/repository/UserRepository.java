package pl.most.backend.features.user.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.most.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Modifying
    @Query("UPDATE User u SET u.points = u.points + :amount WHERE u.id = :userId")
    void updateUserPoints(@Param("userId") UUID userId, @Param("amount") Integer amount);

//    findTop20ByOrderByPointsDesc();

    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRole(User.Role role);
    List<User> findBySectionId(UUID sectionId);
    List<User> findTop20ByRoleInOrderByPointsDesc(List<User.Role> roles);

    List<User> findTop20ByOrderByPointsDesc();
}