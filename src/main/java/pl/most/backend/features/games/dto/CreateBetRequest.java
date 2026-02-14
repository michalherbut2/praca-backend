package pl.most.backend.features.games.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBetRequest {

    @NotBlank(message = "Topic is required")
    @Size(min = 10, max = 500, message = "Topic must be between 10 and 500 characters")
    private String topic;

    @NotNull(message = "Options are required")
    @Size(min = 2, max = 10, message = "Must have between 2 and 10 options")
    private List<@NotBlank String> options;

    @NotNull(message = "Betting deadline is required")
    @Future(message = "Betting deadline must be in the future")
    private LocalDateTime bettingDeadline;

    @NotNull(message = "Resolution date is required")
    @Future(message = "Resolution date must be in the future")
    private LocalDateTime resolutionDate;
}