package com.example.cardmanager.config;

import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.RoleType;
import com.example.cardmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializerConfig {

    @Bean
    public CommandLineRunner initAdminUser(
            UserRepository userRepository,
            PasswordEncoder encoder
    ) {
        return args -> {
            // Проверка чтобы пользователь не создавался при каждом запуске
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                userRepository.save(User.builder()
                        .email("admin@example.com")
                        .password(encoder.encode("admin123"))
                        .role(RoleType.ROLE_ADMIN)
                        .build());
            }
        };
    }
}
