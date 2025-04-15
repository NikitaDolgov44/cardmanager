package com.example.cardmanager.util;

public class CardNumberMasker {
    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 12) return cardNumber;
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
}