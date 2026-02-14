package pl.most.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.most.backend.service.GoogleCalendarService;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final GoogleCalendarService googleCalendarService;

    @GetMapping
    public ResponseEntity<List<GoogleCalendarService.EventDto>> getEvents() {
        return ResponseEntity.ok(googleCalendarService.getUpcomingEvents());
    }

    /**
     * Public endpoint - Get today's events
     * Returns all events where startDateTime equals today's date
     */
    @GetMapping("/today")
    public ResponseEntity<List<GoogleCalendarService.EventDto>> getTodayEvents() {
        return ResponseEntity.ok(googleCalendarService.getTodayEvents());
    }
}