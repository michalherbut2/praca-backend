package pl.most.backend.repository;

import pl.most.backend.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByStartDateAfterOrderByStartDateAsc(LocalDateTime now);
    List<Event> findBySectionIdOrderByStartDateAsc(String sectionId);
    List<Event> findByCreatedByOrderByCreatedAtDesc(UUID userId);
    List<Event> findByAllowRsvpTrueAndStartDateAfterOrderByStartDateAsc(LocalDateTime date);
}