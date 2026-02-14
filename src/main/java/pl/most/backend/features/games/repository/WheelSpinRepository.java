package pl.most.backend.features.games.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.most.backend.features.games.model.WheelSpin;
import pl.most.backend.model.entity.User;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WheelSpinRepository extends JpaRepository<WheelSpin, UUID> {

    Optional<WheelSpin> findByUserIdAndSpinDate(UUID userId, LocalDate spinDate);

    boolean existsByUserIdAndSpinDate(UUID userId, LocalDate spinDate);
    boolean existsByUserAndSpinDate(User user, LocalDate spinDate);
}
