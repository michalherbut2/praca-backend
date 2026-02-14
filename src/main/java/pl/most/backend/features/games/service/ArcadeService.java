package pl.most.backend.features.games.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.most.backend.features.games.dto.CoinFlipRequest;
import pl.most.backend.features.games.dto.WheelSpinResponse;
import pl.most.backend.features.games.model.WheelSpin;
import pl.most.backend.features.games.repository.WheelSpinRepository;
import pl.most.backend.features.points.dto.AwardPointsRequest;
import pl.most.backend.features.points.service.PointsService;
import pl.most.backend.features.user.repository.UserRepository;
import pl.most.backend.model.entity.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArcadeService {

    private final WheelSpinRepository wheelSpinRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();
    private final PointsService pointsService;

    private static final ZoneId ZONE_PL = ZoneId.of("Europe/Warsaw");

    /**
     * Wheel of Fortune - Daily Spin
     * Prizes are weighted:
     * - 60% chance: 10 points
     * - 25% chance: 50 points
     * - 10% chance: 100 points
     * - 4% chance: 250 points
     * - 1% chance: 500 points
     */
    @Transactional
    public WheelSpinResponse spinWheel(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now(ZONE_PL);

        // Check if user already spun today
        Optional<WheelSpinResponse> cooldown = getCooldownResponse(userId, today);
        if (cooldown.isPresent()) {
            return cooldown.get();
        }

        // Calculate prize based on weighted random
        Integer prize = calculateWheelPrize();

        // Save spin record
        WheelSpin spin = WheelSpin.builder()
                .user(user)
                .spinDate(today)
                .prizeAmount(prize)
                .build();

        wheelSpinRepository.save(spin);

        // Credit prize to wallet
        AwardPointsRequest awardRequest = new AwardPointsRequest(
                userId,
                prize,
                "Koło Fortuny - nagroda"
        );
        pointsService.awardPointsManually(awardRequest, null);


        LocalDate tomorrow = today.plusDays(1);
        return WheelSpinResponse.builder()
                .prizeAmount(prize)
                .canSpinAgain(false)
                .nextSpinAvailable(tomorrow.toString())
                .build();
    }

    private Integer calculateWheelPrize() {
        int roll = random.nextInt(100); // 0-99

        if (roll < 60) return 10;      // 60% chance
        if (roll < 85) return 50;      // 25% chance
        if (roll < 95) return 10;     // 10% chance
        if (roll < 99) return 250;     // 4% chance
        return 500;                     // 1% chance
    }

    public WheelSpinResponse checkWheelStatus(UUID userId) {
        LocalDate today = LocalDate.now(ZONE_PL);

        // 1. Próbujemy pobrać response blokady
        return getCooldownResponse(userId, today)
                // 2. Jeśli blokady nie ma (Optional empty), zwracamy status "Gotowy do gry"
                .orElse(WheelSpinResponse.builder()
                        .prizeAmount(0)
                        .canSpinAgain(true)
                        .nextSpinAvailable("now") // Lub null, zależy jak frontend woli
                        .build());
    }

    /**
     * Coin Flip - 50/50 game
     * User bets X points
     * - Win: Get 2X (profit of X)
     * - Lose: Lose X
     */
    @Transactional
    public CoinFlipResult flipCoin(UUID userId, CoinFlipRequest request) {
        Integer amount = request.getAmount();
        // 50/50 coin flip
        boolean won = random.nextBoolean();
        String outcome = won ? "orzeł" : "reszka";
        Integer result = won ? amount : -amount;
        String reason = won ? "Wygrana w rzucie monetą" : "Przegrana w rzucie monetą";

        AwardPointsRequest awardRequest = new AwardPointsRequest(
                userId,
                result,
                reason
        );
        pointsService.awardPointsManually(awardRequest, null);

        return CoinFlipResult.builder()
                .won(won)
                .amountBet(amount)
                .result(result)
                .outcome(outcome)
                .build();
    }

    private Optional<WheelSpinResponse> getCooldownResponse(UUID userId, LocalDate today) {
        if (wheelSpinRepository.existsByUserIdAndSpinDate(userId, today)) {
            LocalDate tomorrow = today.plusDays(1);
            return Optional.of(WheelSpinResponse.builder()
                    .prizeAmount(0) // Tutaj wstawiasz 0, bo to tylko sprawdzenie statusu
                    .canSpinAgain(false)
                    .nextSpinAvailable(tomorrow.toString())
                    .build());
        }
        return Optional.empty();
    }
}

