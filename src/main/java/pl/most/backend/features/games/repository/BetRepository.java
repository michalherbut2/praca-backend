package pl.most.backend.features.games.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.most.backend.features.games.model.Bet;
import pl.most.backend.features.games.enums.BetStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface BetRepository extends JpaRepository<Bet, UUID> {

    List<Bet> findByStatus(BetStatus status);

    List<Bet> findByCreatorId(UUID creatorId);

    @Query("SELECT b FROM Bet b WHERE b.status = 'OPEN' AND b.bettingDeadline < :now")
    List<Bet> findExpiredOpenBets(LocalDateTime now);

    @Query("SELECT b FROM Bet b WHERE b.status IN ('OPEN', 'LOCKED') ORDER BY b.createdAt DESC")
    List<Bet> findActiveBets();

    @Query("SELECT b FROM Bet b LEFT JOIN FETCH b.entries WHERE b.id = :betId")
    Bet findByIdWithEntries(UUID betId);


    @Query("SELECT b FROM Bet b JOIN b.entries e WHERE b.status IN ('RESOLVED') ORDER BY b.createdAt DESC")
    List<Bet> findSettledBets();

    List<Bet> findByStatusInOrderByResolutionDateDesc(Collection<BetStatus> statuses);
//    List<Bet> findResolvedBets();


}
