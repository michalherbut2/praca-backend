package pl.most.backend.features.intentions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.most.backend.features.intentions.model.Intention;
import pl.most.backend.features.intentions.model.IntentionStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IntentionRepository extends JpaRepository<Intention, UUID> {

    // Dla użytkownika: Moje intencje posortowane od najnowszych
    List<Intention> findAllByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    // Dla Admina: Pobieranie intencji na konkretny dzień (np. do druku)
    List<Intention> findAllByTargetDateAndStatus(LocalDate date, IntentionStatus status);

    // Dla Admina: Wszystkie oczekujące (PENDING)
    List<Intention> findAllByStatusOrderByTargetDateAsc(IntentionStatus status);

    List<Intention> findAllByStatusAndTargetDateLessThanEqual(IntentionStatus status, LocalDate today);
}