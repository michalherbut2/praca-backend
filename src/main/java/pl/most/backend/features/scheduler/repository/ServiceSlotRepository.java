package pl.most.backend.features.scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.most.backend.features.scheduler.model.ServiceCategory;
import pl.most.backend.features.scheduler.model.ServiceSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceSlotRepository extends JpaRepository<ServiceSlot, UUID> {

    List<ServiceSlot> findAllByCategoryAndDateBetweenOrderByDateAscTimeAsc(
            ServiceCategory category, LocalDate dateFrom, LocalDate dateTo);

    List<ServiceSlot> findAllByCategoryAndDateOrderByTimeAsc(
            ServiceCategory category, LocalDate date);

    boolean existsByCategoryAndDate(ServiceCategory category, LocalDate date);
}
