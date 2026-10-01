package com.example.ays.dto;

import com.example.ays.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private UserRole role;
}
