package com.unsocial.unsocial.util;

import com.unsocial.unsocial.entity.User;
import com.unsocial.unsocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Returns the full User entity of the currently authenticated user.
     * Extracts the email from the JWT via SecurityContextHolder.
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Authenticated user not found: " + email
                ));
    }

    /**
     * Convenience method — returns only the user ID.
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
