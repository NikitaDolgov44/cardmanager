package com.example.cardmanager.service;

import com.example.cardmanager.exception.UserAlreadyExistsException;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.RoleType;
import com.example.cardmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(String email, String rawPassword, RoleType role) {
        // Проверка уникальности email
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        // Создание и сохранение пользователя
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User updateUserRole(Long userId, RoleType newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRole(newRole);
        return userRepository.save(user);
    }
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
