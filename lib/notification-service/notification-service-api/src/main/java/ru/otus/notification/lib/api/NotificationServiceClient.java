package ru.otus.notification.lib.api;

public interface NotificationServiceClient {

    String BASE_INTERNAL_URL = "/api/internal/notification";

    String SEND_EMAIL_URL = "/send-email";

    String sendEmail(EmailRequestDto dto);
}
