package pl.most.backend.features.scheduler.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VolunteerInfo {
    private String id;
    private String displayName;
    private String status;
    private boolean wasPresent;
    private String profileImage;
}
