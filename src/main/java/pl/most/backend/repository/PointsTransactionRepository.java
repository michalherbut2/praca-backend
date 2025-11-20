// ============================================
// 4. PointsTransactionRepository.java
package pl.most.backend.repository;

import pl.most.backend.model.entity.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, String> {
    List<PointsTransaction> findByUserIdOrderByAwardedAtDesc(UUID userId);
}