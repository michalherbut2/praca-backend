package pl.most.backend.model.dto;

import lombok.Builder;
import lombok.Data;
import pl.most.backend.model.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private int points;
    private boolean isActive;
    private String profileImage;
    private String sectionId;
    private String sectionName;
    private boolean emailVerified;
    private LocalDateTime createdAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .points(user.getPoints())
                .isActive(user.getIsActive())
                .profileImage(user.getProfileImage())
                .sectionId(user.getSection() != null ? user.getSection().getId().toString() : null)
                .sectionName(user.getSection() != null ? user.getSection().getName() : "Brak przypisania")
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}