package pl.most.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for scraped post data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

    private String title;
    private String excerpt;
    private String fullContent;
    private String link;
    private String imageUrl;
    private String source;
    private LocalDateTime publishedDate;
    private LocalDateTime scrapedAt;

    // Additional metadata
    private String author;
    private String category;
    private Integer likes;
    private Integer comments;
}