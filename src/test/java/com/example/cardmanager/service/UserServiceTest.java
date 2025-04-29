package com.example.cardmanager.service;

import com.example.cardmanager.CardmanagerApplication;
import com.example.cardmanager.exception.UserAlreadyExistsException;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.RoleType;
import com.example.cardmanager.repository.CardRepository;
import com.example.cardmanager.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@SpringBootTest(
        classes = CardmanagerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class UserServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CardRepository cardRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        cardRepository.deleteAll();
    }

    @Test
    void shouldRegisterNewUser() {
        // Регистрация нового пользователя
        User newUser = userService.registerUser(
                "user@test.com",
                "password123",
                RoleType.ROLE_USER
        );

        // Проверки
        assertNotNull(newUser.getId());
        assertEquals("user@test.com", newUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", newUser.getPassword()));
        assertEquals(RoleType.ROLE_USER, newUser.getRole());
    }

    @Test
    void shouldFailWhenRegisterDuplicateEmail() {
        // Первая регистрация
        userService.registerUser("duplicate@test.com", "password", RoleType.ROLE_USER);

        // Попытка повторной регистрации
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser("duplicate@test.com", "password", RoleType.ROLE_USER);
        });
    }

    @Test
    void shouldFindUserByEmail() {
        // Подготовка данных
        userService.registerUser("find@test.com", "password", RoleType.ROLE_ADMIN);

        // Поиск пользователя
        User foundUser = userService.getUserByEmail("find@test.com");

        // Проверки
        assertEquals("find@test.com", foundUser.getEmail());
        assertEquals(RoleType.ROLE_ADMIN, foundUser.getRole());
    }

    @Test
    void shouldUpdateUserRole() {
        // Создание пользователя
        User user = userService.registerUser("update@test.com", "password", RoleType.ROLE_USER);

        // Обновление роли
        User updatedUser = userService.updateUserRole(user.getId(), RoleType.ROLE_ADMIN);

        // Проверки
        assertEquals(RoleType.ROLE_ADMIN, updatedUser.getRole());
    }
}
