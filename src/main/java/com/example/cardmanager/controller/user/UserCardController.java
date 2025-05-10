package com.example.cardmanager.controller.user;

import com.example.cardmanager.model.dto.response.CardResponse;
import com.example.cardmanager.model.dto.response.TransactionResponse;
import com.example.cardmanager.service.CardCryptoService;
import com.example.cardmanager.service.CardService;
import com.example.cardmanager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/user/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserCardController {
    private final CardService cardService;
    private final TransactionService transactionService;
    private final CardCryptoService cardCryptoService;

    @GetMapping("/{cardId}/transactions")
    public Page<TransactionResponse> getCardTransactions(
            @PathVariable Long cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        return transactionService.getCardTransactions(
                cardId,
                principal.getName(),
                PageRequest.of(page, size, Sort.unsorted())
        ).map(TransactionResponse::fromEntity);
    }

    @GetMapping
    public Page<CardResponse> getUserCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        return cardService.getUserCards(
                PageRequest.of(page, size),
                principal.getName()
        ).map(card -> CardResponse.fromEntity(card, cardCryptoService));
    }

    @PutMapping("/{cardId}/block")
    public ResponseEntity<Void> requestBlockCard(
            @PathVariable Long cardId,
            Principal principal
    ) {
        cardService.requestCardBlock(cardId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}