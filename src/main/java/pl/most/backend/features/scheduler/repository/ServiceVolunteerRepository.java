package pl.most.backend.features.scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.most.backend.features.scheduler.model.ServiceVolunteer;
import pl.most.backend.features.scheduler.model.ServiceVolunteerStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceVolunteerRepository extends JpaRepository<ServiceVolunteer, UUID> {

    Optional<ServiceVolunteer> findBySlotIdAndUserId(UUID slotId, UUID userId);

    long countBySlotIdAndStatus(UUID slotId, ServiceVolunteerStatus status);

    boolean existsBySlotIdAndUserId(UUID slotId, UUID userId);
}
