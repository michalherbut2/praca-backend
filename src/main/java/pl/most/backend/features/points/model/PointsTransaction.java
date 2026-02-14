package pl.most.backend.features.points.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pl.most.backend.model.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "points_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String description;

    // ID źródła (np. ID zakładu, ID zadania). Null dla MANUAL_AWARD.
    @Column(name = "source_id")
    private UUID sourceId;

    // Kto wywołał akcję (Admin/System). Null jeśli automat.
    @Column(name = "created_by")
    private UUID createdBy;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}