package ru.otus.inventory.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.otus.lib.kafka.model.ReservationProcessModel;
import ru.otus.lib.kafka.service.BusinessTopics;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryKafkaConsumerService {

    private final InventoryService service;

    @KafkaListener(topics = BusinessTopics.ORDER_RESERVATION_PROCESS, groupId = "${spring.kafka.consumer.group-id}")
    public void listenOrderReservationProcess(ReservationProcessModel model) {
        log.debug("Received new reservation process: {}", model);
        try {
            service.processReservation(model);
        } catch (Exception e) {
            log.error("Error while processing order reservation process", e);
            throw e;
        }
    }

//    @KafkaListener(topics = BusinessTopics.ORDER_RELEASE_INGREDIENTS, groupId = "${spring.kafka.consumer.group-id}")
//    public void listenOrderReleaseIngredients(ReleaseIngredientsModel model) {
//        log.debug("Received new release ingredients: {}", model);
//        try {
//            service.processRelease(model);
//        } catch (Exception e) {
//            log.error("Error while processing order release ingredients", e);
//            throw e;
//        }
//    }
}
