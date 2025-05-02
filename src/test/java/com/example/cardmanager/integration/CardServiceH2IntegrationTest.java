package com.example.cardmanager.integration;

import com.example.cardmanager.model.dto.request.CreateCardRequest;
import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.RoleType;
import com.example.cardmanager.repository.UserRepository;
import com.example.cardmanager.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CardService.class)
@ActiveProfiles("test-h2")
class CardServiceH2IntegrationTest {

    @Autowired
    private CardService cardService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createCard_shouldCreateCardWithBasicDetails() {
        // Создаем пользователя со всеми обязательными полями
        User user = userRepository.save(
                User.builder()
                        .email("test@test.com")
                        .password("encodedPass")
                        .role(RoleType.ROLE_USER)
                        .build()
        );

        CreateCardRequest request = new CreateCardRequest(
                user.getEmail(),
                "TEST USER",
                LocalDate.now().plusYears(2),
                BigDecimal.valueOf(1000)
        );

        Card card = cardService.createCard(request, user);

        assertNotNull(card.getId());
        assertEquals("TEST USER", card.getHolderName());
        assertEquals(BigDecimal.valueOf(1000), card.getBalance());
    }
}