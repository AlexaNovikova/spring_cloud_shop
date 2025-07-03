package ru.otus.notification.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.otus.lib.kafka.model.SendNotificationModel;
import ru.otus.lib.kafka.service.BusinessTopics;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationKafkaConsumerService {

    private final EmailService service;

    @KafkaListener(topics = BusinessTopics.NOTIFICATION_SEND, groupId = "${spring.kafka.consumer.group-id}")
    public void listenSendNotification(SendNotificationModel model) {
        log.debug("Received send notification model: {}", model);
        try {
            service.send(model);
        } catch (Exception e) {
            log.error("Error while processing send notification", e);
            throw e;
        }
    }
}
