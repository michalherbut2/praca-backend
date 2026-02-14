package pl.most.backend.features.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.most.backend.features.games.enums.BetStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetResponse {

    private UUID id;
    private UUID creatorId;
    private String topic;
    private List<String> options;
    private BetStatus status;
    private LocalDateTime bettingDeadline;
    private LocalDateTime resolutionDate;
    private String winningOption;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Long totalPool;
    private Map<String, Long> poolByOption;
    private Map<String, Integer> entriesByOption;
    private Integer totalEntries;
    private BetEntryResponse userEntry; // Current user's entry, if exists
}