package pl.most.backend.features.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetEntryResponse {

    private UUID id;
    private UUID userId;
    private UUID betId;
    private Long amount;
    private String selectedOption;
    private LocalDateTime placedAt;
    private Long winnings;
    private Boolean settled;
}