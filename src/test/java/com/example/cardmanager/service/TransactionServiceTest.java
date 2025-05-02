package com.example.cardmanager.service;

import com.example.cardmanager.exception.*;
import com.example.cardmanager.model.dto.request.TransferRequest;
import com.example.cardmanager.model.dto.request.WithdrawalRequest;
import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.repository.CardRepository;
import com.example.cardmanager.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void transfer_shouldFailWhenSourceCardBlocked() {
        // Упрощенная подготовка: только необходимые поля
        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(
                        Card.builder().status(CardStatus.BLOCKED).build()
                ));

        assertThrows(CardBlockedException.class, () ->
                transactionService.transferBetweenUserCards(
                        new TransferRequest(1L, 2L, BigDecimal.TEN)
                )
        );
    }

    @Test
    void transfer_shouldFailWhenTargetCardExpired() {
        // Настраиваем моки для обеих карт
        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(
                        Card.builder().status(CardStatus.ACTIVE).build()
                ));
        when(cardRepository.findById(2L))
                .thenReturn(Optional.of(
                        Card.builder().status(CardStatus.EXPIRED).build()
                ));

        assertThrows(CardExpiredException.class, () ->
                transactionService.transferBetweenUserCards(
                        new TransferRequest(1L, 2L, BigDecimal.TEN)
                ));
    }

    @Test
    void withdrawal_shouldFailWhenCardExpired() {
        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(
                        Card.builder().status(CardStatus.EXPIRED).build()
                ));

        assertThrows(CardExpiredException.class, () ->
                transactionService.processWithdrawal(
                        new WithdrawalRequest(1L, BigDecimal.ONE, "Test")
                ));
    }
}
