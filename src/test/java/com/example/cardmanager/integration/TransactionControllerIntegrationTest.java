package com.example.cardmanager.integration;

import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CardRepository cardRepository;

    @Test
    @WithMockUser
    void transfer_shouldCompleteSuccessfully() throws Exception {
        // Arrange
        Card card1 = cardRepository.save(activeCardWithBalance(1000));
        Card card2 = cardRepository.save(activeCardWithBalance(500));

        // Act & Assert
        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferRequest(card1.getId(), card2.getId(), 100)))
                .andExpect(status().isOk());
    }

    private Card activeCardWithBalance(double balance) {
        return Card.builder()
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(balance))
                .build();
    }

    private String transferRequest(Long sourceId, Long targetId, double amount) {
        return String.format(
                "{\"sourceCardId\":%d,\"targetCardId\":%d,\"amount\":%.2f}",
                sourceId, targetId, amount
        );
    }
}
