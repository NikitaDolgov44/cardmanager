package com.example.cardmanager.service;

import com.example.cardmanager.exception.CardNotFoundException;
import com.example.cardmanager.exception.InsufficientFundsException;
import com.example.cardmanager.exception.UserNotFoundException;
import com.example.cardmanager.model.dto.request.TransferRequest;
import com.example.cardmanager.model.dto.request.WithdrawalRequest;
import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.Transaction;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.TransactionType;
import com.example.cardmanager.repository.CardRepository;
import com.example.cardmanager.repository.TransactionRepository;
import com.example.cardmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Component
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final LimitService limitService; // Если реализовано

    @Transactional
    public Page<Transaction> getCardTransactions(Long cardId, String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return transactionRepository.findByCardAndUser(cardId, user.getId(), pageable);
    }

    @Transactional
    public Transaction transferBetweenUserCards(TransferRequest request) {
        // 1. Получаем исходную и целевую карты
        Card sourceCard = cardRepository.findById(request.sourceCardId())
                .orElseThrow(() -> new CardNotFoundException("Source card not found"));

        Card targetCard = cardRepository.findById(request.targetCardId())
                .orElseThrow(() -> new CardNotFoundException("Target card not found"));

        // 2. Проверяем принадлежность карт одному пользователю
        if (!sourceCard.getUser().getId().equals(targetCard.getUser().getId())) {
            throw new SecurityException("Cards belong to different users");
        }

        // 3. Проверяем достаточно ли средств
        if (sourceCard.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // 4. Проверяем лимиты (реализуйте при необходимости)
        limitService.checkTransferLimits(sourceCard, request.amount());

        // 5. Обновляем балансы
        sourceCard.setBalance(sourceCard.getBalance().subtract(request.amount()));
        targetCard.setBalance(targetCard.getBalance().add(request.amount()));

        cardRepository.saveAll(List.of(sourceCard, targetCard));

        // 6. Сохраняем транзакцию
        return transactionRepository.save(
                Transaction.builder()
                        .amount(request.amount())
                        .type(TransactionType.TRANSFER)
                        .sourceCard(sourceCard)
                        .targetCard(targetCard)
                        .build()
        );
    }

    @Transactional
    public Transaction processWithdrawal(WithdrawalRequest request) {
        // 1. Получаем карту
        Card card = cardRepository.findById(request.cardId())
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        // 2. Проверяем баланс
        if (card.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // 3. Проверяем лимиты (реализуйте при необходимости)
        limitService.checkWithdrawalLimits(card, request.amount());

        // 4. Обновляем баланс
        card.setBalance(card.getBalance().subtract(request.amount()));
        cardRepository.save(card);

        // 5. Сохраняем транзакцию
        return transactionRepository.save(
                Transaction.builder()
                        .amount(request.amount())
                        .type(TransactionType.WITHDRAWAL)
                        .sourceCard(card)
                        .build()
        );
    }

    public void deleteAllTransactions() {
        transactionRepository.deleteAll();
    }
}
