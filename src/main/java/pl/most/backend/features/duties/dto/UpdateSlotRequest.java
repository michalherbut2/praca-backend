package pl.most.backend.features.duties.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pl.most.backend.features.duties.model.DutyCategory;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateSlotRequest {

    @NotBlank
    private String title;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime time;

    @NotNull
    private DutyCategory category;

    @Min(1)
    private int capacity;

    @Min(0)
    private int pointsValue;

    private boolean autoApproved;
}
