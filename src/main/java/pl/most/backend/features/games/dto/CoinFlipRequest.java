package pl.most.backend.features.games.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinFlipRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Minimum bet is 1 points")
    private Integer amount;
}
