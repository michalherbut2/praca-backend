package pl.most.backend.features.scheduler.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.features.scheduler.dto.ServiceSlotResponse;
import pl.most.backend.features.scheduler.dto.SignUpRequest;
import pl.most.backend.features.scheduler.model.ServiceCategory;
import pl.most.backend.features.scheduler.service.ServiceSchedulerService;
import pl.most.backend.model.entity.User;
import pl.most.backend.security.AppUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class ServiceSchedulerController {

    private final ServiceSchedulerService schedulerService;

    // ─── USER ENDPOINTS ──────────────────────────────────────────────────────

    // 1. Pobierz sloty (filtrowane po kategorii i zakresie dat)
    @GetMapping("/slots")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServiceSlotResponse>> getSlots(
            @RequestParam ServiceCategory category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        boolean isAdmin = userDetails.getUser().getRole() == User.Role.ADMIN;
        List<ServiceSlotResponse> slots = schedulerService.getSlots(
                category, dateFrom, dateTo, userDetails.getUser().getId(), isAdmin);
        return ResponseEntity.ok(slots);
    }

    // 2. Zapisz się na slot
    @PostMapping("/slots/{id}/sign-up")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceSlotResponse> signUp(
            @PathVariable UUID id,
            @RequestBody SignUpRequest request,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        ServiceSlotResponse response = schedulerService.signUp(
                id, userDetails.getUser().getId(), request.isAnonymous());
        return ResponseEntity.ok(response);
    }

    // 3. Wypisz się ze slotu
    @DeleteMapping("/slots/{id}/sign-up")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelSignUp(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        schedulerService.cancelSignUp(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    // ─── ADMIN ENDPOINTS ─────────────────────────────────────────────────────

    // 4. Generuj tydzień liturgii
    @PostMapping("/admin/generate/liturgy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServiceSlotResponse>> generateLiturgyWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startMonday) {
        return ResponseEntity.ok(schedulerService.generateLiturgyWeek(startMonday));
    }

    // 5. Generuj slot kuchenny (niedziela)
    @PostMapping("/admin/generate/kitchen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceSlotResponse> generateSundayKitchen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sunday) {
        return ResponseEntity.ok(schedulerService.generateSundayKitchen(sunday));
    }

    // 6. Potwierdź obecność wolontariusza
    @PatchMapping("/admin/volunteers/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> confirmPresence(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        schedulerService.confirmPresence(id, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
}
