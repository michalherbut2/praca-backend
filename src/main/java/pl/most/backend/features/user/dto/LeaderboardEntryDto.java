package pl.most.backend.features.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LeaderboardEntryDto {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String profileImage;
    private int points;
    private int rank;
}
