package com.unsocial.unsocial.service;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.entity.*;
import com.unsocial.unsocial.entity.User;
import com.unsocial.unsocial.repository.UserRepository;

import com.unsocial.unsocial.security.JwtServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServices jwtService;
    private final AuthenticationManager authenticationManager;

    // ──────────────────────────────────────────────
    // Register
    // ──────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        // Check for duplicate phone
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("An account with this phone number already exists");
        }

        // Build and save the user
        User user = User.builder()
                .name(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.USER)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        // Generate JWT
        UserDetails userDetails = buildUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.of(
                token,
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                "Registration successful! Welcome to UnSocial."
        );
    }

    // ──────────────────────────────────────────────
    // Login
    // ──────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtService.generateToken(buildUserDetails(user));
        log.info("Login successful for: {}", user.getEmail());

        return AuthResponse.of(
                token,
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                "Login successful"
        );
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
