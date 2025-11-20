package pl.most.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDto {

    @NotBlank(message = "Tytuł jest wymagany")
    private String title;

    private String description;

    @NotNull(message = "Data rozpoczęcia jest wymagana")
    private LocalDateTime startDate;

    @NotNull(message = "Data zakończenia jest wymagana")
    private LocalDateTime endDate;

    private String location;

    private Integer maxParticipants;

    private Boolean allowRsvp;
}