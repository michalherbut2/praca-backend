package pl.most.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.most.backend.model.dto.FridgeItemDto;
import pl.most.backend.model.entity.FridgeItem;
import pl.most.backend.features.user.repository.UserRepository;
import pl.most.backend.service.FridgeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fridge")
@RequiredArgsConstructor
public class FridgeController {

    private final FridgeService fridgeService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<FridgeItem>> getAllItems() {
        return ResponseEntity.ok(fridgeService.getAllItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FridgeItem> getItem(@PathVariable String id) {
        return ResponseEntity.ok(fridgeService.getItemById(id));
    }

    @PostMapping
    public ResponseEntity<FridgeItem> addItem(
            @Valid @RequestBody FridgeItemDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();

        FridgeItem item = fridgeService.addItem(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable String id) {


        fridgeService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}