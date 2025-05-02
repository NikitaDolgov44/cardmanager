package com.example.cardmanager.model.dto.request;

import com.example.cardmanager.model.entity.enums.CardStatus;

public record UpdateCardRequest(CardStatus status) {}