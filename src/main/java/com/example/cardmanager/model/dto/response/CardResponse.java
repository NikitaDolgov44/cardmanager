package com.example.cardmanager.model.dto.response;


import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.service.CardCryptoService;
import com.example.cardmanager.util.CardNumberMasker;

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

    private static String maskCardNumber(String encryptedNumber, CardCryptoService cryptoService) {
        String decrypted = cryptoService.decrypt(encryptedNumber);
        return "**** **** **** " + decrypted.substring(decrypted.length() - 4);
    }
}
