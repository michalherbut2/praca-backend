package pl.most.backend.features.duties.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.most.backend.features.notifications.model.NotificationType;
import pl.most.backend.features.notifications.service.NotificationService;
import pl.most.backend.features.points.model.PointsTransaction;
import pl.most.backend.features.points.model.TransactionType;
import pl.most.backend.features.points.repository.PointsTransactionRepository;
import pl.most.backend.features.duties.dto.DutySlotResponse;
import pl.most.backend.features.duties.dto.VolunteerInfo;
import pl.most.backend.features.duties.model.*;
import pl.most.backend.features.duties.repository.DutySlotRepository;
import pl.most.backend.features.duties.repository.DutyVolunteerRepository;
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
public class DutyService {

    private final DutySlotRepository slotRepository;
    private final DutyVolunteerRepository volunteerRepository;
    private final UserRepository userRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final NotificationService notificationService;

    // ─── QUERIES ─────────────────────────────────────────────────────────────

    public List<DutySlotResponse> getSlots(
            DutyCategory category,
            LocalDate dateFrom,
            LocalDate dateTo,
            UUID currentUserId,
            boolean isAdmin,
            boolean includePast) {

        // When includePast=false, clamp dateFrom to today so past slots are excluded
        LocalDate effectiveFrom = includePast ? dateFrom
                : dateFrom.isBefore(LocalDate.now()) ? LocalDate.now() : dateFrom;

        List<DutySlot> slots = slotRepository
                .findAllByCategoryAndDateBetweenOrderByDateAscTimeAsc(category, effectiveFrom, dateTo);

        return slots.stream()
                .map(slot -> mapToResponse(slot, currentUserId, isAdmin))
                .collect(Collectors.toList());
    }

    // ─── SIGN UP ─────────────────────────────────────────────────────────────

    @Transactional
    public DutySlotResponse signUp(UUID slotId, UUID userId, boolean isAnonymous) {
        DutySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot nie istnieje"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        // Sprawdź czy już zapisany
        if (volunteerRepository.existsBySlotIdAndUserId(slotId, userId)) {
            throw new IllegalStateException("Jesteś już zapisany na tę służbę");
        }

        // Policz zatwierdzone miejsca
        long approvedCount = volunteerRepository.countBySlotIdAndStatus(slotId, DutyVolunteerStatus.APPROVED);

        DutyVolunteerStatus status;
        if (slot.isAutoApproved() && approvedCount < slot.getCapacity()) {
            status = DutyVolunteerStatus.APPROVED;
        } else {
            status = DutyVolunteerStatus.PENDING;
        }

        DutyVolunteer volunteer = DutyVolunteer.builder()
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
        DutyVolunteer volunteer = volunteerRepository.findBySlotIdAndUserId(slotId, userId)
                .orElseThrow(() -> new RuntimeException("Nie jesteś zapisany na tę służbę"));

        volunteerRepository.delete(volunteer);
    }
    // ─── APPROVE VOLUNTEER (ADMIN) ──────────────────────────────────────────

    @Transactional
    public void approveVolunteer(UUID volunteerId) {
        DutyVolunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Wolontariusz nie istnieje"));

        if (volunteer.getStatus() == DutyVolunteerStatus.APPROVED) {
            throw new IllegalStateException("Wolontariusz jest już zatwierdzony");
        }

        volunteer.setStatus(DutyVolunteerStatus.APPROVED);
        volunteerRepository.save(volunteer);

        // Powiadomienie wolontariusza
        DutySlot slot = volunteer.getSlot();
        notificationService.send(
                volunteer.getUser(),
                "Zgłoszenie zatwierdzone ✅",
                "Twoje zgłoszenie na \"" + slot.getTitle() + "\" (" + slot.getDate() + ") zostało zatwierdzone.",
                NotificationType.SUCCESS,
                slot.getId());
    }

    // ─── CONFIRM PRESENCE (ADMIN) ────────────────────────────────────────────

    @Transactional
    public void confirmPresence(UUID volunteerId, UUID adminUserId) {
        DutyVolunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Wolontariusz nie istnieje"));

        volunteer.setWasPresent(true);

        DutySlot slot = volunteer.getSlot();

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

    // ─── ADMIN: CREATE / DELETE ──────────────────────────────────────────────

    @Transactional
    public DutySlotResponse createSlot(pl.most.backend.features.duties.dto.CreateSlotRequest request) {
        DutySlot slot = DutySlot.builder()
                .title(request.getTitle())
                .date(request.getDate())
                .time(request.getTime())
                .category(request.getCategory())
                .capacity(request.getCapacity())
                .pointsValue(request.getPointsValue())
                .isAutoApproved(request.isAutoApproved())
                .build();

        DutySlot saved = slotRepository.save(slot);
        return mapToResponse(saved, null, true);
    }

    @Transactional
    public DutySlotResponse updateSlot(UUID slotId, pl.most.backend.features.duties.dto.UpdateSlotRequest request) {
        DutySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot nie istnieje"));

        // Validate: new capacity must not be below current approved count
        long approvedCount = volunteerRepository.countBySlotIdAndStatus(slotId, DutyVolunteerStatus.APPROVED);
        if (request.getCapacity() < approvedCount) {
            throw new IllegalStateException(
                    "Nie można zmniejszyć limitu poniżej liczby zatwierdzonych osób (" + approvedCount + ")");
        }

        slot.setTitle(request.getTitle());
        slot.setDate(request.getDate());
        slot.setTime(request.getTime());
        slot.setCategory(request.getCategory());
        slot.setCapacity(request.getCapacity());
        slot.setPointsValue(request.getPointsValue());
        slot.setAutoApproved(request.isAutoApproved());

        DutySlot saved = slotRepository.save(slot);
        return mapToResponse(saved, null, true);
    }

    @Transactional
    public void deleteSlot(UUID slotId) {
        DutySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot nie istnieje"));
        slotRepository.delete(slot); // CascadeType.ALL usunie wolontariuszy
    }

    // ─── GENERATORS ──────────────────────────────────────────────────────────

    @Transactional
    public List<DutySlotResponse> generateLiturgyWeek(LocalDate date) {
        // Zawsze normalizuj do poniedziałku tygodnia zawierającego podaną datę
        LocalDate startMonday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<DutySlot> generated = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day = startMonday.plusDays(i);
            DayOfWeek dow = day.getDayOfWeek();

            // Sobota — brak slotów
            if (dow == DayOfWeek.SATURDAY)
                continue;

            // Pomiń jeśli na ten dzień już wygenerowano sloty
            if (slotRepository.existsByCategoryAndDate(DutyCategory.LITURGY, day))
                continue;

            if (dow == DayOfWeek.SUNDAY) {
                // Niedziela: Czytanie 1, Czytanie 2, Psalm
                generated.add(createLiturgySlot(day, "Czytanie 1"));
                // generated.add(createLiturgySlot(day, "Czytanie 2"));
                // generated.add(createLiturgySlot(day, "Psalm"));
            } else if (dow == DayOfWeek.TUESDAY) {
                // Wtorek: Czytanie 1, Psalm
                generated.add(createLiturgySlot(day, "Czytanie 1"));
                generated.add(createLiturgySlot(day, "Psalm"));
            } else {
                // Pon, Śr, Czw, Pt: Czytanie 1
                generated.add(createLiturgySlot(day, "Czytanie 1"));
            }
        }

        List<DutySlot> saved = slotRepository.saveAll(generated);
        return saved.stream()
                .map(s -> mapToResponse(s, null, true))
                .collect(Collectors.toList());
    }

    @Transactional
    public DutySlotResponse generateSundayKitchen(LocalDate sunday) {
        if (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Data musi być niedzielą!");
        }

        if (slotRepository.existsByCategoryAndDate(DutyCategory.KITCHEN, sunday)) {
            throw new IllegalStateException("Slot kuchenny na ten dzień już istnieje");
        }

        DutySlot slot = DutySlot.builder()
                .date(sunday)
                .time(LocalTime.of(14, 0))
                .category(DutyCategory.KITCHEN)
                .title("Przygotowanie kolacji")
                .capacity(5)
                .isAutoApproved(true)
                .pointsValue(1)
                .build();

        DutySlot saved = slotRepository.save(slot);
        return mapToResponse(saved, null, true);
    }

    // ─── MAPPING ─────────────────────────────────────────────────────────────

    private DutySlot createLiturgySlot(LocalDate date, String title) {
        return DutySlot.builder()
                .date(date)
                .time(LocalTime.of(18, 0)) // Domyślna godzina Mszy
                .category(DutyCategory.LITURGY)
                .title(title)
                .capacity(1)
                .isAutoApproved(false)
                .pointsValue(0)
                .build();
    }

    private DutySlotResponse mapToResponse(DutySlot slot, UUID requesterId, boolean isRequesterAdmin) {
        long approvedCount = volunteerRepository.countBySlotIdAndStatus(slot.getId(), DutyVolunteerStatus.APPROVED);

        boolean isSignedUp = requesterId != null
                && volunteerRepository.existsBySlotIdAndUserId(slot.getId(), requesterId);

        List<VolunteerInfo> volunteerInfos = slot.getVolunteers().stream()
                .map(v -> mapVolunteerInfo(v, isRequesterAdmin, requesterId))
                .collect(Collectors.toList());

        return DutySlotResponse.builder()
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

    private VolunteerInfo mapVolunteerInfo(DutyVolunteer v, boolean isRequesterAdmin, UUID requesterId) {
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
