package pl.most.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    // Dodaj te wartości w application.properties
    @Value("${google.calendar.api-key}")
    private String apiKey;

    @Value("${google.calendar.id}")
    private String calendarId; // np. sda.most@gmail.com

    private final RestTemplate restTemplate = new RestTemplate();

    public List<EventDto> getUpcomingEvents() {
        try {
            String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendarId + "/events";

            // Budujemy URL z parametrami
            String requestUrl = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("key", apiKey)
                    .queryParam("timeMin", DateTimeFormatter.ISO_INSTANT.format(Instant.now())) // Tylko przyszłe
                    .queryParam("showDeleted", "false")
                    .queryParam("singleEvents", "true") // Rozwiń wydarzenia cykliczne! Ważne!
                    .queryParam("orderBy", "startTime")
                    .queryParam("maxResults", "10") // Pobierz max 10 najbliższych
                    .toUriString();

            GoogleCalendarResponse response = restTemplate.getForObject(requestUrl, GoogleCalendarResponse.class);

            if (response != null && response.getItems() != null) {
                return response.getItems().stream()
                        .map(this::mapToDto)
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error("Błąd pobierania kalendarza Google: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private EventDto mapToDto(GoogleEvent event) {
        EventDto dto = new EventDto();
        dto.setTitle(event.getSummary());
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());

        // Google zwraca datę w "dateTime" (konkretna godzina) LUB "date" (całodniowe)
        if (event.getStart() != null) {
            if (event.getStart().getDateTime() != null) {
                dto.setStart(event.getStart().getDateTime());
                dto.setAllDay(false);
            } else {
                dto.setStart(event.getStart().getDate());
                dto.setAllDay(true);
            }
        }
        return dto;
    }

    // --- KLASY POMOCNICZE DO MAPOWANIA JSONA Z GOOGLE ---

    @Data
    public static class EventDto {
        private String title;
        private String description;
        private String location;
        private String start; // ISO String
        private boolean allDay;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleCalendarResponse {
        private List<GoogleEvent> items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleEvent {
        private String summary;
        private String description;
        private String location;
        private EventDate start;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class EventDate {
            private String dateTime; // Np. "2023-11-05T19:00:00+01:00"
            private String date;     // Np. "2023-11-05" (dla całodniowych)
        }
    }


    public List<EventDto> getTodayEvents() {
        List<EventDto> upcomingEvents = getUpcomingEvents(); // pobieramy wszystkie nadchodzące
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        return upcomingEvents.stream()
                .filter(event -> {
                    try {
                        if (event.getStart() == null) return false;

                        // Jeśli całodniowe wydarzenie (YYYY-MM-DD)
                        if (event.isAllDay()) {
                            LocalDate eventDate = LocalDate.parse(event.getStart());
                            return eventDate.equals(today);
                        }

                        // Jeśli wydarzenie z godziną (ISO 8601)
                        ZonedDateTime eventDateTime = ZonedDateTime.parse(event.getStart());
                        LocalDate eventDate = eventDateTime.toLocalDate();
                        return eventDate.equals(today);

                    } catch (Exception e) {
                        log.warn("Nie można sparsować daty wydarzenia: " + event.getStart());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
}