package pl.most.backend.features.games.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import pl.most.backend.model.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wheel_spins",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "spin_date"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WheelSpin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "spin_date", nullable = false)
    private LocalDate spinDate;

    @Column(nullable = false)
    private Integer prizeAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime spunAt;

    @PrePersist
    protected void onCreate() {
        spunAt = LocalDateTime.now();
//        spinDate = LocalDate.now();
    }
}
