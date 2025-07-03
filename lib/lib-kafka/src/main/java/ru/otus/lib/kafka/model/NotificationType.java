package ru.otus.lib.kafka.model;

public enum NotificationType {
    ORDER_CREATED("Ваш заказ создан", "Здравствуйте! Спасибо за заказ! Мы передали Ваш заказ № %d на обработку."),
    PRODUCTS_NOT_AVAILABLE ("В Вашем заказе есть продукты, которые недоступны для заказа", "К сожалению, некоторые продукты закончились на складе. " +
            "Выберите другие продукты, либо удалите отсутствующие продукты из корзины и попробуйте оформить заказ еще раз! Спасибо за понимание!"),
    PAYMENT_FAILED("Оплата не прошла", "К сожалению, оплата не прошла, возможно недостаточно средств на счете." +
            " Пополните счет и попробуйте оформить заказ еще раз."),
    ORDER_IS_COLLECTING("Заказ принят", "Ваш заказ № %d принят! Мы уже собиреам его!"),
    ORDER_IS_READY("Заказ собран", "Ваш заказ № %d готов! Ожидайте курьера!"),
    ORDER_IS_DELIVERED("Заказ доставлен", "Ваш заказ № %d получен.");

    private final String subject;
    private final String message;

    NotificationType(String subject, String message) {
        this.subject = subject;
        this.message = message;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getMessage() {
        return this.message;
    }
}
