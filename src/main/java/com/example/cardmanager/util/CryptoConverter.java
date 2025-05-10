package com.example.cardmanager.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Component
public class CryptoConverter {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private final SecretKeySpec key;
    private final IvParameterSpec iv;

    public CryptoConverter(
            @Value("${encryption.key}") String secretKey,
            @Value("${encryption.iv}") String ivString
    ) {
        // Проверка длин
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] ivBytes = ivString.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length != 32 || ivBytes.length != 16) {
            throw new IllegalArgumentException("Некорректные ключи шифрования. Key bytes: "
                    + keyBytes.length + ", IV bytes: " + ivBytes.length);
        }

        this.key = new SecretKeySpec(keyBytes, "AES");
        this.iv = new IvParameterSpec(ivBytes);
    }

    public String encrypt(String rawData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encrypted = cipher.doFinal(rawData.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}