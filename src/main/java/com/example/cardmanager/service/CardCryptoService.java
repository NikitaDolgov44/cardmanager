package com.example.cardmanager.service;

import com.example.cardmanager.util.CryptoConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardCryptoService {
    private final CryptoConverter cryptoConverter;

    public String encrypt(String cardNumber) {
        return cryptoConverter.encrypt(cardNumber);
    }

    public String decrypt(String encryptedData) {
        return cryptoConverter.decrypt(encryptedData);
    }

    public String maskCardNumber(String encryptedNumber) {
        String decrypted = decrypt(encryptedNumber);
        return "**** **** **** " + decrypted.substring(decrypted.length() - 4);
    }
}