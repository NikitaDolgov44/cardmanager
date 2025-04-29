package com.example.cardmanager.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransferRequest(
        @NotNull Long sourceCardId,
        @NotNull Long targetCardId,
        @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount
) {}