package pl.most.backend.features.notifications.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pl.most.backend.model.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient; // Do kogo?

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private boolean isRead = false; // Domyślnie nieprzeczytane

    // Opcjonalne: ID obiektu, którego dotyczy (np. ID intencji), żeby po kliknięciu przenieść usera
    private UUID relatedEntityId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}