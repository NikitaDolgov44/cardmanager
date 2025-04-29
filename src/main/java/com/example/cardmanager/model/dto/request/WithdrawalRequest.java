package com.example.cardmanager.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record WithdrawalRequest(
        @NotNull Long cardId,
        @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
        String description
) {}
