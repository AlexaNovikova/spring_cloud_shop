package ru.otus.payment.service.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "account", schema = "payment")
public class Account {
    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(precision = 10, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;
    @CreationTimestamp
    private OffsetDateTime createdAt = OffsetDateTime.now();
}