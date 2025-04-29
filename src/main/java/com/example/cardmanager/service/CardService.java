package com.example.cardmanager.service;

import com.example.cardmanager.exception.CardNotFoundException;
import com.example.cardmanager.exception.UserNotFoundException;
import com.example.cardmanager.model.dto.request.CreateCardRequest;
import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.enums.CardStatus;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.repository.CardRepository;
import com.example.cardmanager.repository.UserRepository;
import com.example.cardmanager.util.CryptoConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final CryptoConverter cryptoConverter;
    private final CardCryptoService cardCryptoService; // Сервис для шифрования

    @Transactional
    public Card createCard(CreateCardRequest request, User user) {
        String rawNumber = cardNumberGenerator.generate();
        String encryptedNumber = cardCryptoService.encrypt(rawNumber);
        return cardRepository.save(
                Card.builder()
                        .cardNumber(encryptedNumber)
                        .holderName(request.holderName())
                        .expirationDate(request.expirationDate())
                        .status(CardStatus.ACTIVE)
                        .balance(request.initialBalance())
                        .user(user)
                        .build()
        );
    }

    public Page<Card> getAllCards(int page, int size) {
        return cardRepository.findAll(PageRequest.of(page, size));
    }

    public Page<Card> getUserCards(Pageable pageable, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return cardRepository.findByUserId(user.getId(), pageable);
    }

    @Transactional
    public void requestCardBlock(Long cardId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Card card = cardRepository.findByIdAndUserId(cardId, user.getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found or access denied"));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Card is not active");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }
}