package pl.most.backend.features.games.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.most.backend.features.games.dto.*;
import pl.most.backend.features.games.model.Bet;
import pl.most.backend.features.games.model.BetEntry;
import pl.most.backend.features.games.enums.BetStatus;
import pl.most.backend.features.games.repository.BetEntryRepository;
import pl.most.backend.features.games.repository.BetRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BettingService {

    private final BetRepository betRepository;
    private final BetEntryRepository betEntryRepository;
    // Assume WalletService exists in pl.most.backend.features.wallet
    // private final WalletService walletService;

    @Transactional
    public BetResponse createBet(UUID creatorId, CreateBetRequest request) {
        // Validate resolution date is after betting deadline
        if (request.getResolutionDate().isBefore(request.getBettingDeadline())) {
            throw new IllegalArgumentException("Resolution date must be after betting deadline");
        }

        Bet bet = Bet.builder()
                .creatorId(creatorId)
                .topic(request.getTopic())
                .options(new ArrayList<>(request.getOptions()))
                .status(BetStatus.OPEN)
                .bettingDeadline(request.getBettingDeadline())
                .resolutionDate(request.getResolutionDate())
                .build();

        bet = betRepository.save(bet);
        log.info("Bet created: {} by user {}", bet.getId(), creatorId);

        return mapToBetResponse(bet, null);
    }

    @Transactional
    public BetEntryResponse placeBet(UUID userId, PlaceBetRequest request) {
        Bet bet = betRepository.findById(request.getBetId())
                .orElseThrow(() -> new IllegalArgumentException("Bet not found"));

        // Validation
        if (bet.getStatus() != BetStatus.OPEN) {
            throw new IllegalStateException("Bet is not open for betting");
        }

        if (LocalDateTime.now().isAfter(bet.getBettingDeadline())) {
            throw new IllegalStateException("Betting deadline has passed");
        }

        if (!bet.getOptions().contains(request.getSelectedOption())) {
            throw new IllegalArgumentException("Invalid option selected");
        }

        if (betEntryRepository.existsByUserIdAndBetId(userId, bet.getId())) {
            throw new IllegalStateException("You have already placed a bet on this");
        }

        // Deduct points from wallet
        // walletService.deductPoints(userId, request.getAmount(), "BET_" + bet.getId());

        BetEntry entry = BetEntry.builder()
                .userId(userId)
                .bet(bet)
                .amount(request.getAmount())
                .selectedOption(request.getSelectedOption())
                .build();

        entry = betEntryRepository.save(entry);
        log.info("User {} placed bet of {} points on option '{}' for bet {}",
                userId, request.getAmount(), request.getSelectedOption(), bet.getId());

        return mapToBetEntryResponse(entry);
    }

    @Transactional
    public BetResponse resolveBet(UUID requestingUserId, ResolveBetRequest request, boolean isAdmin) {
        Bet bet = betRepository.findByIdWithEntries(request.getBetId());
        if (bet == null) {
            throw new IllegalArgumentException("Bet not found");
        }

        // Permission check
        if (!isAdmin && !bet.getCreatorId().equals(requestingUserId)) {
            throw new SecurityException("Only the bet creator or admin can resolve this bet");
        }

        // Validation
        if (bet.getStatus() == BetStatus.RESOLVED || bet.getStatus() == BetStatus.CANCELLED) {
            throw new IllegalStateException("Bet is already " + bet.getStatus());
        }

        if (!bet.getOptions().contains(request.getWinningOption())) {
            throw new IllegalArgumentException("Invalid winning option");
        }

        // **CRITICAL: Pool Distribution Logic**
        distributeWinnings(bet, request.getWinningOption());

        bet.setStatus(BetStatus.RESOLVED);
        bet.setWinningOption(request.getWinningOption());
        bet.setResolvedAt(LocalDateTime.now());
        bet = betRepository.save(bet);

        log.info("Bet {} resolved with winning option: {}", bet.getId(), request.getWinningOption());

        return mapToBetResponse(bet, null);
    }

    /**
     * CORE BUSINESS LOGIC: Distribute pool among winners proportionally
     *
     * Algorithm:
     * 1. Calculate total pool (sum of all bets)
     * 2. Calculate winning pool (sum of bets on winning option)
     * 3. For each winner: winnings = (their bet / winning pool) * total pool
     * 4. If nobody won, points are burned (alternative: return to all)
     */
    private void distributeWinnings(Bet bet, String winningOption) {
        List<BetEntry> allEntries = bet.getEntries();
        long totalPool = allEntries.stream().mapToLong(BetEntry::getAmount).sum();

        List<BetEntry> winners = allEntries.stream()
                .filter(entry -> entry.getSelectedOption().equals(winningOption))
                .collect(Collectors.toList());

        if (winners.isEmpty()) {
            log.warn("No winners for bet {}. Points are burned.", bet.getId());
            // Alternative: Refund everyone
            // allEntries.forEach(entry -> {
            //     walletService.addPoints(entry.getUserId(), entry.getAmount(), "BET_REFUND_" + bet.getId());
            //     entry.setWinnings(entry.getAmount());
            //     entry.setSettled(true);
            // });
            return;
        }

        long winningPool = winners.stream().mapToLong(BetEntry::getAmount).sum();

        for (BetEntry winner : winners) {
            // Calculate proportional share
            double shareRatio = (double) winner.getAmount() / winningPool;
            long winnings = Math.round(shareRatio * totalPool);

            winner.setWinnings(winnings);
            winner.setSettled(true);

            // Credit winnings to wallet
            // walletService.addPoints(winner.getUserId(), winnings, "BET_WIN_" + bet.getId());

            log.info("User {} won {} points (bet: {}, share: {:.2f}%)",
                    winner.getUserId(), winnings, winner.getAmount(), shareRatio * 100);
        }

        betEntryRepository.saveAll(allEntries);
    }

    @Transactional
    public void cancelBet(UUID betId, UUID requestingUserId, boolean isAdmin) {
        if (!isAdmin) {
            throw new SecurityException("Only admins can cancel bets");
        }

        Bet bet = betRepository.findByIdWithEntries(betId);
        if (bet == null) {
            throw new IllegalArgumentException("Bet not found");
        }

        if (bet.getStatus() == BetStatus.RESOLVED) {
            throw new IllegalStateException("Cannot cancel a resolved bet");
        }

        // Refund all participants
        for (BetEntry entry : bet.getEntries()) {
            // walletService.addPoints(entry.getUserId(), entry.getAmount(), "BET_CANCEL_" + betId);
            entry.setWinnings(entry.getAmount());
            entry.setSettled(true);
        }

        bet.setStatus(BetStatus.CANCELLED);
        betRepository.save(bet);
        betEntryRepository.saveAll(bet.getEntries());

        log.info("Bet {} cancelled by admin {}", betId, requestingUserId);
    }

    public List<BetResponse> getActiveBets(UUID userId) {
        List<Bet> bets = betRepository.findActiveBets();
        return bets.stream()
                .map(bet -> {
                    BetEntry userEntry = betEntryRepository
                            .findByUserIdAndBetId(userId, bet.getId())
                            .orElse(null);
                    return mapToBetResponse(bet, userEntry);
                })
                .collect(Collectors.toList());
    }

    public BetResponse getBetById(UUID betId, UUID userId) {
        Bet bet = betRepository.findById(betId)
                .orElseThrow(() -> new IllegalArgumentException("Bet not found"));

        BetEntry userEntry = betEntryRepository
                .findByUserIdAndBetId(userId, betId)
                .orElse(null);

        return mapToBetResponse(bet, userEntry);
    }

    public List<BetResponse> getUserBets(UUID userId) {
        List<BetEntry> entries = betEntryRepository.findByUserId(userId);
        return entries.stream()
                .map(entry -> {
                    Bet bet = entry.getBet();
                    return mapToBetResponse(bet, entry);
                })
                .collect(Collectors.toList());
    }

    // Scheduled task to auto-lock bets when deadline passes
    // @Scheduled(fixedRate = 60000) // Every minute
    public void autoLockExpiredBets() {
        List<Bet> expiredBets = betRepository.findExpiredOpenBets(LocalDateTime.now());
        for (Bet bet : expiredBets) {
            bet.setStatus(BetStatus.LOCKED);
        }
        if (!expiredBets.isEmpty()) {
            betRepository.saveAll(expiredBets);
            log.info("Auto-locked {} expired bets", expiredBets.size());
        }
    }

    private BetResponse mapToBetResponse(Bet bet, BetEntry userEntry) {
        Map<String, Long> poolByOption = bet.getOptions().stream()
                .collect(Collectors.toMap(
                        option -> option,
                        option -> bet.getPoolForOption(option)
                ));

        Map<String, Integer> entriesByOption = bet.getOptions().stream()
                .collect(Collectors.toMap(
                        option -> option,
                        option -> (int) bet.getEntries().stream()
                                .filter(e -> e.getSelectedOption().equals(option))
                                .count()
                ));

        return BetResponse.builder()
                .id(bet.getId())
                .creatorId(bet.getCreatorId())
                .topic(bet.getTopic())
                .options(bet.getOptions())
                .status(bet.getStatus())
                .bettingDeadline(bet.getBettingDeadline())
                .resolutionDate(bet.getResolutionDate())
                .winningOption(bet.getWinningOption())
                .createdAt(bet.getCreatedAt())
                .resolvedAt(bet.getResolvedAt())
                .totalPool(bet.getTotalPool())
                .poolByOption(poolByOption)
                .entriesByOption(entriesByOption)
                .totalEntries(bet.getEntries().size())
                .userEntry(userEntry != null ? mapToBetEntryResponse(userEntry) : null)
                .build();
    }

    private BetEntryResponse mapToBetEntryResponse(BetEntry entry) {
        return BetEntryResponse.builder()
                .id(entry.getId())
                .userId(entry.getUserId())
                .betId(entry.getBet().getId())
                .amount(entry.getAmount())
                .selectedOption(entry.getSelectedOption())
                .placedAt(entry.getPlacedAt())
                .winnings(entry.getWinnings())
                .settled(entry.getSettled())
                .build();
    }

    public List<BetResponse> getSettledBets() {
        // Definiujemy, co to znaczy "zakończony zakład"
        List<BetStatus> completedStatuses = List.of(BetStatus.RESOLVED, BetStatus.CANCELLED);

        return betRepository.findByStatusInOrderByResolutionDateDesc(completedStatuses)
                .stream()
                .map(bet -> mapToBetResponse(bet, null))
                .collect(Collectors.toList());
    }
}
