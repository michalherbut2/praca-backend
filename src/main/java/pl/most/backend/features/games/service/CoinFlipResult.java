package pl.most.backend.features.games.service;

// Inner class for coin flip result
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class CoinFlipResult {
    private Boolean won;
    private Integer amountBet;
    private Integer result;
    private String outcome;
}
