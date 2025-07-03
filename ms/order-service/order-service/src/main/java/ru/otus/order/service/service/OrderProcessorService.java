package ru.otus.order.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.common.ShopUser;
import ru.otus.common.error.ShopException;
import ru.otus.lib.kafka.model.*;
import ru.otus.lib.kafka.service.BusinessTopics;
import ru.otus.lib.kafka.service.KafkaProducerService;
import ru.otus.order.service.model.OrderEvent;
import ru.otus.order.service.model.OrderStatus;
import ru.otus.order.service.model.entity.Order;
import ru.otus.order.service.model.entity.OrderItem;
import ru.otus.order.service.repository.OrderRepository;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessorService {

    private final OrderRepository orderRepository;
    private final KafkaProducerService kafkaProducerService;
    private final OrderStateMachineService orderStateMachineService;

    public Order createOrder(Order order, ShopUser shopUser) {
        var createdOrder = orderRepository.save(order);

        var orderId = order.getId();

        var productMap = createdOrder.getItems().stream()
                .collect(Collectors.toMap(
                        i -> i.getId().getProductId(),
                        OrderItem::getQuantity,
                        Integer::sum)
                );

        var reservationModel = ReservationProcessModel.initReserve(orderId, productMap);
        kafkaProducerService.send(BusinessTopics.ORDER_RESERVATION_PROCESS, reservationModel);

        var notificationModel = SendNotificationModel.orderCreated(orderId, order.getEmail());
        kafkaProducerService.send(BusinessTopics.NOTIFICATION_SEND, notificationModel);
        return createdOrder;
    }

    @Transactional
    public Order setStatus(Integer orderId, OrderStatus status) {
        var order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ShopException("order.not.found", "Order not found"));

        order.setStatus(status);
        if (OrderStatus.READY == status) {
            orderStateMachineService.sendToReadyAction(order);
        } else if (OrderStatus.DELIVERED == status) {
            orderStateMachineService.sendToDeliveredAction(order);
        } else {
            throw new ShopException("order.status.transition.invalid", "Неверный статус");
        }
        return order;
    }

    @Transactional
    public void handleReservationConfirmation(ReservationConfirmationModel model) {
        var orderId = model.getOrderId();
        var orderOpt = orderRepository.findByIdForUpdate(orderId);
        if (orderOpt.isEmpty()) {
            log.error("Order not found for reservation confirmation for: {}. Skip it", model);
            return;
        }
        var order = orderOpt.get();
        var status = model.getStatus();
        if (ProcessStatus.SUCCESS == status) {
            log.debug("Successfully reserved some products for the order with id {}", orderId);
            orderStateMachineService.sendToPaymentAction(order);
        } else {
            log.warn("Products are not reserved for order {}", orderId);
            orderStateMachineService.cancelOrderAction(order, OrderEvent.PRODUCTS_NOT_AVAILABLE);
        }
    }

    @Transactional
    public void handlePaymentConfirmation(PaymentConfirmationModel model) {
        var orderId = model.getOrderId();
        var orderOpt = orderRepository.findByIdForUpdate(orderId);
        if (orderOpt.isEmpty()) {
            log.error("Order not found for payment confirmation for: {}. Skip it", model);
            return;
        }

        var order = orderOpt.get();

        var status = model.getStatus();
        if (ProcessStatus.SUCCESS == status) {
            log.debug("Successfully paid for the order with id {}", orderId);
            orderStateMachineService.startCollectingAction(order);
        } else {
            log.warn("Failed to pay for the order {}", orderId);
            orderStateMachineService.cancelOrderAction(order, OrderEvent.PAYMENT_FAILED);
        }
    }

}
