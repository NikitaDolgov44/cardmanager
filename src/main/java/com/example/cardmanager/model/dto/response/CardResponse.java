package com.example.cardmanager.model.dto.response;


import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.util.CardNumberMasker;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
        String maskedNumber,  // Используем маскированный номер
        String holderName,
        LocalDate expirationDate,
        BigDecimal balance,
        CardStatus status
) {
    public static CardResponse fromEntity(Card card) {
        return new CardResponse(
                maskCardNumber(card.getCardNumber()),
                card.getHolderName(),
                card.getExpirationDate(),
                card.getBalance(),
                card.getStatus()
        );
    }
    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) return "****";
        return "****-****-****-" + cardNumber.substring(12);
    }
}
