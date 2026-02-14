package pl.most.backend.features.points.model;

public enum TransactionType {
    MANUAL_AWARD,   // Ręczne przyznanie przez lidera
    BET_ENTRY,      // Wejście w zakład (minusowe punkty)
    BET_WIN,        // Wygrana w zakładzie
    TASK_COMPLETION // Wykonanie zadania
}
