package pl.most.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.most.backend.model.dto.AnnouncementDto;
import pl.most.backend.model.entity.Announcement;
import pl.most.backend.model.entity.User;
import pl.most.backend.repository.AnnouncementRepository;
import pl.most.backend.features.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findByVisibleUntilAfterOrVisibleUntilIsNullOrderByCreatedAtDesc(
                LocalDateTime.now()
        );
    }

    public Announcement getAnnouncementById(String id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ogłoszenie nie znalezione"));
    }

    @Transactional
    public Announcement createAnnouncement(AnnouncementDto dto, UUID authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie znaleziony"));

        // Check permissions
        if (author.getRole() != User.Role.ADMIN && author.getRole() != User.Role.LEADER) {
            throw new SecurityException("Brak uprawnień do tworzenia ogłoszeń");
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setCategory(dto.getCategory() != null ? dto.getCategory() : Announcement.Category.INFO);
        announcement.setPriority(dto.getPriority() != null ? dto.getPriority() : Announcement.Priority.NORMAL);
        announcement.setAuthorId(authorId);
        announcement.setAuthorName(author.getFirstName() + " " + author.getLastName());
        announcement.setImageUrl(dto.getImageUrl());
        announcement.setPinned(dto.getPinned() != null ? dto.getPinned() : false);

        return announcementRepository.save(announcement);
    }

    @Transactional
    public void deleteAnnouncement(String id, UUID userId) {
        Announcement announcement = getAnnouncementById(id);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie znaleziony"));

        // Check permissions
        if (user.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Brak uprawnień do usunięcia ogłoszenia");
        }

        announcementRepository.delete(announcement);
    }
}