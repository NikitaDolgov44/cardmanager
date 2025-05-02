package com.example.cardmanager.controller.api;

import com.example.cardmanager.model.dto.request.CreateCardRequest;
import com.example.cardmanager.model.dto.request.UpdateCardRequest;
import com.example.cardmanager.model.dto.response.CardResponse;
import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.service.CardCryptoService;
import com.example.cardmanager.service.CardService;
import com.example.cardmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {
    private final CardService cardService;
    private final UserService userService;
    private final CardCryptoService cardCryptoService;

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@RequestBody CreateCardRequest request) {
        User user = userService.getUserByEmail(request.userEmail());
        Card card = cardService.createCard(request, user);
        return ResponseEntity.ok(CardResponse.fromEntity(card, cardCryptoService));
    }

    @GetMapping
    public Page<CardResponse> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return cardService.getAllCards(page, size)
                .map(card -> CardResponse.fromEntity(card, cardCryptoService));
    }

    @PatchMapping("/{cardId}/status")
    public ResponseEntity<CardResponse> updateCardStatus(
            @PathVariable Long cardId,
            @RequestBody UpdateCardRequest request
    ) {
        Card card = cardService.updateCardStatus(cardId, request.status());
        return ResponseEntity.ok(CardResponse.fromEntity(card, cardCryptoService));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}