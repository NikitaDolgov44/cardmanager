package com.example.cardmanager.service;

import com.example.cardmanager.exception.UserAlreadyExistsException;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.RoleType;
import com.example.cardmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_shouldCreateNewUser() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.registerUser(email, password, RoleType.ROLE_USER);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(RoleType.ROLE_USER, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowWhenEmailExists() {
        // Arrange
        String email = "exists@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () ->
                userService.registerUser(email, "password", RoleType.ROLE_USER)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserByEmail_shouldReturnUser() {
        // Arrange
        String email = "user@example.com";
        User expectedUser = User.builder()
                .email(email)
                .password("encodedPass")
                .role(RoleType.ROLE_USER)
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));

        // Act
        User result = userService.getUserByEmail(email);

        // Assert
        assertEquals(expectedUser, result);
    }

    @Test
    void updateUserRole_shouldChangeRole() {
        // Arrange
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("user@example.com")
                .role(RoleType.ROLE_USER)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateUserRole(userId, RoleType.ROLE_ADMIN);

        // Assert
        assertEquals(RoleType.ROLE_ADMIN, result.getRole());
        verify(userRepository).save(user);
    }
}
