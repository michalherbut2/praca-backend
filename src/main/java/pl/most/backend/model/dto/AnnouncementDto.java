package pl.most.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pl.most.backend.model.entity.Announcement;

@Data
public class AnnouncementDto {

    @NotBlank(message = "Tytuł jest wymagany")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Treść jest wymagana")
    private String content;

    private Announcement.Category category;
    private Announcement.Priority priority;
    private String imageUrl;
    private Boolean pinned;
}