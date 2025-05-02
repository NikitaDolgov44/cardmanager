package com.example.cardmanager.service;

import com.example.cardmanager.exception.*;
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
        // 1. Получаем карты
        Card sourceCard = getActiveCardOrThrow(request.sourceCardId(), "Source");
        Card targetCard = getActiveCardOrThrow(request.targetCardId(), "Target");

        // 2. Проверяем принадлежность
        if (!sourceCard.getUser().getId().equals(targetCard.getUser().getId())) {
            throw new SecurityException("Cards belong to different users");
        }

        // 3. Проверяем баланс
        if (sourceCard.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // 4. Проверяем лимиты
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
        Card card = getActiveCardOrThrow(request.cardId(), "");

        // 2. Проверяем баланс
        if (card.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // 3. Проверяем лимиты
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
    private Card getActiveCardOrThrow(Long cardId, String cardType) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(
                        (cardType.isEmpty() ? "Card" : cardType + " card") + " not found"));

        switch (card.getStatus()) {
            case BLOCKED:
                throw new CardBlockedException(
                        (cardType.isEmpty() ? "Card" : cardType + " card") + " is blocked");
            case EXPIRED:
                throw new CardExpiredException(
                        (cardType.isEmpty() ? "Card" : cardType + " card") + " is expired");
            case ACTIVE:
                return card;
            default:
                throw new IllegalStateException("Unknown card status: " + card.getStatus());
        }
    }
}
