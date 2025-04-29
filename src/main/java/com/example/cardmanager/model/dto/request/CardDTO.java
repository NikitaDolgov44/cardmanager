package com.example.cardmanager.model.dto.request;

public class CardDTO {
    private String maskedNumber;

    public static String maskNumber(String decryptedNumber) {
        return decryptedNumber.replaceAll("\\d{12}(\\d{4})", "**** **** **** $1");
    }
}
