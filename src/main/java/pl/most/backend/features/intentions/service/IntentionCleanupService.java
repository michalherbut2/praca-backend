package pl.most.backend.features.intentions.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.most.backend.features.intentions.model.Intention;
import pl.most.backend.features.intentions.model.IntentionStatus;
import pl.most.backend.features.intentions.repository.IntentionRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntentionCleanupService {
    private final IntentionRepository intentionRepository;

    @Scheduled(cron = "0 0 23 * * WED,SUN") // W każdą Środę i Niedzielę o 23:00
    @Transactional
    public void archiveOldIntentions() {
        LocalDate today = LocalDate.now();
        
        // Znajdź wszystkie ZATWIERDZONE z dzisiaj (lub wcześniej)
        List<Intention> oldIntentions = intentionRepository
            .findAllByStatusAndTargetDateLessThanEqual(IntentionStatus.APPROVED, today);

        for (Intention i : oldIntentions) {
            i.setStatus(IntentionStatus.COMPLETED);
            // Opcjonalnie: wyślij powiadomienie "Twoja intencja została omodlona 🙏"
        }
    }
}