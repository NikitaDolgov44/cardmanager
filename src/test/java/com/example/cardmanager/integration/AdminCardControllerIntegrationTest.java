package com.example.cardmanager.integration;

import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.model.entity.enums.RoleType;
import com.example.cardmanager.repository.CardRepository;
import com.example.cardmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AdminCardControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> "false");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCardShouldReturnNoContent() throws Exception {
        // Arrange
        User user = userRepository.save(
                User.builder()
                        .email("admin@test.com")
                        .password("encodedPass")
                        .role(RoleType.ADMIN)
                        .build()
        );

        Card card = cardRepository.save(
                Card.builder()
                        .cardNumber("encrypted_1234567890123456")
                        .holderName("TEST USER")
                        .expirationDate(LocalDate.now().plusYears(1))
                        .status(CardStatus.BLOCKED)
                        .balance(BigDecimal.ZERO)
                        .user(user)
                        .build()
        );

        // Act & Assert
        mockMvc.perform(delete("/api/admin/cards/{id}", card.getId()))
                .andExpect(status().isNoContent());

        assertFalse(cardRepository.existsById(card.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCardShouldReturnNotFound() throws Exception {
        // Используем заведомо несуществующий ID
        long nonExistentId = 9999L;

        // Проверяем что карты действительно нет
        assertFalse(cardRepository.existsById(nonExistentId));

        // Тест должен ожидать 404, так как карта не найдена
        mockMvc.perform(delete("/api/admin/cards/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
}
