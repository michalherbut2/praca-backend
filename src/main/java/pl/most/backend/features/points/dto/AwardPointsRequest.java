package pl.most.backend.features.points.dto;

import java.util.UUID;

// Do przyznawania punktów
public record AwardPointsRequest(
        UUID userId,
        Integer amount,
        String reason
) {}
