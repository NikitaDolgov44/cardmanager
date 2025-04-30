package com.example.cardmanager.model.dto.request;

public record BlockCardRequest(
        Long cardId,
        String reason
) {}
