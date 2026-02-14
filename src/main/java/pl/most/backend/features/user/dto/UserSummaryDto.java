package pl.most.backend.features.user.dto;

import java.util.UUID;

public record UserSummaryDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Integer points,
        String profileImage
) {}
