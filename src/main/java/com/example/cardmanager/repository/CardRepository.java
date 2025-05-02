package com.example.cardmanager.repository;

import com.example.cardmanager.model.entity.Card;
import com.example.cardmanager.model.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByUserId(Long userId, Pageable pageable);

    Optional<Card> findByIdAndUserId(Long cardId, Long userId);

    @Query("SELECT c FROM Card c WHERE c.expirationDate < :date AND c.status <> :status")
    List<Card> findAllByExpirationDateBeforeAndStatusNot(
            @Param("date") LocalDate date,
            @Param("status") CardStatus status
    );
}
