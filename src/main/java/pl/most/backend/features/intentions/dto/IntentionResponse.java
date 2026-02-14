package pl.most.backend.features.intentions.dto;

import lombok.Builder;
import lombok.Data;
import pl.most.backend.features.intentions.model.IntentionStatus;
import pl.most.backend.features.intentions.model.IntentionType;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class IntentionResponse {
    private UUID id;
    private String content;
    private IntentionType type;
    private IntentionStatus status;
    private LocalDate targetDate;
    private boolean isAnonymous;
    private String adminResponse;
    private String authorName; // Np. "Jan K." lub "Anonim"
    private LocalDate createdAt;
}