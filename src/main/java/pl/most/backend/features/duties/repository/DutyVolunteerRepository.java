package pl.most.backend.features.duties.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.most.backend.features.duties.model.DutyVolunteer;
import pl.most.backend.features.duties.model.DutyVolunteerStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DutyVolunteerRepository extends JpaRepository<DutyVolunteer, UUID> {

    Optional<DutyVolunteer> findBySlotIdAndUserId(UUID slotId, UUID userId);

    long countBySlotIdAndStatus(UUID slotId, DutyVolunteerStatus status);

    boolean existsBySlotIdAndUserId(UUID slotId, UUID userId);
}
