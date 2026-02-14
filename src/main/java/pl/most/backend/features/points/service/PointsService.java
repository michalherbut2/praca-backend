// PointsService.java

package pl.most.backend.features.points.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.most.backend.features.points.dto.AwardPointsDto;
import pl.most.backend.features.points.dto.AwardPointsRequest;
import pl.most.backend.features.points.dto.LeaderboardEntry;
import pl.most.backend.features.points.dto.TransactionHistoryDto;
import pl.most.backend.features.points.model.PointsTransaction;
import pl.most.backend.features.points.model.TransactionType;
import pl.most.backend.model.entity.User;
import pl.most.backend.features.points.repository.PointsTransactionRepository;
import pl.most.backend.features.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TransactionHistoryDto> getMyHistory(UUID userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(t -> new TransactionHistoryDto(
                        t.getId(),
                        t.getAmount(),
                        t.getType().name(),
                        t.getDescription(),
                        t.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard() {
        // Pobieramy Top 20 Userów (zoptymalizowane zapytanie w repo)
        return userRepository.findTop20ByOrderByPointsDesc().stream()
                .map(u -> new LeaderboardEntry(
                        u.getFirstName(),
                        u.getLastName(),
                        u.getProfileImage(),
                        u.getPoints()))
                .toList();
    }

    @Transactional
    public void awardPointsManually(AwardPointsRequest request, UUID adminId) {
        // 1. Walidacja istnienia usera (szybki check)
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje"));

        // 2. Atomowa aktualizacja punktów w Userze
        userRepository.updateUserPoints(request.userId(), request.amount());

        // 3. Zapis historii
        PointsTransaction tx = PointsTransaction.builder()
                .user(user)
                .amount(request.amount())
                .type(TransactionType.MANUAL_AWARD)
                .description(request.reason())
                .createdBy(adminId)
                .build();

        transactionRepository.save(tx);
    }
}