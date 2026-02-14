package pl.most.backend.features.games.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.most.backend.features.games.model.BetEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BetEntryRepository extends JpaRepository<BetEntry, UUID> {

    Optional<BetEntry> findByUserIdAndBetId(UUID userId, UUID betId);

    List<BetEntry> findByUserId(UUID userId);

    List<BetEntry> findByBetId(UUID betId);

    boolean existsByUserIdAndBetId(UUID userId, UUID betId);
}
