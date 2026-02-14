package pl.most.backend.features.games.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import pl.most.backend.features.games.enums.BetStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bet {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID creatorId;

    @Column(nullable = false, length = 500)
    private String topic;

    @ElementCollection
    @CollectionTable(name = "bet_options", joinColumns = @JoinColumn(name = "bet_id"))
    @Column(name = "option_value")
    private List<String> options = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BetStatus status = BetStatus.OPEN;

    @Column(nullable = false)
    private LocalDateTime bettingDeadline;

    @Column(nullable = false)
    private LocalDateTime resolutionDate;

    @Column
    private String winningOption;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime resolvedAt;

    @OneToMany(mappedBy = "bet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BetEntry> entries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Helper method to calculate total pool
    public Long getTotalPool() {
        return entries.stream()
                .mapToLong(BetEntry::getAmount)
                .sum();
    }

    // Helper method to get pool for specific option
    public Long getPoolForOption(String option) {
        return entries.stream()
                .filter(entry -> entry.getSelectedOption().equals(option))
                .mapToLong(BetEntry::getAmount)
                .sum();
    }
}