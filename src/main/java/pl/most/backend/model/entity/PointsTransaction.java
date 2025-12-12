// PointsTransaction.java (Entity)
package pl.most.backend.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "points_transactions")
@Data
@NoArgsConstructor
public class PointsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "awarded_by", nullable = false)
    private UUID awardedBy;

    @Column(name = "awarded_at", nullable = false)
    private LocalDateTime awardedAt;
}