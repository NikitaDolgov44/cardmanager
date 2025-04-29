package com.example.cardmanager.repository;

import com.example.cardmanager.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Pageable;
@Component
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE (t.sourceCard.id = :cardId OR t.targetCard.id = :cardId) AND t.sourceCard.user.id = :userId")
    Page<Transaction> findByCardAndUser(
            @Param("cardId") Long cardId,
            @Param("userId") Long userId,
            Pageable pageable // Используйте Spring Pageable
    );
}
