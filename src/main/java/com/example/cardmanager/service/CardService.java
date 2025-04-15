package com.example.cardmanager.service;

import com.example.cardmanager.exception.CardNotFoundException;
import com.example.cardmanager.exception.InsufficientFundsException;
import com.example.cardmanager.exception.UserNotFoundException;
import com.example.cardmanager.model.dto.request.CreateCardRequest;
import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.repository.CardRepository;
import com.example.cardmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    @Transactional
    public Card createCard(CreateCardRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.userId()));

        return cardRepository.save(
                Card.builder()
                        .cardNumber(request.cardNumber())
                        .holderName(request.holderName())
                        .expirationDate(request.expirationDate())
                        .status(CardStatus.ACTIVE)
                        .balance(BigDecimal.ZERO)
                        .user(user)
                        .build()
        );
    }
    @Transactional
    public void transferFunds(Long sourceCardId, Long targetCardId, BigDecimal amount, Long userId) {
        Card source = cardRepository.findByIdAndUserId(sourceCardId, userId)
                .orElseThrow(() -> new CardNotFoundException("Source card not found"));

        Card target = cardRepository.findByIdAndUserId(targetCardId, userId)
                .orElseThrow(() -> new CardNotFoundException("Target card not found"));

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        cardRepository.saveAll(List.of(source, target));

        // Логирование транзакции (необходимо реализовать TransactionService)
        transactionService.recordTransaction(source, target, amount);
    }

    public Page<Card> getAllCards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findAll(pageable);
    }
}