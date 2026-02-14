package pl.most.backend.features.intentions.model;

public enum IntentionStatus {
    PENDING,    // Czeka na decyzję podprzęsłowego
    APPROVED,   // Zatwierdzona - trafi na ołtarz
    REJECTED,   // Odrzucona (np. spam)
    COMPLETED   // Już po Mszy (omodlona)
}