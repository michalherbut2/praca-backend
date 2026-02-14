package pl.most.backend.features.intentions.dto;

import lombok.Data;

@Data
public class ReviewIntentionRequest {
    private boolean isApproved; // true = APPROVED, false = REJECTED
    private String adminResponse; // Opcjonalna notatka
}