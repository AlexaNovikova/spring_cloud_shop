package ru.otus.notification.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;
import ru.otus.common.error.ShopException;
import ru.otus.notification.lib.api.EmailRequestDto;
import ru.otus.notification.lib.api.NotificationServiceClient;
import ru.otus.notification.service.service.EmailService;

@Slf4j
@Validated
@RestController
@RequestMapping(NotificationServiceClient.BASE_INTERNAL_URL)
@RequiredArgsConstructor
public class NotificationController implements NotificationServiceClient {

    private final EmailService service;

    @Override
    @PostMapping(SEND_EMAIL_URL)
    public String sendEmail(@RequestBody EmailRequestDto dto) {
        try {
            var context = new Context();
            context.setVariable("subject", dto.getIntro());
            context.setVariable("text", dto.getMessage());
            service.sendEmailFromTemplate(dto.getTo(), dto.getSubject(), "email-template", context);
            return "ok";

        } catch (Exception e) {
            throw new ShopException("email.sending.error", "Ошибка отправки email");
        }
    }
}
