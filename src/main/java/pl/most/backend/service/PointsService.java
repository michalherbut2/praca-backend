// ============================================
// 2. PointsService.java
package pl.most.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.most.backend.model.dto.AwardPointsDto;
import pl.most.backend.model.entity.PointsTransaction;
import pl.most.backend.model.entity.User;
import pl.most.backend.repository.PointsTransactionRepository;
import pl.most.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<PointsTransaction> getUserTransactions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return transactionRepository.findByUserIdOrderByAwardedAtDesc(user.getId());
    }

    public List<User> getLeaderboard() {
        return userRepository.findTop20ByRoleInOrderByPointsDesc(
                List.of(User.Role.MOSTOWIAK, User.Role.PODPRZESLOWY, User.Role.PRZESLOWY)
        );
    }

    @Transactional
    public void awardPoints(AwardPointsDto dto, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // Check permissions
        if (admin.getRole() != User.Role.ADMIN && admin.getRole() != User.Role.PRZESLOWY) {
            throw new SecurityException("Brak uprawnień");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update user points
        user.setPoints(user.getPoints() + dto.getAmount());
        userRepository.save(user);

        // Create transaction
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUserId(user.getId());
        transaction.setAmount(dto.getAmount());
        transaction.setReason(dto.getReason());
        transaction.setAwardedBy(admin.getId());
        transaction.setAwardedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
    }
}