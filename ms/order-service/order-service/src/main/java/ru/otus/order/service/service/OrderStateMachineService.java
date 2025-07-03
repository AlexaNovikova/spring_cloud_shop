package ru.otus.order.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.common.error.ShopException;
import ru.otus.lib.kafka.model.PaymentProcessModel;
import ru.otus.lib.kafka.model.ReleaseProductsModel;
import ru.otus.lib.kafka.model.SendNotificationModel;
import ru.otus.lib.kafka.service.BusinessTopics;
import ru.otus.lib.kafka.service.KafkaProducerService;
import ru.otus.product.lib.api.ProductServiceClient;
import ru.otus.order.service.mapper.CartMapper;
import ru.otus.order.service.model.OrderEvent;
import ru.otus.order.service.model.OrderStatus;
import ru.otus.order.service.model.entity.Order;
import ru.otus.order.service.repository.CartRepository;
import ru.otus.order.service.repository.OrderRepository;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStateMachineService {

    private final OrderRepository orderRepository;
    private final KafkaProducerService kafkaProducerService;
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductServiceClient productServiceClient;

    public void sendToPaymentAction(Order order) {
        var orderId = order.getId();
        log.debug("Preparing order with id: {} for the payment action", orderId);
        order.changeStatus(OrderStatus.PENDING_PAYMENT);
        orderRepository.save(order);
        //orderRepository.updateStatus(orderId, OrderStatus.PENDING_PAYMENT);

        var userId = order.getUserId();
        var amount = order.getTotalPrice();
        var paymentModel = PaymentProcessModel.init(orderId, userId, amount);
        kafkaProducerService.send(BusinessTopics.ORDER_PAYMENT_PROCESS, paymentModel);
    }

    @Transactional
    public void startCollectingAction(Order order) {
        order.changeStatus(OrderStatus.PAID);
        var orderId = order.getId();
        log.debug("Collecting order with id: {} ", order.getId());
        order.changeStatus(OrderStatus.COLLECTING);
        orderRepository.save(order);

        var releaseModel = ReleaseProductsModel.initCollecting(orderId);
        kafkaProducerService.send(BusinessTopics.ORDER_RELEASE_PRODUCTS, releaseModel);

        var notificationModel = SendNotificationModel.orderIsCooking(order.getId(), order.getEmail());
        kafkaProducerService.send(BusinessTopics.NOTIFICATION_SEND, notificationModel);
    }

    @Transactional
    public void sendToReadyAction(Order order) {
        log.debug("Sending ready action email info for order with id: {}", order.getId());
        var notificationModel = SendNotificationModel.orderIsReady(order.getId(), order.getEmail());
        kafkaProducerService.send(BusinessTopics.NOTIFICATION_SEND, notificationModel);
    }

    @Transactional
    public void sendToDeliveredAction(Order order) {
        log.debug("Sending delivered action email info for order with id: {}", order.getId());
        var notificationModel = SendNotificationModel.orderIsDelivered(order.getId(), order.getEmail());
        kafkaProducerService.send(BusinessTopics.NOTIFICATION_SEND, notificationModel);
    }

    @Transactional
    public void cancelOrderAction(Order order, OrderEvent event) {
        var orderId = order.getId();
        log.debug("Preparing order with id: {} for the cancelling because of the event: {}", order.getId(), event);
        order.changeStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        var productIds = order
                .getItems()
                .stream()
                .map(i -> i.getId().getProductId())
                .map(Integer::longValue)
                .collect(Collectors.toList());
        var actualProducts = productServiceClient.getAllProductsByIds(productIds);
        var cart = cartMapper.map(order, actualProducts);
        cartRepository.save(cart);

        var releaseModel = ReleaseProductsModel.initRelease(orderId);
        kafkaProducerService.send(BusinessTopics.ORDER_RELEASE_PRODUCTS, releaseModel);

        var notificationModel = prepareErrorNotification(orderId, order.getEmail(), event);
        kafkaProducerService.send(BusinessTopics.NOTIFICATION_SEND, notificationModel);
    }

    private SendNotificationModel prepareErrorNotification(Integer orderId, String email, OrderEvent event) {
        if (OrderEvent.PRODUCTS_NOT_AVAILABLE == event) {
            return SendNotificationModel.orderCancelledByProducts(orderId, email);
        } else if (OrderEvent.PAYMENT_FAILED == event) {
            return SendNotificationModel.orderCancelledByPayment(orderId, email);
        }
        throw new ShopException("order.error.unknown.event", "Неизвестное событие");
    }
}
