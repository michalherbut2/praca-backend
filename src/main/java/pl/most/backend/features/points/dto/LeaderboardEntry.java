package pl.most.backend.features.points.dto;

// Do rankingu (nie zwracamy emaila ani hasła!)
public record LeaderboardEntry(
        String firstName,
        String lastName,
        String avatarUrl, // Jeśli masz
        Integer points
) {}
