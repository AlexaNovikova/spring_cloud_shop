package ru.otus.lib.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationModel {
    private Integer orderId;
    private String email;
    private NotificationType type;
    private String subject;
    private String message;

    public static SendNotificationModel orderCreated(Integer orderId, String email) {
        var type = NotificationType.ORDER_CREATED;
        return prepare(orderId, email, type);
    }

    public static SendNotificationModel orderCancelledByProducts(Integer orderId, String email) {
        var type = NotificationType.PRODUCTS_NOT_AVAILABLE;
        return prepare(orderId, email, type);
    }

    public static SendNotificationModel orderCancelledByPayment(Integer orderId, String email) {
        var type = NotificationType.PAYMENT_FAILED;
        return prepare(orderId, email, type);
    }

    public static SendNotificationModel orderIsCooking(Integer orderId, String email) {
        var type = NotificationType.ORDER_IS_COLLECTING;
        return prepare(orderId, email, type);
    }

    public static SendNotificationModel orderIsReady(Integer orderId, String email) {
        var type = NotificationType.ORDER_IS_READY;
        return prepare(orderId, email, type);
    }

    public static SendNotificationModel orderIsDelivered(Integer orderId, String email) {
        var type = NotificationType.ORDER_IS_DELIVERED;
        return prepare(orderId, email, type);
    }

    private static SendNotificationModel prepare(Integer orderId, String email, NotificationType type) {
        var subject = type.getSubject();
        var message = type.getMessage().formatted(orderId);
        return SendNotificationModel.builder()
                .email(email)
                .type(type)
                .subject(subject)
                .message(message)
                .build();
    }
}
