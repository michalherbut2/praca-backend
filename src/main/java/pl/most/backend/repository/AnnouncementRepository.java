package pl.most.backend.repository;

import pl.most.backend.model.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, String> {
    List<Announcement> findByPinnedTrueOrderByCreatedAtDesc();
    List<Announcement> findByVisibleUntilAfterOrVisibleUntilIsNullOrderByCreatedAtDesc(LocalDateTime now);
    List<Announcement> findByCategoryOrderByCreatedAtDesc(Announcement.Category category);
}
