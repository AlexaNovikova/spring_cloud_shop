package ru.otus.order.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.otus.lib.kafka.model.PaymentConfirmationModel;
import ru.otus.lib.kafka.model.ReservationConfirmationModel;
import ru.otus.lib.kafka.service.BusinessTopics;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaConsumerService {

    private final OrderProcessorService service;

    @KafkaListener(topics = BusinessTopics.ORDER_RESERVATION_CONFIRMATION,
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenOrderReservationConfirmation(ReservationConfirmationModel model) {
        log.debug("Received new reservation confirmation: {}", model);
        try {
            service.handleReservationConfirmation(model);
        } catch (Exception e) {
            log.error("Error while processing order reservation confirmation", e);
            throw e;
        }
    }

    @KafkaListener(topics = BusinessTopics.ORDER_PAYMENT_CONFIRMATION,
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenOrderPaymentConfirmation(PaymentConfirmationModel model) {
        log.debug("Received new payment confirmation: {}", model);
        try {
            service.handlePaymentConfirmation(model);
        } catch (Exception e) {
            log.error("Error while processing order payment confirmation", e);
            throw e;
        }
    }
}
