package pl.most.backend.repository;

import pl.most.backend.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByStartDateAfterOrderByStartDateAsc(LocalDateTime now);
    List<Event> findByPrzesloIdOrderByStartDateAsc(String przesloId);
    List<Event> findByCreatedByOrderByCreatedAtDesc(UUID userId);
}