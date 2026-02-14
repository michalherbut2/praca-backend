package pl.most.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.most.backend.model.dto.LoginRequest;
import pl.most.backend.model.dto.LoginResponse;
import pl.most.backend.model.dto.RegisterRequest;
import pl.most.backend.model.dto.UserDto;
import pl.most.backend.model.entity.User;
import pl.most.backend.features.user.repository.UserRepository;
import pl.most.backend.security.JwtTokenProvider;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email już zarejestrowany");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowy email lub hasło"));

        if (!user.getIsActive()) {
            throw new SecurityException("Twoje konto zostało zablokowane. Skontaktuj się z administratorem.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Nieprawidłowy email lub hasło");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);

        UserDto userDto = UserDto.fromEntity(user);

        return LoginResponse.builder()
                .token(token)
                .user(userDto)
                .build();
    }
}