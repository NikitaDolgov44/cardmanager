package com.example.cardmanager.service;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class CardNumberGenerator {
    private static final String BIN = "123456"; // Банковский идентификатор
    private final Random random = new Random();

    public String generate() {
        StringBuilder sb = new StringBuilder(BIN);
        // Генерация 10 случайных цифр
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
