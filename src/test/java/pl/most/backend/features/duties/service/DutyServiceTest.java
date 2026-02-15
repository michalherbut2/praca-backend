package pl.most.backend.features.duties.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.most.backend.features.notifications.service.NotificationService;
import pl.most.backend.features.points.repository.PointsTransactionRepository;
import pl.most.backend.features.duties.dto.DutySlotResponse;
import pl.most.backend.features.duties.model.*;
import pl.most.backend.features.duties.repository.DutySlotRepository;
import pl.most.backend.features.duties.repository.DutyVolunteerRepository;
import pl.most.backend.features.user.repository.UserRepository;
import pl.most.backend.model.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DutyServiceTest {

    @Mock
    private DutySlotRepository slotRepository;

    @Mock
    private DutyVolunteerRepository volunteerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointsTransactionRepository pointsTransactionRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DutyService service;

    private User testUser;
    private UUID slotId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        slotId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setFirstName("Jan");
        testUser.setLastName("Kowalski");
        testUser.setEmail("jan@test.pl");
        testUser.setPassword("password");
        testUser.setRole(User.Role.USER);
        testUser.setPoints(0);
    }

    private DutySlot createSlot(boolean autoApproved, int capacity) {
        return DutySlot.builder()
                .id(slotId)
                .date(LocalDate.of(2026, 2, 15))
                .time(LocalTime.of(18, 0))
                .category(DutyCategory.LITURGY)
                .title("Czytanie 1")
                .capacity(capacity)
                .isAutoApproved(autoApproved)
                .pointsValue(0)
                .volunteers(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("signUp()")
    class SignUpTests {

        @Test
        @DisplayName("autoApprove=true i jest miejsce → status APPROVED")
        void signUp_autoApproveWithCapacity_shouldReturnApproved() {
            // given
            DutySlot slot = createSlot(true, 2);

            when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(volunteerRepository.existsBySlotIdAndUserId(slotId, userId)).thenReturn(false);
            when(volunteerRepository.countBySlotIdAndStatus(slotId, DutyVolunteerStatus.APPROVED)).thenReturn(0L);
            when(volunteerRepository.save(any(DutyVolunteer.class))).thenAnswer(inv -> inv.getArgument(0));

            // mapToResponse potrzebuje ponownego pobrania slotu
            when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

            // when
            DutySlotResponse response = service.signUp(slotId, userId, false);

            // then
            ArgumentCaptor<DutyVolunteer> captor = ArgumentCaptor.forClass(DutyVolunteer.class);
            verify(volunteerRepository).save(captor.capture());

            DutyVolunteer saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(DutyVolunteerStatus.APPROVED);
            assertThat(saved.getUser()).isEqualTo(testUser);
            assertThat(saved.getSlot()).isEqualTo(slot);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("autoApprove=false → status PENDING")
        void signUp_noAutoApprove_shouldReturnPending() {
            // given
            DutySlot slot = createSlot(false, 2);

            when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(volunteerRepository.existsBySlotIdAndUserId(slotId, userId)).thenReturn(false);
            when(volunteerRepository.countBySlotIdAndStatus(slotId, DutyVolunteerStatus.APPROVED)).thenReturn(0L);
            when(volunteerRepository.save(any(DutyVolunteer.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            service.signUp(slotId, userId, false);

            // then
            ArgumentCaptor<DutyVolunteer> captor = ArgumentCaptor.forClass(DutyVolunteer.class);
            verify(volunteerRepository).save(captor.capture());

            assertThat(captor.getValue().getStatus()).isEqualTo(DutyVolunteerStatus.PENDING);
        }

        @Test
        @DisplayName("autoApprove=true ale slot pełny → status PENDING (brak miejsca)")
        void signUp_autoApproveButFull_shouldReturnPending() {
            // given
            DutySlot slot = createSlot(true, 1);

            when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(volunteerRepository.existsBySlotIdAndUserId(slotId, userId)).thenReturn(false);
            // Capacity = 1, a jeden już zatwierdzony
            when(volunteerRepository.countBySlotIdAndStatus(slotId, DutyVolunteerStatus.APPROVED)).thenReturn(1L);
            when(volunteerRepository.save(any(DutyVolunteer.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            service.signUp(slotId, userId, false);

            // then
            ArgumentCaptor<DutyVolunteer> captor = ArgumentCaptor.forClass(DutyVolunteer.class);
            verify(volunteerRepository).save(captor.capture());

            // Mimo autoApprove, capacity wyczerpana → PENDING
            assertThat(captor.getValue().getStatus()).isEqualTo(DutyVolunteerStatus.PENDING);
        }

        @Test
        @DisplayName("Użytkownik już zapisany → IllegalStateException")
        void signUp_alreadySignedUp_shouldThrow() {
            // given
            DutySlot slot = createSlot(true, 2);

            when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(volunteerRepository.existsBySlotIdAndUserId(slotId, userId)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> service.signUp(slotId, userId, false))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("już zapisany");
        }

        @Test
        @DisplayName("Slot nie istnieje → RuntimeException")
        void signUp_slotNotFound_shouldThrow() {
            // given
            when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> service.signUp(slotId, userId, false))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Slot nie istnieje");
        }
    }

    @Nested
    @DisplayName("getSlots() — logika anonimowości")
    class GetSlotsAnonymityTests {

        @Test
        @DisplayName("Zwykły user NIE widzi imienia anonimowego wolontariusza")
        void getSlots_anonymousVolunteer_userSeesAnonymousName() {
            // given
            UUID otherUserId = UUID.randomUUID();
            User otherUser = new User();
            otherUser.setId(otherUserId);
            otherUser.setFirstName("Anna");
            otherUser.setLastName("Nowak");

            DutySlot slot = createSlot(true, 2);
            DutyVolunteer anonymousVol = DutyVolunteer.builder()
                    .id(UUID.randomUUID())
                    .user(otherUser)
                    .slot(slot)
                    .status(DutyVolunteerStatus.APPROVED)
                    .isAnonymous(true)
                    .build();
            slot.getVolunteers().add(anonymousVol);

            when(slotRepository.findAllByCategoryAndDateBetweenOrderByDateAscTimeAsc(
                    any(), any(), any())).thenReturn(java.util.List.of(slot));
            when(volunteerRepository.countBySlotIdAndStatus(slotId, DutyVolunteerStatus.APPROVED)).thenReturn(1L);
            when(volunteerRepository.existsBySlotIdAndUserId(slotId, userId)).thenReturn(false);

            // when — requester jest zwykłym userem (nie adminem, nie wolontariuszem)
            var response = service.getSlots(
                    DutyCategory.LITURGY,
                    LocalDate.of(2026, 2, 15),
                    LocalDate.of(2026, 2, 15),
                    userId,
                    false);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).getVolunteers()).hasSize(1);
            assertThat(response.get(0).getVolunteers().get(0).getDisplayName())
                    .isEqualTo("Anonimowy Uczestnik");
        }

        @Test
        @DisplayName("Admin WIDZI prawdziwe imię anonimowego wolontariusza")
        void getSlots_anonymousVolunteer_adminSeesRealName() {
            // given
            UUID adminId = UUID.randomUUID();

            User volunteer = new User();
            volunteer.setId(UUID.randomUUID());
            volunteer.setFirstName("Anna");
            volunteer.setLastName("Nowak");

            DutySlot slot = createSlot(true, 2);
            DutyVolunteer anonymousVol = DutyVolunteer.builder()
                    .id(UUID.randomUUID())
                    .user(volunteer)
                    .slot(slot)
                    .status(DutyVolunteerStatus.APPROVED)
                    .isAnonymous(true)
                    .build();
            slot.getVolunteers().add(anonymousVol);

            when(slotRepository.findAllByCategoryAndDateBetweenOrderByDateAscTimeAsc(
                    any(), any(), any())).thenReturn(java.util.List.of(slot));
            when(volunteerRepository.countBySlotIdAndStatus(slotId, DutyVolunteerStatus.APPROVED)).thenReturn(1L);
            when(volunteerRepository.existsBySlotIdAndUserId(slotId, adminId)).thenReturn(false);

            // when — requester jest adminem
            var response = service.getSlots(
                    DutyCategory.LITURGY,
                    LocalDate.of(2026, 2, 15),
                    LocalDate.of(2026, 2, 15),
                    adminId,
                    true);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).getVolunteers().get(0).getDisplayName())
                    .isEqualTo("Anna Nowak");
        }

        @Test
        @DisplayName("Anonimowy wolontariusz WIDZI swoje własne imię")
        void getSlots_anonymousVolunteer_selfSeesOwnName() {
            // given
            DutySlot slot = createSlot(true, 2);
            DutyVolunteer anonymousVol = DutyVolunteer.builder()
                    .id(UUID.randomUUID())
                    .user(testUser) // sam requester jest wolontariuszem
                    .slot(slot)
                    .status(DutyVolunteerStatus.APPROVED)
                    .isAnonymous(true)
                    .build();
            slot.getVolunteers().add(anonymousVol);

            when(slotRepository.findAllByCategoryAndDateBetweenOrderByDateAscTimeAsc(
                    any(), any(), any())).thenReturn(java.util.List.of(slot));
            when(volunteerRepository.countBySlotIdAndStatus(slotId, DutyVolunteerStatus.APPROVED)).thenReturn(1L);
            when(volunteerRepository.existsBySlotIdAndUserId(slotId, userId)).thenReturn(true);

            // when — requester to sam wolontariusz, nie admin
            var response = service.getSlots(
                    DutyCategory.LITURGY,
                    LocalDate.of(2026, 2, 15),
                    LocalDate.of(2026, 2, 15),
                    userId,
                    false);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).getVolunteers().get(0).getDisplayName())
                    .isEqualTo("Jan Kowalski");
        }
    }
}
