package com.example.cardmanager.model.dto.response;

import com.example.cardmanager.model.entity.Transaction;
import com.example.cardmanager.model.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        TransactionType type,
        LocalDateTime timestamp,
        Long sourceCardId,
        Long targetCardId
) {
    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTimestamp(),
                transaction.getSourceCard() != null ? transaction.getSourceCard().getId() : null,
                transaction.getTargetCard() != null ? transaction.getTargetCard().getId() : null
        );
    }
}
