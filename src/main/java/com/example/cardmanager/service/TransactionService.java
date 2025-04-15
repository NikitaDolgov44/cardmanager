package com.example.cardmanager.service;

import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.Transaction;
import com.example.cardmanager.model.entity.enums.TransactionType;
import com.example.cardmanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public void recordTransaction(Card source, Card target, BigDecimal amount) {
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .sourceCard(source)
                .targetCard(target)
                .type(TransactionType.TRANSFER)
                .build();

        transactionRepository.save(transaction);
    }
}
