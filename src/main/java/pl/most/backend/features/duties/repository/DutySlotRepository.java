package pl.most.backend.features.duties.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.most.backend.features.duties.model.DutyCategory;
import pl.most.backend.features.duties.model.DutySlot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DutySlotRepository extends JpaRepository<DutySlot, UUID> {

    List<DutySlot> findAllByCategoryAndDateBetweenOrderByDateAscTimeAsc(
            DutyCategory category, LocalDate dateFrom, LocalDate dateTo);

    List<DutySlot> findAllByCategoryAndDateOrderByTimeAsc(
            DutyCategory category, LocalDate date);

    boolean existsByCategoryAndDate(DutyCategory category, LocalDate date);
}
