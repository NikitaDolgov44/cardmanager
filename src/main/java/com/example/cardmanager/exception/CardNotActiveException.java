package com.example.cardmanager.exception;

public class CardNotActiveException extends RuntimeException {
    public CardNotActiveException(String message) {
        super(message);
    }
}
