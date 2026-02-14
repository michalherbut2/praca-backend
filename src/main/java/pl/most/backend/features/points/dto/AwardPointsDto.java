// AwardPointsDto.java
package pl.most.backend.features.points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AwardPointsDto {

    @NotBlank
    private UUID userId;

    @NotNull
    private Integer amount;

    @NotBlank
    private String reason;
}

