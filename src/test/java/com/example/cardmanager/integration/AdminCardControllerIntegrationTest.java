package com.example.cardmanager.integration;

import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_shouldReturnNoContent() throws Exception {
        // Создаем тестовую карту
        Card card = cardRepository.save(testCard());

        mockMvc.perform(delete("/api/admin/cards/{id}", card.getId()))
                .andExpect(status().isNoContent());

        assertFalse(cardRepository.existsById(card.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/cards/9999"))
                .andExpect(status().isNotFound());
    }

    private Card testCard() {
        return Card.builder()
                .cardNumber("encrypted_1234567890123456")
                .holderName("TEST USER")
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.ZERO)
                .build();
    }
}
