package com.example.cardmanager.integration;

import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.model.entity.enums.RoleType;
import com.example.cardmanager.repository.CardRepository;
import com.example.cardmanager.repository.UserRepository;
import com.example.cardmanager.service.CardService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class CardServiceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldTransferFundsBetweenCards() {
        // Создание пользователя согласно текущей сущности
        User user = userRepository.save(
                User.builder()
                        .email("test@example.com")
                        .password("password")
                        .role(RoleType.ROLE_USER)
                        .build()
        );

        // Создание карт
        Card source = createTestCard(user, BigDecimal.valueOf(1000));
        Card target = createTestCard(user, BigDecimal.ZERO);

        // Перевод средств
        cardService.transferFunds(
                source.getId(),
                target.getId(),
                BigDecimal.valueOf(500),
                user.getId()
        );

        // Проверка результатов
        Card updatedSource = cardRepository.findById(source.getId()).orElseThrow();
        Card updatedTarget = cardRepository.findById(target.getId()).orElseThrow();

        assertThat(updatedSource.getBalance()).isEqualByComparingTo("500");
        assertThat(updatedTarget.getBalance()).isEqualByComparingTo("500");
    }

    private Card createTestCard(User user, BigDecimal balance) {
        return cardRepository.save(
                Card.builder()
                        .cardNumber("4111111111111111")
                        .holderName("Test Holder")
                        .expirationDate(LocalDate.now().plusYears(2))
                        .balance(balance)
                        .user(user)
                        .status(CardStatus.ACTIVE)
                        .build()
        );
    }
}