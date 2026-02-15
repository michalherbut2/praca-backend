package pl.most.backend.features.duties.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pl.most.backend.model.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "duty_volunteers", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "slot_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DutyVolunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private DutySlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DutyVolunteerStatus status;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "was_present", nullable = false)
    @Builder.Default
    private boolean wasPresent = false;

    @Column(name = "points_awarded", nullable = false)
    @Builder.Default
    private boolean pointsAwarded = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
