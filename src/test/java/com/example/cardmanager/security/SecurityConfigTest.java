package com.example.cardmanager.security;

import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.model.entity.enums.RoleType;
import com.example.cardmanager.repository.CardRepository;
import com.example.cardmanager.repository.UserRepository;
import com.example.cardmanager.service.CardCryptoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class UserCardIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true);

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CardCryptoService cardCryptoService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("encryption.key", () -> "12345678901234567890123456789012"); // 32 chars
        registry.add("encryption.iv", () -> "1234567890123456"); // 16 chars
    }

    @BeforeEach
    void setUp() {
        // Настройка моков для шифрования
        when(cardCryptoService.encrypt(anyString())).thenAnswer(inv -> "encrypted_" + inv.getArgument(0));
        when(cardCryptoService.decrypt(anyString())).thenAnswer(inv -> {
            String arg = inv.getArgument(0);
            return arg.startsWith("encrypted_") ? arg.substring(10) : arg;
        });
        when(cardCryptoService.maskCardNumber(anyString())).thenReturn("**** **** **** 1111");

        // Создаем тестовых пользователей
        User admin = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("adminpass"))
                .role(RoleType.ROLE_ADMIN)
                .build();

        User user = User.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("userpass"))
                .role(RoleType.ROLE_USER)
                .build();

        userRepository.saveAll(List.of(admin, user));

        // Создаем тестовые карты с зашифрованным номером
        Card userCard = Card.builder()
                .cardNumber("encrypted_4111111111111111") // Шифрованный номер
                .holderName("TEST USER")
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000.00))
                .user(user)
                .build();

        cardRepository.save(userCard);
    }

    @AfterEach
    void tearDown() {
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldDenyUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserShouldGetOwnCards() throws Exception {
        mockMvc.perform(get("/api/user/cards")
                        .with(user("user@test.com").password("userpass").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 1111"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldAllowAdminToManageCards() throws Exception {
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 1111"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void shouldDenyUserAccessToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldGet401() throws Exception {
        mockMvc.perform(get("/api/user/cards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminShouldAccessAllCards() throws Exception {
        mockMvc.perform(get("/api/admin/cards")
                        .with(user("admin@test.com").password("adminpass").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 1111"));
    }
}