package pl.most.backend.features.scheduler.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.most.backend.features.notifications.model.NotificationType;
import pl.most.backend.features.notifications.service.NotificationService;
import pl.most.backend.features.points.model.PointsTransaction;
import pl.most.backend.features.points.model.TransactionType;
import pl.most.backend.features.points.repository.PointsTransactionRepository;
import pl.most.backend.features.scheduler.dto.ServiceSlotResponse;
import pl.most.backend.features.scheduler.dto.VolunteerInfo;
import pl.most.backend.features.scheduler.model.*;
import pl.most.backend.features.scheduler.repository.ServiceSlotRepository;
import pl.most.backend.features.scheduler.repository.ServiceVolunteerRepository;
import pl.most.backend.features.user.repository.UserRepository;
import pl.most.backend.model.entity.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceSchedulerService {

    private final ServiceSlotRepository slotRepository;
    private final ServiceVolunteerRepository volunteerRepository;
    private final UserRepository userRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final NotificationService notificationService;

    // ─── QUERIES ─────────────────────────────────────────────────────────────

    public List<ServiceSlotResponse> getSlots(
            ServiceCategory category,
            LocalDate dateFrom,
            LocalDate dateTo,
            UUID currentUserId,
            boolean isAdmin) {
        List<ServiceSlot> slots = slotRepository
                .findAllByCategoryAndDateBetweenOrderByDateAscTimeAsc(category, dateFrom, dateTo);

        return slots.stream()
                .map(slot -> mapToResponse(slot, currentUserId, isAdmin))
                .collect(Collectors.toList());
    }

    // ─── SIGN UP ─────────────────────────────────────────────────────────────

    @Transactional
    public ServiceSlotResponse signUp(UUID slotId, UUID userId, boolean isAnonymous) {
        ServiceSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot nie istnieje"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        // Sprawdź czy już zapisany
        if (volunteerRepository.existsBySlotIdAndUserId(slotId, userId)) {
            throw new IllegalStateException("Jesteś już zapisany na tę służbę");
        }

        // Policz zatwierdzone miejsca
        long approvedCount = volunteerRepository.countBySlotIdAndStatus(slotId, ServiceVolunteerStatus.APPROVED);

        ServiceVolunteerStatus status;
        if (slot.isAutoApproved() && approvedCount < slot.getCapacity()) {
            status = ServiceVolunteerStatus.APPROVED;
        } else {
            status = ServiceVolunteerStatus.PENDING;
        }

        ServiceVolunteer volunteer = ServiceVolunteer.builder()
                .user(user)
                .slot(slot)
                .status(status)
                .isAnonymous(isAnonymous)
                .build();

        volunteerRepository.save(volunteer);

        return mapToResponse(
                slotRepository.findById(slotId).orElseThrow(),
                userId,
                false);
    }

    // ─── CANCEL ──────────────────────────────────────────────────────────────

    @Transactional
    public void cancelSignUp(UUID slotId, UUID userId) {
        ServiceVolunteer volunteer = volunteerRepository.findBySlotIdAndUserId(slotId, userId)
                .orElseThrow(() -> new RuntimeException("Nie jesteś zapisany na tę służbę"));

        volunteerRepository.delete(volunteer);
    }

    // ─── CONFIRM PRESENCE (ADMIN) ────────────────────────────────────────────

    @Transactional
    public void confirmPresence(UUID volunteerId, UUID adminUserId) {
        ServiceVolunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Wolontariusz nie istnieje"));

        volunteer.setWasPresent(true);

        ServiceSlot slot = volunteer.getSlot();

        // Dodaj punkty jeśli jeszcze nie przyznano i slot ma wartość punktową
        if (!volunteer.isPointsAwarded() && slot.getPointsValue() > 0) {
            userRepository.updateUserPoints(volunteer.getUser().getId(), slot.getPointsValue());

            PointsTransaction tx = PointsTransaction.builder()
                    .user(volunteer.getUser())
                    .amount(slot.getPointsValue())
                    .type(TransactionType.TASK_COMPLETION)
                    .description("Służba: " + slot.getTitle() + " (" + slot.getDate() + ")")
                    .sourceId(slot.getId())
                    .createdBy(adminUserId)
                    .build();
            pointsTransactionRepository.save(tx);

            volunteer.setPointsAwarded(true);

            // Powiadomienie
            notificationService.send(
                    volunteer.getUser(),
                    "Punkty za służbę 🎉",
                    "Otrzymałeś +" + slot.getPointsValue() + " pkt za \"" + slot.getTitle() + "\" (" + slot.getDate()
                            + ").",
                    NotificationType.SUCCESS,
                    slot.getId());
        }

        volunteerRepository.save(volunteer);
    }

    // ─── GENERATORS ──────────────────────────────────────────────────────────

    @Transactional
    public List<ServiceSlotResponse> generateLiturgyWeek(LocalDate date) {
        // Zawsze normalizuj do poniedziałku tygodnia zawierającego podaną datę
        LocalDate startMonday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<ServiceSlot> generated = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day = startMonday.plusDays(i);
            DayOfWeek dow = day.getDayOfWeek();

            // Sobota — brak slotów
            if (dow == DayOfWeek.SATURDAY)
                continue;

            // Pomiń jeśli na ten dzień już wygenerowano sloty
            if (slotRepository.existsByCategoryAndDate(ServiceCategory.LITURGY, day))
                continue;

            if (dow == DayOfWeek.SUNDAY) {
                // Niedziela: Czytanie 1, Czytanie 2, Psalm
                generated.add(createLiturgySlot(day, "Czytanie 1"));
                generated.add(createLiturgySlot(day, "Czytanie 2"));
                generated.add(createLiturgySlot(day, "Psalm"));
            } else if (dow == DayOfWeek.TUESDAY) {
                // Wtorek: Czytanie 1, Psalm
                generated.add(createLiturgySlot(day, "Czytanie 1"));
                generated.add(createLiturgySlot(day, "Psalm"));
            } else {
                // Pon, Śr, Czw, Pt: Czytanie 1
                generated.add(createLiturgySlot(day, "Czytanie 1"));
            }
        }

        List<ServiceSlot> saved = slotRepository.saveAll(generated);
        return saved.stream()
                .map(s -> mapToResponse(s, null, true))
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceSlotResponse generateSundayKitchen(LocalDate sunday) {
        if (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Data musi być niedzielą!");
        }

        if (slotRepository.existsByCategoryAndDate(ServiceCategory.KITCHEN, sunday)) {
            throw new IllegalStateException("Slot kuchenny na ten dzień już istnieje");
        }

        ServiceSlot slot = ServiceSlot.builder()
                .date(sunday)
                .time(LocalTime.of(14, 0))
                .category(ServiceCategory.KITCHEN)
                .title("Przygotowanie kolacji")
                .capacity(5)
                .isAutoApproved(true)
                .pointsValue(1)
                .build();

        ServiceSlot saved = slotRepository.save(slot);
        return mapToResponse(saved, null, true);
    }

    // ─── MAPPING ─────────────────────────────────────────────────────────────

    private ServiceSlot createLiturgySlot(LocalDate date, String title) {
        return ServiceSlot.builder()
                .date(date)
                .time(LocalTime.of(18, 0)) // Domyślna godzina Mszy
                .category(ServiceCategory.LITURGY)
                .title(title)
                .capacity(1)
                .isAutoApproved(false)
                .pointsValue(0)
                .build();
    }

    private ServiceSlotResponse mapToResponse(ServiceSlot slot, UUID requesterId, boolean isRequesterAdmin) {
        long approvedCount = volunteerRepository.countBySlotIdAndStatus(slot.getId(), ServiceVolunteerStatus.APPROVED);

        boolean isSignedUp = requesterId != null
                && volunteerRepository.existsBySlotIdAndUserId(slot.getId(), requesterId);

        List<VolunteerInfo> volunteerInfos = slot.getVolunteers().stream()
                .map(v -> mapVolunteerInfo(v, isRequesterAdmin, requesterId))
                .collect(Collectors.toList());

        return ServiceSlotResponse.builder()
                .id(slot.getId())
                .date(slot.getDate())
                .time(slot.getTime())
                .category(slot.getCategory().name())
                .title(slot.getTitle())
                .capacity(slot.getCapacity())
                .approvedCount((int) approvedCount)
                .isAutoApproved(slot.isAutoApproved())
                .pointsValue(slot.getPointsValue())
                .volunteers(volunteerInfos)
                .currentUserSignedUp(isSignedUp)
                .build();
    }

    private VolunteerInfo mapVolunteerInfo(ServiceVolunteer v, boolean isRequesterAdmin, UUID requesterId) {
        // Pokaż prawdziwe dane jeśli:
        // 1. Wolontariusz NIE jest anonimowy, LUB
        // 2. Requester jest adminem, LUB
        // 3. Requester to ten sam wolontariusz (widzi siebie)
        boolean showRealName = !v.isAnonymous()
                || isRequesterAdmin
                || (v.getUser().getId().equals(requesterId));

        String displayName;
        String profileImage = null;

        if (showRealName) {
            displayName = v.getUser().getFirstName() + " " + v.getUser().getLastName();
            profileImage = v.getUser().getProfileImage();
        } else {
            displayName = "Anonimowy Uczestnik";
        }

        return VolunteerInfo.builder()
                .id(v.getId().toString())
                .displayName(displayName)
                .status(v.getStatus().name())
                .wasPresent(v.isWasPresent())
                .profileImage(profileImage)
                .build();
    }
}
