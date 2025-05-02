package com.example.cardmanager.service;

import com.example.cardmanager.exception.CardNotFoundException;
import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void deleteCard_shouldDeleteWhenExistsAndBlocked() {
        // Arrange
        Long cardId = 1L;
        Card blockedCard = Card.builder().id(cardId).status(CardStatus.BLOCKED).build();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(blockedCard));

        // Act
        cardService.deleteCard(cardId);

        // Assert
        verify(cardRepository).delete(blockedCard);
    }

    @Test
    void deleteCard_shouldThrowWhenNotFound() {
        // Arrange
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(cardId));
    }

    @Test
    void deleteCard_shouldThrowWhenCardActive() {
        // Arrange
        Long cardId = 1L;
        Card activeCard = Card.builder().id(cardId).status(CardStatus.ACTIVE).build();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(activeCard));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> cardService.deleteCard(cardId));
        verify(cardRepository, never()).delete(any()); // Убедимся, что delete не вызывался
    }
}