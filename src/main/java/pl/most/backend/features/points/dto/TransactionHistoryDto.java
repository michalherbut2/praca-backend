package pl.most.backend.features.points.dto;

import java.time.LocalDateTime;
import java.util.UUID;

// Do wyświetlania historii
public record TransactionHistoryDto(
        UUID id,
        Integer amount,
        String type,
        String description,
        LocalDateTime createdAt
) {}
