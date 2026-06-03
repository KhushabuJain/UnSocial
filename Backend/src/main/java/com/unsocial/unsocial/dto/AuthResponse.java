package com.unsocial.unsocial.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private String email;
    private String fullName;
    private String role;
    private String message;

    public static AuthResponse of(String token, String email, String fullName, String role, String message) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(email)
                .fullName(fullName)
                .role(role)
                .message(message)
                .build();
    }
}
