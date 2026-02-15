package pl.most.backend.features.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PointsTransactionDto {
    private UUID id;
    private Integer amount;
    private String type;
    private String description;
    private LocalDateTime createdAt;
}
