package pl.most.backend.features.intentions.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.features.intentions.dto.CreateIntentionRequest;
import pl.most.backend.features.intentions.dto.IntentionResponse;
import pl.most.backend.features.intentions.dto.ReviewIntentionRequest;
import pl.most.backend.features.intentions.service.IntentionService;
import pl.most.backend.security.AppUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/intentions")
@RequiredArgsConstructor
public class IntentionController {

    private final IntentionService intentionService;

    // 1. User: Dodaj intencję
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IntentionResponse> createIntention(
            @AuthenticationPrincipal AppUserDetails user,
            @RequestBody @Valid CreateIntentionRequest request) {
        return ResponseEntity.ok(intentionService.createIntention(user.getUser().getId(), request));
    }

    // 2. User: Moje intencje
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IntentionResponse>> getMyIntentions(
            @AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(intentionService.getMyIntentions(user.getUser().getId()));
    }

    // --- ADMIN / PODPRZĘSŁOWY ---

    // 3. Admin: Pobierz wszystkie oczekujące
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')") // lub 'PRIEST', zależy od ról
    public ResponseEntity<List<IntentionResponse>> getPendingIntentions() {
        return ResponseEntity.ok(intentionService.getPendingIntentions());
    }

    // 4. Admin: Pobierz intencje na konkretny dzień (np. lista do czytania)
    @GetMapping("/admin/by-date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IntentionResponse>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(intentionService.getApprovedForDate(date));
    }

    // 5. Admin: Zatwierdź / Odrzuć
    @PutMapping("/admin/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IntentionResponse> reviewIntention(
            @PathVariable UUID id,
            @RequestBody ReviewIntentionRequest request) {
        return ResponseEntity.ok(intentionService.reviewIntention(id, request));
    }
}