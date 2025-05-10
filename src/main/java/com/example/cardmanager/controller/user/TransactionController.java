package com.example.cardmanager.controller.user;

import com.example.cardmanager.model.dto.request.TransferRequest;
import com.example.cardmanager.model.dto.request.WithdrawalRequest;
import com.example.cardmanager.model.dto.response.TransactionResponse;
import com.example.cardmanager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class TransactionController {

    private final TransactionService transactionService;

    // Перевод между своими картами
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransactionResponse> transferBetweenCards(
            @RequestBody @Valid TransferRequest request
    ) {
        return ResponseEntity.ok(
                TransactionResponse.fromEntity(
                        transactionService.transferBetweenUserCards(request)
                )
        );
    }

    // Списание средств
    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransactionResponse> withdrawFunds(
            @RequestBody @Valid WithdrawalRequest request
    ) {
        return ResponseEntity.ok(
                TransactionResponse.fromEntity(
                        transactionService.processWithdrawal(request)
                )
        );
    }
}
