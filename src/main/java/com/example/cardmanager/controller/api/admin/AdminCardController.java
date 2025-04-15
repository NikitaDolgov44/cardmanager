package com.example.cardmanager.controller.api.admin;

import com.example.cardmanager.model.dto.request.CreateCardRequest;
import com.example.cardmanager.model.dto.response.CardResponse;
import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.service.CardService;
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

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@RequestBody CreateCardRequest request) {
        Card card = cardService.createCard(request);
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @GetMapping
    public Page<CardResponse> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return cardService.getAllCards(page, size)
                .map(this::mapToResponse);
    }

    private CardResponse mapToResponse(Card card) {
        return CardResponse.fromEntity(card);
    }
}
