package pl.most.backend.features.intentions.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pl.most.backend.model.entity.User;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "intentions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Intention {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author; // Żeby wysłać powiadomienie (nawet jak anonimowa, system wie kto to)

    @Column(nullable = false, length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntentionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntentionStatus status;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private boolean isAnonymous; // Czy czytać nazwisko? (Zazwyczaj w modlitwie wiernych się nie czyta, ale warto mieć flagę)

    private String adminResponse;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate createdAt;
}