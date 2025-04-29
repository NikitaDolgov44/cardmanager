package com.example.cardmanager.service;

import com.example.cardmanager.model.entity.Card;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class LimitService {
    public void checkTransferLimits(Card card, BigDecimal amount) {
        // Заглушка для будущей реализации
    }

    public void checkWithdrawalLimits(Card card, BigDecimal amount) {
        // Заглушка для будущей реализации
    }
}
