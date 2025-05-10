package com.example.cardmanager.model.dto.response;


import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.service.CardCryptoService;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
        Long id,
        String maskedNumber,
        String holderName,
        LocalDate expirationDate,
        CardStatus status,
        BigDecimal balance
) {
    public static CardResponse fromEntity(Card card, CardCryptoService cryptoService) {
        return new CardResponse(
                card.getId(),
                cryptoService.maskCardNumber(card.getCardNumber()),
                card.getHolderName(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()
        );
    }
}
