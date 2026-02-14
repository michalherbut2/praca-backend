package pl.most.backend.features.games.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.features.games.dto.CoinFlipRequest;
import pl.most.backend.features.games.dto.WheelSpinResponse;
import pl.most.backend.features.games.service.ArcadeService;
import pl.most.backend.features.games.service.CoinFlipResult;
import pl.most.backend.model.entity.User;
import pl.most.backend.security.AppUserDetails;
import pl.most.backend.security.UserPrincipal;

import java.util.UUID;

@RestController
@RequestMapping("/api/games/arcade")
@RequiredArgsConstructor
public class ArcadeController {

    private final ArcadeService arcadeService;

    /**
     * Spin the Wheel of Fortune
     * POST /api/games/arcade/wheel/spin
     */
    @PostMapping("/wheel/spin")
    public ResponseEntity<WheelSpinResponse> spinWheel(
            @AuthenticationPrincipal AppUserDetails user) {

        WheelSpinResponse response = arcadeService.spinWheel(user.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Check Wheel of Fortune status
     * GET /api/games/arcade/wheel/status
     */
    @GetMapping("/wheel/status")
    public ResponseEntity<WheelSpinResponse> checkWheelStatus(
            @AuthenticationPrincipal AppUserDetails user) {

        WheelSpinResponse response = arcadeService.checkWheelStatus(user.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Play Coin Flip
     * POST /api/games/arcade/coinflip
     */
    @PostMapping("/coinflip")
    public ResponseEntity<CoinFlipResult> playCoinFlip(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody CoinFlipRequest request) {

        CoinFlipResult result = arcadeService.flipCoin(user.getUser().getId(), request);
        return ResponseEntity.ok(result);
    }
}