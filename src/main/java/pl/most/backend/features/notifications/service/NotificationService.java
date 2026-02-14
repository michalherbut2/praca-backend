package pl.most.backend.features.notifications.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.most.backend.features.notifications.dto.NotificationResponse;
import pl.most.backend.features.notifications.model.Notification;
import pl.most.backend.features.notifications.model.NotificationType;
import pl.most.backend.features.notifications.repository.NotificationRepository;
import pl.most.backend.model.entity.User;
import pl.most.backend.features.user.repository.UserRepository; // Import twojego repo użytkowników

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // --- Metody publiczne dla API ---

    public List<NotificationResponse> getUserNotifications(UUID userId) {
        return notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Zabezpieczenie: Czy user na pewno jest właścicielem tego powiadomienia?
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new RuntimeException("Nie masz dostępu do tego powiadomienia");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread = notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // --- Metoda wewnętrzna do wysyłania (używana przez inne serwisy) ---

    @Transactional
    public void send(User recipient, String title, String message, NotificationType type, UUID relatedEntityId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    // Przeciążenie dla prostszego użycia (bez relatedId)
    public void send(User recipient, String title, String message, NotificationType type) {
        send(recipient, title, message, type, null);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.isRead())
                .relatedEntityId(n.getRelatedEntityId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}