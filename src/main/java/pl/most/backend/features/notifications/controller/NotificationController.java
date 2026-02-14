package pl.most.backend.features.notifications.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.features.notifications.dto.NotificationResponse;
import pl.most.backend.features.notifications.service.NotificationService;
import pl.most.backend.security.AppUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 1. Pobierz listę
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getUser().getId()));
    }

    // 2. Pobierz liczbę nieprzeczytanych (do badge'a)
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(notificationService.getUnreadCount(user.getUser().getId()));
    }

    // 3. Oznacz konkretne jako przeczytane (po kliknięciu)
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal AppUserDetails user,
            @PathVariable UUID id) {
        notificationService.markAsRead(id, user.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // 4. Oznacz wszystkie jako przeczytane ("Mark all as read")
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal AppUserDetails user) {
        notificationService.markAllAsRead(user.getUser().getId());
        return ResponseEntity.ok().build();
    }
}