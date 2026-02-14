package pl.most.backend.features.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WheelSpinResponse {

    private Integer prizeAmount;
    private Boolean canSpinAgain;
    private String nextSpinAvailable;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CoinFlipResponse {

    private Boolean won;
    private Integer amountBet;
    private Integer result; // Net result: positive if won, negative if lost
    private String outcome; // "HEADS" or "TAILS"
}