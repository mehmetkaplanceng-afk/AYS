package com.example.ays.service;

import com.example.ays.dto.AuthResponse;
import com.example.ays.dto.LoginRequest;
import com.example.ays.dto.RegisterRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.AuthToken;
import com.example.ays.enums.UserRole;
import com.example.ays.exception.ResourceNotFoundException;
import com.example.ays.repository.AppUserRepository;
import com.example.ays.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        UserRole role = appUserRepository.count() == 0 ? UserRole.ADMIN : UserRole.USER;
        AppUser user = appUserRepository.save(AppUser.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build());

        return createToken(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResourceNotFoundException("Invalid email or password");
        }

        return createToken(user);
    }

    private AuthResponse createToken(AppUser user) {
        AuthToken token = authTokenRepository.save(AuthToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .createdAt(LocalDateTime.now())
                .build());

        return AuthResponse.builder()
                .token(token.getToken())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
