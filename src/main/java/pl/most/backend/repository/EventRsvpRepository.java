// ============================================
// 2. EventRsvpRepository.java
package pl.most.backend.repository;

import pl.most.backend.model.entity.EventRsvp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRsvpRepository extends JpaRepository<EventRsvp, String> {

    Optional<EventRsvp> findByEventIdAndUserId(String eventId, UUID userId);

    List<EventRsvp> findByEventId(String eventId);

    List<EventRsvp> findByUserId(UUID userId);

    int countByEventIdAndStatus(String eventId, EventRsvp.Status status);

    void deleteByEventIdAndUserId(String eventId, UUID userId);
}
