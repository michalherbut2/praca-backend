package pl.most.backend.features.intentions.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.most.backend.features.intentions.dto.CreateIntentionRequest;
import pl.most.backend.features.intentions.dto.IntentionResponse;
import pl.most.backend.features.intentions.dto.ReviewIntentionRequest;
import pl.most.backend.features.intentions.model.Intention;
import pl.most.backend.features.intentions.model.IntentionStatus;
import pl.most.backend.features.intentions.model.IntentionType;
import pl.most.backend.features.intentions.repository.IntentionRepository;
import pl.most.backend.features.notifications.model.NotificationType;
import pl.most.backend.features.notifications.service.NotificationService;
import pl.most.backend.features.user.repository.UserRepository;
import pl.most.backend.model.entity.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntentionService {

    private final IntentionRepository intentionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // Twój serwis powiadomień

    // Metoda dla Usera: Dodaj intencję
    public IntentionResponse createIntention(UUID userId, CreateIntentionRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate targetDate;

        if (request.getType() == IntentionType.BOX_INTENTION) {
            // Opcja 3: Skrzynka -> System sam wybiera datę (Środa/Niedziela)
            targetDate = calculateNextMassDate();
        } else {
            // Opcja 2: Msza Indywidualna -> User wybrał datę w kalendarzu
            if (request.getUserSelectedDate() == null) {
                throw new IllegalArgumentException("Dla intencji mszalnej musisz wybrać datę!");
            }
            if (request.getUserSelectedDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Nie można zamówić intencji na datę wsteczną");
            }
            targetDate = request.getUserSelectedDate();
        }

        Intention intention = Intention.builder()
                .author(user)
                .content(request.getContent())
                .type(request.getType())
                .targetDate(targetDate)
                .isAnonymous(request.isAnonymous())
                .status(IntentionStatus.PENDING) // Zawsze czeka na akceptację
                .build();

        Intention saved = intentionRepository.save(intention);
        return mapToResponse(saved);
    }

    public List<IntentionResponse> getMyIntentions(UUID userId) {
        return intentionRepository.findAllByAuthorIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Dla Admina: Pobierz wszystkie oczekujące
    public List<IntentionResponse> getPendingIntentions() {
        return intentionRepository.findAllByStatusOrderByTargetDateAsc(IntentionStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Dla Admina: Pobierz zatwierdzone na dany dzień (do druku)
    public List<IntentionResponse> getApprovedForDate(LocalDate date) {
        return intentionRepository.findAllByTargetDateAndStatus(date, IntentionStatus.APPROVED).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Metoda dla Podprzęsłowego: Zatwierdź/Odrzuć
    @Transactional
    public IntentionResponse reviewIntention(UUID intentionId, ReviewIntentionRequest request) {
        Intention intention = intentionRepository.findById(intentionId)
                .orElseThrow(() -> new RuntimeException("Intention not found"));

        if (request.isApproved()) {
            intention.setStatus(IntentionStatus.APPROVED);
            // Powiadomienie Push/In-App
            notificationService.send(
                    intention.getAuthor(),
                    "Intencja przyjęta ✅",
                    "Twoja intencja na dzień " + intention.getTargetDate() + " została zaakceptowana.",
                    NotificationType.SUCCESS,
                    intention.getId()
            );
        } else {
            intention.setStatus(IntentionStatus.REJECTED);
            String reason = request.getAdminResponse() != null ? request.getAdminResponse() : "Brak powodu.";
            notificationService.send(
                    intention.getAuthor(),
                    "Intencja odrzucona ⚠️",
                    "Wiadomość od opiekuna: " + reason,
                    NotificationType.WARNING,
                    intention.getId()
            );
        }
        if (request.getAdminResponse() != null && !request.getAdminResponse().isEmpty()) {
            intention.setAdminResponse(request.getAdminResponse());
        }

        return mapToResponse(intentionRepository.save(intention));
    }

    private void validateBoxDate(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day != DayOfWeek.WEDNESDAY && day != DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Intencje do skrzynki tylko w Środy i Niedziele!");
        }
    }

//    public void approveIntention(UUID intentionId) {
//        Intention intention = repository.findById(intentionId).orElseThrow();
//        intention.setStatus(IntentionStatus.APPROVED);
//        repository.save(intention);
//
//        // Tworzymy powiadomienie
//        notificationService.send(
//            intention.getAuthor().getId(),
//            "Intencja przyjęta ✅",
//            "Twoja intencja na dzień " + intention.getMassDate() + " została zaakceptowana."
//        );
//    }

    private LocalDate calculateNextMassDate() {
        LocalDate today = LocalDate.now();

        // Znajdź najbliższą (lub dzisiejszą) środę
        LocalDate nextWed = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));

        // Znajdź najbliższą (lub dzisiejszą) niedzielę
        LocalDate nextSun = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // LUB: Możesz dodać logikę godziny (np. cutoff o 18:00).
        return nextWed.isBefore(nextSun) ? nextWed : nextSun;
    }

    private IntentionResponse mapToResponse(Intention intention) {
        return IntentionResponse.builder()
                .id(intention.getId())
                .content(intention.getContent())
                .type(intention.getType())
                .status(intention.getStatus())
                .targetDate(intention.getTargetDate())
                .isAnonymous(intention.isAnonymous())
                .adminResponse(intention.getAdminResponse())
                .createdAt(intention.getCreatedAt())
                // Jeśli anonim, ukrywamy nazwisko (chyba że admin patrzy, ale to prosta logika)
                .authorName(intention.isAnonymous() ? "Anonim" :
                        intention.getAuthor().getFirstName() + " " + intention.getAuthor().getLastName())
                .build();
    }
}

