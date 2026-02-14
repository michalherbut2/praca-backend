// pl/most/backend/controller/UserController.java
package pl.most.backend.features.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.features.user.dto.UserSummaryDto;
import pl.most.backend.features.user.service.UserService;
import pl.most.backend.model.dto.UserDto;
import pl.most.backend.model.entity.User;
import pl.most.backend.features.user.repository.UserRepository;
import pl.most.backend.service.AuthService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

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
        // Pobierz wszystkich, ale zmapuj na lekkie DTO
        // W produkcji dodałbyś tu ?search=Jan, ale na start lista wszystkich jest OK
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
    @PreAuthorize("hasRole('ADMIN')") // Tylko admin może to robić
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable UUID id, @RequestParam boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(active);
        userRepository.save(user);

        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

}