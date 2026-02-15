package pl.most.backend.features.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pl.most.backend.model.entity.User;

@Data
public class AdminUpdateUserRequest {

    @NotNull
    private User.Role role;

    @NotNull
    @Min(0)
    private Integer points;
}
