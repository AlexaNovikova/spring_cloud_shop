package ru.otus.lib.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessModel {
    private Integer orderId;
    private UUID userId;
    private BigDecimal amount;

    public static PaymentProcessModel init(Integer orderId, UUID userId, BigDecimal amount) {
        return new PaymentProcessModel(orderId, userId, amount);
    }
}
