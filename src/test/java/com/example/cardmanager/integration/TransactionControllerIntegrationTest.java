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
import org.springframework.http.MediaType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class TransactionControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
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
    @WithMockUser(username = "user@test.com", roles = "USER")
    void shouldTransferFundsSuccessfullyBetweenUserCards() throws Exception {
        // Arrange
        User user = userRepository.save(
                User.builder()
                        .email("user@test.com")
                        .password("encodedPass")
                        .role(RoleType.USER)
                        .build()
        );

        Card sourceCard = cardRepository.save(
                Card.builder()
                        .cardNumber("encrypted_1111222233334444")
                        .holderName("TEST USER 1")
                        .expirationDate(LocalDate.now().plusYears(1))
                        .status(CardStatus.ACTIVE)
                        .balance(BigDecimal.valueOf(1000))
                        .user(user)
                        .build()
        );

        Card targetCard = cardRepository.save(
                Card.builder()
                        .cardNumber("encrypted_5555666677778888")
                        .holderName("TEST USER 2")
                        .expirationDate(LocalDate.now().plusYears(1))
                        .status(CardStatus.ACTIVE)
                        .balance(BigDecimal.valueOf(500))
                        .user(user)
                        .build()
        );

        String transferJson = """
            {
                "sourceCardId": %d,
                "targetCardId": %d,
                "amount": 100.00
            }
            """.formatted(sourceCard.getId(), targetCard.getId());

        // Act
        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson))
                .andExpect(status().isOk());

        // Assert
        Card updatedSource = cardRepository.findById(sourceCard.getId()).orElseThrow();
        Card updatedTarget = cardRepository.findById(targetCard.getId()).orElseThrow();

        assertEquals(0, BigDecimal.valueOf(900).compareTo(updatedSource.getBalance()));
        assertEquals(0, BigDecimal.valueOf(600).compareTo(updatedTarget.getBalance()));
    }
}
