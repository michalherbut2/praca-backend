package pl.most.backend.features.notifications.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.most.backend.features.notifications.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Pobierz wszystkie dla usera, najnowsze na górze
    List<Notification> findAllByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    // Policz ile nieprzeczytanych (do czerwonej kropki na dzwoneczku)
    long countByRecipientIdAndIsReadFalse(UUID recipientId);
}