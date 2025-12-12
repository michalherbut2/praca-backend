// PointsController.java
package pl.most.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.model.dto.AwardPointsDto;
import pl.most.backend.service.PointsService;
import pl.most.backend.model.entity.PointsTransaction;
import pl.most.backend.model.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @GetMapping("/me")
    public ResponseEntity<List<PointsTransaction>> getMyPoints(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PointsTransaction> transactions = pointsService.getUserTransactions(userDetails.getUsername());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<User>> getLeaderboard() {
        List<User> leaderboard = pointsService.getLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }

    @PostMapping("/award")
    public ResponseEntity<Void> awardPoints(
            @RequestBody AwardPointsDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        pointsService.awardPoints(dto, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}