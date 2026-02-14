package pl.most.backend.features.games.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.features.games.dto.*;
import pl.most.backend.features.games.service.BettingService;
import pl.most.backend.security.AppUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/games/bets")
@RequiredArgsConstructor
public class BettingController {

    private final BettingService bettingService;

    /**
     * Create a new bet
     * POST /api/games/bets
     */
    @PostMapping
    public ResponseEntity<BetResponse> createBet(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody CreateBetRequest request) {

        BetResponse response = bettingService.createBet(user.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all active bets
     * GET /api/games/bets/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<BetResponse>> getActiveBets(
            @AuthenticationPrincipal AppUserDetails user) {

        List<BetResponse> bets = bettingService.getActiveBets(user.getUser().getId());
        return ResponseEntity.ok(bets);
    }

    /**
     * Get all settled bets
     * GET /api/games/bets/settled
     */
    @GetMapping("/settled")
    public ResponseEntity<List<BetResponse>> getSettledBets() {

        List<BetResponse> bets = bettingService.getSettledBets();
        return ResponseEntity.ok(bets);
    }

    /**
     * Get specific bet by ID
     * GET /api/games/bets/{betId}
     */
    @GetMapping("/{betId}")
    public ResponseEntity<BetResponse> getBet(
            @AuthenticationPrincipal AppUserDetails user,
            @PathVariable UUID betId) {

        BetResponse bet = bettingService.getBetById(betId, user.getUser().getId());
        return ResponseEntity.ok(bet);
    }

    /**
     * Get user's bets
     * GET /api/games/bets/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<BetResponse>> getMyBets(
            @AuthenticationPrincipal AppUserDetails user) {

        List<BetResponse> bets = bettingService.getUserBets(user.getUser().getId());
        return ResponseEntity.ok(bets);
    }

    /**
     * Place a bet
     * POST /api/games/bets/place
     */
    @PostMapping("/place")
    public ResponseEntity<BetEntryResponse> placeBet(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody PlaceBetRequest request) {

        BetEntryResponse response = bettingService.placeBet(user.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Resolve a bet (Creator or Admin only)
     * POST /api/games/bets/resolve
     */
    @PostMapping("/resolve")
    public ResponseEntity<BetResponse> resolveBet(
            @AuthenticationPrincipal AppUserDetails user,
            @RequestAttribute(value = "isAdmin", required = false) Boolean isAdmin,
            @Valid @RequestBody ResolveBetRequest request) {

        boolean admin = isAdmin != null && isAdmin;
        BetResponse response = bettingService.resolveBet(user.getUser().getId(), request, admin);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a bet (Admin only)
     * DELETE /api/games/bets/{betId}
     */
    @DeleteMapping("/{betId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelBet(
            @AuthenticationPrincipal AppUserDetails user,
            @PathVariable UUID betId) {

        bettingService.cancelBet(betId, user.getUser().getId(), true);
        return ResponseEntity.noContent().build();
    }
}
