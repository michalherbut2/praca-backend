package pl.most.backend.features.duties.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.features.duties.dto.DutySlotResponse;
import pl.most.backend.features.duties.dto.SignUpRequest;
import pl.most.backend.features.duties.model.DutyCategory;
import pl.most.backend.features.duties.service.DutyService;
import pl.most.backend.model.entity.User;
import pl.most.backend.security.AppUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/duties")
@RequiredArgsConstructor
public class DutyController {

    private final DutyService dutyService;

    // ─── USER ENDPOINTS ──────────────────────────────────────────────────────

    // 1. Pobierz sloty (filtrowane po kategorii i zakresie dat)
    @GetMapping("/slots")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DutySlotResponse>> getSlots(
            @RequestParam DutyCategory category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        boolean isAdmin = userDetails.getUser().getRole() == User.Role.ADMIN;
        List<DutySlotResponse> slots = dutyService.getSlots(
                category, dateFrom, dateTo, userDetails.getUser().getId(), isAdmin);
        return ResponseEntity.ok(slots);
    }

    // 2. Zapisz się na slot
    @PostMapping("/slots/{id}/sign-up")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DutySlotResponse> signUp(
            @PathVariable UUID id,
            @RequestBody SignUpRequest request,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        DutySlotResponse response = dutyService.signUp(
                id, userDetails.getUser().getId(), request.isAnonymous());
        return ResponseEntity.ok(response);
    }

    // 3. Wypisz się ze slotu
    @DeleteMapping("/slots/{id}/sign-up")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelSignUp(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        dutyService.cancelSignUp(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    // ─── ADMIN ENDPOINTS ─────────────────────────────────────────────────────

    // 4. Generuj tydzień liturgii
    @PostMapping("/admin/generate/liturgy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DutySlotResponse>> generateLiturgyWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startMonday) {
        return ResponseEntity.ok(dutyService.generateLiturgyWeek(startMonday));
    }

    // 5. Generuj slot kuchenny (niedziela)
    @PostMapping("/admin/generate/kitchen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DutySlotResponse> generateSundayKitchen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sunday) {
        return ResponseEntity.ok(dutyService.generateSundayKitchen(sunday));
    }

    // 6. Potwierdź obecność wolontariusza
    @PatchMapping("/admin/volunteers/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> confirmPresence(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        dutyService.confirmPresence(id, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
}
