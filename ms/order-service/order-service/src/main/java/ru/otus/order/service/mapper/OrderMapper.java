package ru.otus.order.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.otus.order.service.model.OrderStatus;
import ru.otus.order.service.model.dto.OrderResponseDto;
import ru.otus.order.service.model.entity.Cart;
import ru.otus.order.service.model.entity.Order;
import ru.otus.order.service.model.entity.OrderItem;
import ru.otus.order.service.model.entity.OrderItemId;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    default Order map(Cart cart) {
        Order order = Order.builder()
                .userId(cart.getUserId())
                .status(OrderStatus.CREATED)
                .createdAt(OffsetDateTime.now())
                .items(new HashSet<>()) // Инициализируем коллекцию
                .build();

        cart.getItems().values().forEach(i -> {
            OrderItem item = OrderItem.builder()
                    .id(new OrderItemId(null, i.getProductId()))
                    .name(i.getName())
                    .price(i.getPrice())
                    .quantity(i.getQuantity())
                    .order(order) // Устанавливаем связь с Order
                    .build();

            order.getItems().add(item);
        });

        var totalPrice = order.getItems().stream()
                .map(i -> {
                    var q = i.getQuantity() != null ? i.getQuantity() : 1;
                    var p = i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO;
                    return p.multiply(new BigDecimal(q));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(totalPrice);
        return order;
    }

    OrderResponseDto map(Order order);

    @Mapping(target = "productId", source = "id.productId")
    OrderResponseDto.Item map(OrderItem orderItem);
}
