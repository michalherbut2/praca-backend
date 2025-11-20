package pl.most.backend.repository;

import pl.most.backend.model.entity.FridgeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FridgeItemRepository extends JpaRepository<FridgeItem, String> {
    List<FridgeItem> findByCategoryOrderByCreatedAtDesc(FridgeItem.Category category);
    List<FridgeItem> findByExpiryDateBeforeOrderByExpiryDateAsc(LocalDate date);
    List<FridgeItem> findByAddedByOrderByCreatedAtDesc(UUID userId);
}