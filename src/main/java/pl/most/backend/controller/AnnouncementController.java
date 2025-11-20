package pl.most.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.model.dto.AnnouncementDto;
import pl.most.backend.model.entity.Announcement;
import pl.most.backend.model.entity.User;
import pl.most.backend.repository.AnnouncementRepository;
import pl.most.backend.repository.UserRepository;
import pl.most.backend.service.AnnouncementService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;

    @GetMapping
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        return ResponseEntity.ok(announcementService.getAllAnnouncements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Announcement> getAnnouncement(@PathVariable String id) {
        return ResponseEntity.ok(announcementService.getAnnouncementById(id));
    }

    @PostMapping
    public ResponseEntity<Announcement> createAnnouncement(
            @Valid @RequestBody AnnouncementDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();

        Announcement announcement = announcementService.createAnnouncement(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(announcement);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();

        announcementService.deleteAnnouncement(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable String id,
            @Valid @RequestBody AnnouncementDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found"));

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        // Check permissions
        if (!announcement.getAuthorId().equals(user.getId()) &&
                user.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Brak uprawnień");
        }

        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setCategory(dto.getCategory());
        announcement.setPriority(dto.getPriority());
        announcement.setPinned(dto.getPinned() != null ? dto.getPinned() : false);

        return ResponseEntity.ok(announcementRepository.save(announcement));
    }
}