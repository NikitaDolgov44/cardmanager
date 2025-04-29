package com.example.cardmanager.model.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCardRequest(
        @NotBlank String userEmail,
        @NotBlank String holderName,
        @Future LocalDate expirationDate,
        @PositiveOrZero BigDecimal initialBalance
) {}
