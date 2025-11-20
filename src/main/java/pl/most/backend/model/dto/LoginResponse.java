package pl.most.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Integer points;
}