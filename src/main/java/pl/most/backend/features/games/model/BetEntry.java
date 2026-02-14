package pl.most.backend.features.games.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bet_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "bet_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetEntry {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bet_id", nullable = false)
    private Bet bet;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String selectedOption;

    @Column(nullable = false, updatable = false)
    private LocalDateTime placedAt;

    @Column
    private Long winnings;

    @Column
    private Boolean settled = false;

    @PrePersist
    protected void onCreate() {
        placedAt = LocalDateTime.now();
    }
}