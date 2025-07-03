package ru.otus.order.service.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    CREATED, PENDING_PAYMENT, PAID, COLLECTING, READY, DELIVERED, CANCELLED;

    public static boolean isValidForManualTransition(OrderStatus value) {
        return value == READY || value == DELIVERED;
    }

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            CREATED, EnumSet.of(PENDING_PAYMENT, CANCELLED),
            PENDING_PAYMENT, EnumSet.of(CANCELLED, PAID),
            PAID, EnumSet.of(COLLECTING),
            COLLECTING, EnumSet.of(READY),
            READY, EnumSet.of(DELIVERED),
            DELIVERED, EnumSet.noneOf(OrderStatus.class),
            CANCELLED, EnumSet.noneOf(OrderStatus.class)
    );

    public boolean canTransitionTo(OrderStatus newStatus) {
        return TRANSITIONS.get(this).contains(newStatus);
    }
}
