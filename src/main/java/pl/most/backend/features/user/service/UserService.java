package pl.most.backend.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.most.backend.features.user.dto.UserSummaryDto;
import pl.most.backend.features.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserSummaryDto> getAllUsersLite() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummaryDto(
                        u.getId(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getEmail(),
                        u.getPoints(),
                        u.getProfileImage()
                ))
                .toList();
    }
}
