// PointsController.java
package pl.most.backend.features.points.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.features.points.dto.AwardPointsDto;
import pl.most.backend.features.points.dto.AwardPointsRequest;
import pl.most.backend.features.points.dto.LeaderboardEntry;
import pl.most.backend.features.points.dto.TransactionHistoryDto;
import pl.most.backend.features.points.service.PointsService;
import pl.most.backend.features.points.model.PointsTransaction;
import pl.most.backend.model.entity.User;
import pl.most.backend.security.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    // Każdy widzi swoje punkty
    @GetMapping("/history")
    public ResponseEntity<List<TransactionHistoryDto>> getMyHistory(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(pointsService.getMyHistory(user.getId()));
    }

    // Ranking publiczny (dla zalogowanych)
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        return ResponseEntity.ok(pointsService.getLeaderboard());
    }

    // Tylko ADMIN lub LEADER może nagradzać
    @PostMapping("/award")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<Void> awardPoints(
            @RequestBody AwardPointsRequest request,
            @AuthenticationPrincipal UserPrincipal admin) {

        // 2. DODAJ TE LOGI (SOUT):
        System.out.println("--- DEBUGOWANIE UPRAWNIEŃ ---");
        System.out.println("Zalogowany user ID: " + admin.getId());
        System.out.println("Zalogowany admin Email: " + admin.getEmail());

        // To jest kluczowe! Zobacz, co tu wypisze:
        System.out.println("Uprawnienia (Authorities): " + admin.getAuthorities());
        System.out.println("-----------------------------");

        pointsService.awardPointsManually(request, admin.getId());
        return ResponseEntity.ok().build();
    }

}