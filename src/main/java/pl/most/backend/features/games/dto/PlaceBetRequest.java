package pl.most.backend.features.games.dto;

import jakarta.validation.constraints.Min;
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
public class PlaceBetRequest {

    @NotNull(message = "Bet ID is required")
    private UUID betId;

    @NotBlank(message = "Selected option is required")
    private String selectedOption;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1 point")
    private Long amount;
}