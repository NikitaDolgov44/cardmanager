package com.example.cardmanager.security;

import com.example.cardmanager.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void shouldLoadUserByUsername() {
        // Arrange
        UserDetailsService service = new TestSecurityConfig().testUserDetailsService();

        // Act
        UserDetails user = service.loadUserByUsername("user@test.com");
        UserDetails admin = service.loadUserByUsername("admin@test.com");

        // Assert
        assertNotNull(user);
        assertEquals("user@test.com", user.getUsername());
        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        assertNotNull(admin);
        assertEquals("admin@test.com", admin.getUsername());
        assertTrue(admin.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        // Arrange
        UserDetailsService service = new TestSecurityConfig().testUserDetailsService();

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername("unknown@test.com");
        });
    }
}