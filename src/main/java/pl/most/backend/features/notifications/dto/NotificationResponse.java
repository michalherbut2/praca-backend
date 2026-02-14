package pl.most.backend.features.notifications.dto;

import lombok.Builder;
import lombok.Data;
import pl.most.backend.features.notifications.model.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private UUID relatedEntityId;
    private LocalDateTime createdAt;
}