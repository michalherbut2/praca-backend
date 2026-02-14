package pl.most.backend.features.games.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveBetRequest {

    @NotNull(message = "Bet ID is required")
    private UUID betId;

    @NotBlank(message = "Winning option is required")
    private String winningOption;
}
