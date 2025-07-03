package ru.otus.notification.lib.client;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.otus.notification.lib.api.EmailRequestDto;
import ru.otus.notification.lib.api.NotificationServiceClient;

@Component
public class NotificationServiceClientImpl implements NotificationServiceClient {

    @Value("${webclient.notificationService.url}")
    private String baseUrl;
    private RestClient client;


    @PostConstruct
    public void init() {
        client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public String sendEmail(EmailRequestDto dto) {
        return client.post()
                .uri(BASE_INTERNAL_URL + SEND_EMAIL_URL)
                .body(dto)
                .retrieve()
                .body(String.class);
    }
}