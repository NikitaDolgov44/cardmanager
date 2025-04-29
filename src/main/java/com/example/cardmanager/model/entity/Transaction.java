package com.example.cardmanager.model.entity;

import com.example.cardmanager.model.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @ManyToOne
    @JoinColumn(name = "source_card_id")
    private Card sourceCard;

    @ManyToOne
    @JoinColumn(name = "target_card_id")
    private Card targetCard;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp // Добавляем автоматическое заполнение
    private LocalDateTime timestamp;

    @PrePersist // Дополнительная страховка
    protected void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}