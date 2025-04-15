package com.example.cardmanager.model.dto.request;

import java.time.LocalDate;

public record CreateCardRequest(
        String cardNumber,
        String holderName,
        LocalDate expirationDate,
        Long userId
) {}