package pl.most.backend.features.games.enums;

public enum BetStatus {
    OPEN,       // Accepting bets
    LOCKED,     // No more bets, waiting for resolution
    RESOLVED,   // Outcome determined, winnings distributed
    CANCELLED   // Bet cancelled, refunds issued
}