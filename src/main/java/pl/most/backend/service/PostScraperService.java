package pl.most.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import pl.most.backend.model.dto.PostDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class PostScraperService {

    private static final String SOURCE_URL = "https://most.salezjanie.pl/";
    private static final int TIMEOUT_MS = 10000;
    private static final String CACHE_KEY = "latest_posts";

    // Caffeine cache with 15-minute TTL
    private final Cache<String, List<PostDTO>> cache;

    public PostScraperService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(15))
                .maximumSize(10)
                .build();
    }

    /**
     * Get latest posts (with caching)
     */
    public List<PostDTO> scrapeLatestPosts() {
        List<PostDTO> cachedPosts = cache.getIfPresent(CACHE_KEY);

        if (cachedPosts != null) {
            log.info("Returning cached posts (count: {})", cachedPosts.size());
            return cachedPosts;
        }

        return forceRefreshPosts();
    }

    /**
     * Force refresh - bypass cache
     */
    public List<PostDTO> forceRefreshPosts() {
        log.info("Scraping posts from: {}", SOURCE_URL);

        try {
            List<PostDTO> posts = performScraping();
            cache.put(CACHE_KEY, posts);
            log.info("Successfully scraped {} posts", posts.size());
            return posts;
        } catch (Exception e) {
            log.error("Error scraping posts from {}", SOURCE_URL, e);

            // Return cached data if available, even if expired
            List<PostDTO> cachedPosts = cache.getIfPresent(CACHE_KEY);
            if (cachedPosts != null) {
                log.warn("Returning stale cached data due to scraping error");
                return cachedPosts;
            }

            return new ArrayList<>();
        }
    }

    /**
     * Perform actual web scraping
     * Based on working Flutter implementation - uses exact same selectors
     */
    private List<PostDTO> performScraping() throws Exception {
        Document doc = Jsoup.connect(SOURCE_URL)
                .timeout(TIMEOUT_MS)
                .userAgent("Mozilla/5.0 (compatible; MOSTBot/1.0)")
                .get();

        List<PostDTO> posts = new ArrayList<>();

        // Select all Facebook post containers (same as Flutter: .fts-jal-single-fb-post)
        Elements postElements = doc.select(".fts-jal-single-fb-post");

        log.info("Found {} post elements with selector .fts-jal-single-fb-post", postElements.size());

        for (Element postElement : postElements) {
            try {
                PostDTO post = extractPostData(postElement);
                if (post != null && (!post.getExcerpt().isEmpty() || post.getImageUrl() != null)) {
                    posts.add(post);
                }
            } catch (Exception e) {
                log.warn("Error extracting post data from element", e);
            }

            // Limit to 10 posts
            if (posts.size() >= 10) {
                break;
            }
        }

        return posts;
    }

    /**
     * Extract post data from HTML element
     * Mirrors the Flutter scraper logic exactly
     */
    private PostDTO extractPostData(Element postElement) {
        PostDTO post = new PostDTO();

        // --- EXTRACT DATE ---
        // Flutter: el.querySelector('.fts-jal-fb-post-time')?.text.trim() ?? ""
        Element dateElement = postElement.selectFirst(".fts-jal-fb-post-time");
        String dateText = dateElement != null ? dateElement.text().trim() : "";
        post.setPublishedDate(parseDate(dateText));

        // --- EXTRACT CONTENT ---
        // Flutter: el.querySelector('.fts-jal-fb-message')?.text ?? ""
        Element messageElement = postElement.selectFirst(".fts-jal-fb-message");
        String content = messageElement != null ? messageElement.text().trim() : "";

        post.setExcerpt(content);
        post.setFullContent(messageElement != null ? messageElement.html() : "");

        // Set title to empty or first line of content
        if (!content.isEmpty()) {
            String[] lines = content.split("\n");
            post.setTitle(lines[0].length() > 50 ? lines[0].substring(0, 47) + "..." : lines[0]);
        } else {
            post.setTitle("");
        }

        // --- EXTRACT IMAGE ---
        // Flutter: Try .fts-jal-fb-picture img first, then .fts-jal-fb-link-wrap img
        String imageUrl = "";
        Element imgElement = postElement.selectFirst(".fts-jal-fb-picture img");

        if (imgElement == null) {
            imgElement = postElement.selectFirst(".fts-jal-fb-link-wrap img");
        }

        if (imgElement != null) {
            imageUrl = imgElement.attr("src");
            if (imageUrl.isEmpty()) {
                imageUrl = imgElement.attr("data-src"); // Lazy loading fallback
            }
        }
        post.setImageUrl(imageUrl);

        // --- EXTRACT LINK ---
        // Flutter: el.querySelector('.fts-jal-fb-see-more')?.attributes['href']
        String link = "https://www.facebook.com/sdamost/"; // Default fallback
        Element linkElement = postElement.selectFirst(".fts-jal-fb-see-more");
        if (linkElement != null) {
            String href = linkElement.attr("href");
            if (!href.isEmpty()) {
                link = href;
            }
        }
        post.setLink(link);

        // Set source and timestamp
        post.setSource("Salezjańskie Duszpasterstwo Akademickie MOST");
        post.setScrapedAt(LocalDateTime.now());

        return post;
    }

    /**
     * Parse date string to LocalDateTime
     * Facebook posts often use relative dates like "2 godz. temu", "wczoraj", etc.
     */
    private LocalDateTime parseDate(String dateText) {
        if (dateText == null || dateText.isEmpty()) {
            return LocalDateTime.now();
        }

        dateText = dateText.toLowerCase().trim();

        // Handle relative dates (Polish)
        if (dateText.contains("godz") || dateText.contains("godzin")) {
            // "2 godz. temu" -> 2 hours ago
            try {
                String[] parts = dateText.split(" ");
                int hours = Integer.parseInt(parts[0]);
                return LocalDateTime.now().minusHours(hours);
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }

        if (dateText.contains("min")) {
            // "30 min temu" -> 30 minutes ago
            try {
                String[] parts = dateText.split(" ");
                int minutes = Integer.parseInt(parts[0]);
                return LocalDateTime.now().minusMinutes(minutes);
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }

        if (dateText.contains("wczoraj")) {
            return LocalDateTime.now().minusDays(1);
        }

        if (dateText.contains("dni") || dateText.contains("dzień")) {
            // "3 dni temu" -> 3 days ago
            try {
                String[] parts = dateText.split(" ");
                int days = Integer.parseInt(parts[0]);
                return LocalDateTime.now().minusDays(days);
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }

        // Try common date formats
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("pl")),
                DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("pl")),
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("pl"))
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(dateText, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        log.debug("Could not parse date: {}, using current time", dateText);
        return LocalDateTime.now();
    }
}