package com.example.ays.service;

import com.example.ays.entity.AppUser;
import com.example.ays.enums.UserRole;
import com.example.ays.exception.ResourceNotFoundException;
import com.example.ays.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final AuthTokenRepository authTokenRepository;

    @Transactional(readOnly = true)
    public AppUser fromAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResourceNotFoundException("Login required");
        }

        String token = authorization.substring("Bearer ".length());
        return authTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid session"))
                .getUser();
    }

    public boolean isAdmin(AppUser user) {
        return user.getRole() == UserRole.ADMIN;
    }

    public void requireAdmin(AppUser user) {
        if (!isAdmin(user)) {
            throw new com.example.ays.exception.ResourceNotFoundException("Access Denied: Admin role required");
        }
    }
}
