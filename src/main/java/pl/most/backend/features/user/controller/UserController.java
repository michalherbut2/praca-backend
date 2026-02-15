// pl/most/backend/controller/UserController.java
package pl.most.backend.features.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.features.points.repository.PointsTransactionRepository;
import pl.most.backend.features.user.dto.*;
import pl.most.backend.features.user.service.UserService;
import pl.most.backend.model.dto.UserDto;
import pl.most.backend.model.entity.User;
import pl.most.backend.features.user.repository.UserRepository;
import pl.most.backend.service.AuthService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PointsTransactionRepository pointsTransactionRepository;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/lite")
    public ResponseEntity<List<UserSummaryDto>> getUsersLite() {
        return ResponseEntity.ok(userService.getAllUsersLite());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        if (admin.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Tylko admin może zmieniać role");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newRole = body.get("role");
        user.setRole(User.Role.valueOf(newRole));

        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        if (admin.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Tylko admin może usuwać konta");
        }

        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable UUID id, @RequestParam boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(active);
        userRepository.save(user);

        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    // ─── ADMIN: Paginated user list with search ──────────────────────────────

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAdminUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> usersPage;
        if (search != null && !search.isBlank()) {
            usersPage = userRepository.searchUsers(search.trim(), pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        Page<UserDto> dtoPage = usersPage.map(UserDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    // ─── ADMIN: Update user role & points ─────────────────────────────────────

    @PutMapping("/{id}/admin-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> adminUpdateUser(
            @PathVariable UUID id,
            @RequestBody AdminUpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(request.getRole());
        user.setPoints(request.getPoints());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(UserDto.fromEntity(saved));
    }

    // ─── PROFILE: Points transaction history ─────────────────────────────────

    @GetMapping("/me/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PointsTransactionDto>> getMyHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PointsTransactionDto> history = pointsTransactionRepository
                .findTop20ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(tx -> PointsTransactionDto.builder()
                        .id(tx.getId())
                        .amount(tx.getAmount())
                        .type(tx.getType().name())
                        .description(tx.getDescription())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(history);
    }

    // ─── PROFILE: Leaderboard (TOP 20) ───────────────────────────────────────

    @GetMapping("/leaderboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard() {
        List<User> topUsers = userRepository.findTop20ByOrderByPointsDesc();

        AtomicInteger rank = new AtomicInteger(1);
        List<LeaderboardEntryDto> entries = topUsers.stream()
                .map(u -> LeaderboardEntryDto.builder()
                        .userId(u.getId())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .profileImage(u.getProfileImage())
                        .points(u.getPoints())
                        .rank(rank.getAndIncrement())
                        .build())
                .toList();

        return ResponseEntity.ok(entries);
    }

    // ─── PROFILE: Update own profile ─────────────────────────────────────────

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(UserDto.fromEntity(saved));
    }

}