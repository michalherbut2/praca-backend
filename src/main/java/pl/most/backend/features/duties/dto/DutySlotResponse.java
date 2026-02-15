package pl.most.backend.features.duties.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DutySlotResponse {
    private UUID id;
    private LocalDate date;
    private LocalTime time;
    private String category;
    private String title;
    private int capacity;
    private int approvedCount;
    private boolean isAutoApproved;
    private int pointsValue;
    private List<VolunteerInfo> volunteers;
    private boolean currentUserSignedUp;
}
