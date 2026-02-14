package pl.most.backend.features.intentions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pl.most.backend.features.intentions.model.IntentionType;
import java.time.LocalDate;

@Data
public class CreateIntentionRequest {
    @NotBlank(message = "Treść intencji jest wymagana")
    private String content;

    @NotNull(message = "Typ intencji jest wymagany")
    private IntentionType type;

    private boolean isAnonymous;

    // Wymagane TYLKO dla MASS_INTENTION. Dla BOX jest ignorowane.
    private LocalDate userSelectedDate;
}