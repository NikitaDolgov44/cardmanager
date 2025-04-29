package com.example.cardmanager.integration;

import com.example.cardmanager.model.dto.request.CreateCardRequest;
import com.example.cardmanager.model.dto.request.TransferRequest;
import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.model.entity.enums.RoleType;
import com.example.cardmanager.repository.CardRepository;
import com.example.cardmanager.repository.UserRepository;
import com.example.cardmanager.service.CardCryptoService;
import com.example.cardmanager.service.CardNumberGenerator;
import com.example.cardmanager.service.CardService;
import com.example.cardmanager.service.TransactionService;
import com.example.cardmanager.util.CryptoConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true);

    @Autowired private CardService cardService;
    @Autowired private CardRepository cardRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TransactionService transactionService;
    @Autowired private CryptoConverter cryptoConverter;
    @Autowired private CardNumberGenerator cardNumberGenerator;
    @Autowired private CardCryptoService cardCryptoService;

    @AfterEach
    void tearDown() {
        transactionService.deleteAllTransactions();
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateCardWithEncryptedNumber() {
        // 1. Создаем пользователя
        User user = userRepository.save(createTestUser());

        // 2. Создаем карту через сервис
        CreateCardRequest request = new CreateCardRequest(
                user.getEmail(),
                "Test User",
                LocalDate.now().plusYears(2),
                BigDecimal.valueOf(1000)
        );
        Card card = cardService.createCard(request, user);

        // 3. Получаем зашифрованный номер из БД
        Card dbCard = cardRepository.findById(card.getId()).orElseThrow();
        String encryptedNumber = dbCard.getCardNumber();

        // 4. Дешифруем номер
        String decryptedNumber = cardCryptoService.decrypt(encryptedNumber);

        // 5. Проверяем валидность номера
        assertThat(decryptedNumber)
                .hasSize(16) // Ожидаемая длина номера карты
                .matches("\\d{16}"); // Проверка формата (16 цифр)
    }

    @Test
    void shouldTransferBetweenCards() {
        // 1. Setup
        User user = userRepository.save(createTestUser());
        Card sourceCard = createTestCard(user, BigDecimal.valueOf(1000));
        Card targetCard = createTestCard(user, BigDecimal.ZERO);

        // 2. Execute transfer
        transactionService.transferBetweenUserCards(
                new TransferRequest(
                        sourceCard.getId(),
                        targetCard.getId(),
                        BigDecimal.valueOf(500)
                )
        );

        // 3. Verify balances
        Card updatedSource = cardRepository.findById(sourceCard.getId()).orElseThrow();
        Card updatedTarget = cardRepository.findById(targetCard.getId()).orElseThrow();

        assertThat(updatedSource.getBalance()).isEqualByComparingTo("500.00");
        assertThat(updatedTarget.getBalance()).isEqualByComparingTo("500.00");
    }

    private User createTestUser() {
        return User.builder()
                .email("user@test.com")
                .password("encodedPass")
                .role(RoleType.ROLE_USER)
                .build();
    }

    private Card createTestCard(User user, BigDecimal balance) {
        return cardService.createCard(
                new CreateCardRequest(
                        user.getEmail(),
                        "Test User",
                        LocalDate.now().plusYears(2),
                        balance
                ),
                user
        );
    }
    @Test
    void cryptoConverterShouldWork() {
        String original = "1234564402968176";
        String encrypted = cryptoConverter.encrypt(original);
        assertThat(encrypted).hasSize(44);
        assertThat(cryptoConverter.decrypt(encrypted)).isEqualTo(original);
    }
    @Test
    void cryptoConverterValidatesAlgorithm() {
        String original = "1234567890123456";
        String encrypted = cryptoConverter.encrypt(original);

        // Проверяем, что зашифрованные данные в Base64
        assertThat(Base64.getDecoder().decode(encrypted)).hasSize(32); // AES-256/CBC
    }
    @Test
    @Transactional
    void databaseStoresEncryptedValue() {
        // 1. Создаем и сохраняем пользователя
        User user = userRepository.save(
                User.builder()
                        .email("test@example.com")
                        .password("encodedPass")
                        .role(RoleType.ROLE_USER)
                        .build()
        );

        // 2. Создаем карту со всеми обязательными полями
        Card card = cardRepository.save(
                Card.builder()
                        .cardNumber(cryptoConverter.encrypt("1234567890123456"))
                        .holderName("Card Holder")
                        .expirationDate(LocalDate.now().plusYears(2))
                        .status(CardStatus.ACTIVE)
                        .balance(BigDecimal.valueOf(1000.00))
                        .user(user)
                        .build()
        );

        // 3. Проверяем сохранение в БД
        Card dbCard = cardRepository.findById(card.getId()).orElseThrow();

        assertThat(dbCard.getCardNumber())
                .isNotEqualTo("1234567890123456")
                .hasSize(44);
    }
}