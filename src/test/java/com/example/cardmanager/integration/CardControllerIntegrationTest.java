package com.example.cardmanager.integration;

import com.example.cardmanager.config.SecurityConfig;
import com.example.cardmanager.model.dto.request.CreateCardRequest;
import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.service.CardCryptoService;
import com.example.cardmanager.service.CardService;
import com.example.cardmanager.service.TransactionService;
import com.example.cardmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class CardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CardCryptoService cardCryptoService;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCardSuccessfullyWhenAdmin() throws Exception {
        // Arrange
        User user = User.builder()
                .email("user@test.com")
                .build();

        Card card = Card.builder()
                .id(1L)
                .holderName("Test User")
                .status(CardStatus.ACTIVE)
                .build();

        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(cardService.createCard(any(), any())).thenReturn(card);
        when(cardCryptoService.maskCardNumber(any())).thenReturn("**** **** **** 1234");

        CreateCardRequest request = new CreateCardRequest(
                "user@test.com",
                "Test User",
                LocalDate.now().plusYears(2),
                BigDecimal.valueOf(1000)
        );

        // Act & Assert
        mockMvc.perform(post("/api/admin/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.holderName").value("Test User"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUserAccessesAdminEndpoint() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/admin/cards")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllCardsSuccessfullyWhenAdmin() throws Exception {
        // Arrange
        Card card = Card.builder().id(1L).build();
        Page<Card> page = new PageImpl<>(List.of(card));

        when(cardService.getAllCards(0, 10)).thenReturn(page);
        when(cardCryptoService.maskCardNumber(any())).thenReturn("**** **** **** 1234");

        // Act & Assert
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }
}
