package pl.most.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.model.dto.EventDto;
import pl.most.backend.model.entity.Event;
import pl.most.backend.model.entity.User;
import pl.most.backend.repository.EventRepository;
import pl.most.backend.repository.EventRsvpRepository;
import pl.most.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.model.entity.EventRsvp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final EventRsvpRepository rsvpRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(
            @Valid @RequestBody EventDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setLocation(dto.getLocation());
        event.setCreatedBy(user.getId());
        event.setMaxParticipants(dto.getMaxParticipants());
        event.setAllowRsvp(dto.getAllowRsvp() != null ? dto.getAllowRsvp() : false);

        return ResponseEntity.status(201).body(eventRepository.save(event));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{id}/rsvp")
    public ResponseEntity<EventRsvp> rsvpToEvent(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String status = body.get("status"); // "ATTENDING", "NOT_ATTENDING", "MAYBE"

        EventRsvp rsvp = rsvpRepository.findByEventIdAndUserId(id, user.getId())
                .orElse(new EventRsvp());

        rsvp.setEventId(id);
        rsvp.setUserId(user.getId());
        rsvp.setStatus(EventRsvp.Status.valueOf(status));

        EventRsvp saved = rsvpRepository.save(rsvp);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}/rsvp/my")
    public ResponseEntity<EventRsvp> getMyRsvp(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EventRsvp rsvp = rsvpRepository.findByEventIdAndUserId(id, user.getId())
                .orElse(null);

        if (rsvp == null) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.ok(rsvp);
    }

    @GetMapping("/{id}/attendees")
    public ResponseEntity<Map<String, Object>> getAttendees(@PathVariable String id) {
        List<EventRsvp> rsvps = rsvpRepository.findByEventId(id);

        int attending = (int) rsvps.stream()
                .filter(r -> r.getStatus() == EventRsvp.Status.ATTENDING)
                .count();
        int notAttending = (int) rsvps.stream()
                .filter(r -> r.getStatus() == EventRsvp.Status.NOT_ATTENDING)
                .count();
        int maybe = (int) rsvps.stream()
                .filter(r -> r.getStatus() == EventRsvp.Status.MAYBE)
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("attending", attending);
        result.put("notAttending", notAttending);
        result.put("maybe", maybe);
        result.put("total", rsvps.size());

        return ResponseEntity.ok(result);
    }

    // 1. EventController.java - DODAJ:
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable String id,
            @Valid @RequestBody EventDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        // Check permissions
        if (!event.getCreatedBy().equals(user.getId()) &&
                user.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Brak uprawnień");
        }

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setLocation(dto.getLocation());
        event.setMaxParticipants(dto.getMaxParticipants());

        return ResponseEntity.ok(eventRepository.save(event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        // Check permissions
        if (!event.getCreatedBy().equals(user.getId()) &&
                user.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Brak uprawnień");
        }

        eventRepository.delete(event);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/rsvps")
    public ResponseEntity<List<Map<String, Object>>> getEventRsvpsDetails(@PathVariable String id) {
        // 1. Pobierz wszystkie zapisy dla wydarzenia
        List<EventRsvp> rsvps = rsvpRepository.findByEventId(id);

        // 2. Zamień każdy zapis na mapę z danymi użytkownika (imie, nazwisko)
        List<Map<String, Object>> response = rsvps.stream().map(rsvp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", rsvp.getId());
            map.put("eventId", rsvp.getEventId());
            map.put("userId", rsvp.getUserId());
            map.put("status", rsvp.getStatus());

            // Szukamy użytkownika po ID, żeby wyciągnąć imię
            User user = userRepository.findById(rsvp.getUserId()).orElse(null);

            String name = (user != null)
                    ? user.getFirstName() + " " + user.getLastName()
                    : "Nieznany";

            map.put("userName", name);
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }
}