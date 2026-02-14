package pl.most.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.model.dto.PostDTO;
import pl.most.backend.service.PostScraperService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostScraperService scraperService;

    public PostController(PostScraperService scraperService) {
        this.scraperService = scraperService;
    }

    /**
     * Public endpoint - Get latest posts from external source
     * Uses caching (15 min TTL) to avoid excessive scraping
     */
    @GetMapping("/latest")
    public ResponseEntity<List<PostDTO>> getLatestPosts() {
        List<PostDTO> posts = scraperService.scrapeLatestPosts();
        return ResponseEntity.ok(posts);
    }

    /**
     * Admin endpoint - Force refresh cache
     */
    @PostMapping("/refresh")
    public ResponseEntity<List<PostDTO>> refreshPosts() {
        List<PostDTO> posts = scraperService.forceRefreshPosts();
        return ResponseEntity.ok(posts);
    }
}
