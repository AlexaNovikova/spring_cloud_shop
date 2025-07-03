package ru.otus.order.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.otus.common.error.ShopException;
import ru.otus.common.Roles;
import ru.otus.common.ShopUser;
import ru.otus.inventory.lib.api.InventoryServiceClient;
import ru.otus.inventory.lib.api.ProductBalanceResponseDto;
import ru.otus.product.lib.api.ProductDto;
import ru.otus.product.lib.api.ProductServiceClient;
import ru.otus.order.service.mapper.CartMapper;
import ru.otus.order.service.mapper.OrderMapper;
import ru.otus.order.service.model.OrderStatus;
import ru.otus.order.service.model.dto.AddItemRequestDto;
import ru.otus.order.service.model.dto.CartResponseDto;
import ru.otus.order.service.model.dto.OrderResponseDto;
import ru.otus.order.service.model.entity.Cart;
import ru.otus.order.service.repository.CartRepository;
import ru.otus.order.service.repository.OrderRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final CartMapper cartMapper;
    private final OrderMapper orderMapper;
    private final OrderProcessorService orderProcessorService;

    @Transactional
    public CartResponseDto addItem(AddItemRequestDto dto, UUID userId) {
        Cart cart;
        var cartOpt = cartRepository.findById(userId);
        if (cartOpt.isPresent()) {
            log.debug("Found existing cart for user with id: {}", userId);
            cart = cartOpt.get();
        } else {
            log.debug("No cart for current user with id: {}. Creating it...", userId);
            cart = Cart.newCart(userId);
        }

        var newProductId = dto.getProductId();
        var quantity = dto.getQuantity();

        Optional<Cart.Item> existingProduct = !CollectionUtils.isEmpty(cart.getItems()) ?
                cart.getItems()
                        .values()
                        .stream()
                        .filter(d -> d != null && d.getProductId().equals(newProductId))
                        .findFirst()
                : Optional.empty();
        if (existingProduct.isPresent()) {
            quantity = quantity + existingProduct.get().getQuantity();
        }

        Integer availableQuantity = null;
        try {
            availableQuantity = inventoryServiceClient.getActualQuantity(newProductId).getQuantity();
            log.debug("Available quantity {}", availableQuantity);
        } catch (Exception e) {
            log.error("Unable to get product info", e);
        }

        if (availableQuantity == null || availableQuantity < quantity) {
            log.error("Current product with id {} is unavailable with quantity {}", newProductId, quantity);
            throw new ShopException("product.is.unavailable", "Выбранный продукт недоступен для заказа в указанном количестве");
        }

        var newProduct = productServiceClient.getOneProductById(Long.valueOf(newProductId));
        var newCartItem= new Cart.Item();
        newCartItem.setProductId(newProductId);
        newCartItem.setName(newProduct.getTitle());
        newCartItem.setPrice(newProduct.getPrice());
        newCartItem.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);

        cart.addItem(newCartItem);

        cartRepository.save(cart);
        return cartMapper.map(cart);
    }

    @Transactional
    public CartResponseDto getCart(UUID userId) {
        var cartOpt = cartRepository.findById(userId);
        if (cartOpt.isPresent()) {
            log.debug("Found existing cart for user with id: {}", userId);
            return cartMapper.map(cartOpt.get());
        } else {
            log.debug("No cart for current user with id: {}. Creating it...", userId);
            var cart = Cart.newCart(userId);
            cartRepository.save(cart);
            return cartMapper.map(cart);
        }
    }

    @Transactional
    public OrderResponseDto submit(ShopUser shopUser) {
        var userId = shopUser.getId();
        var cartOpt = cartRepository.findById(userId);
        if (cartOpt.isEmpty()) {
            log.error("Cart for the user with id {} not found", userId);
            throw new ShopException("cart.not.found", "Корзина не найдена");
        }
        if (CollectionUtils.isEmpty(cartOpt.get().getItems())) {
            log.error("Cart items list is empty for user with id: {}. Unable to submit", userId);
            throw new ShopException("cart.items.list.is.empty", "Попытка оформить заказ с пустой корзиной");
        }

        var order = orderMapper.map(cartOpt.get());
        order.setEmail(shopUser.getLogin());
        var createdOrder = orderProcessorService.createOrder(order, shopUser);
        cartRepository.deleteById(userId);

        return orderMapper.map(createdOrder);
    }

    @Transactional
    public OrderResponseDto getStatus(Integer orderId, ShopUser shopUser) {
        var orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.error("Order with id {} not found", orderId);
            throw new ShopException("order.not.found", "Заказ не найден");
        }

        var order = orderOpt.get();
        if (shopUser.getRoles().contains(Roles.CLIENT) && !Objects.equals(order.getUserId(), shopUser.getId())) {
            log.error("User with id {} trying to get not own order with id {}", shopUser.getId(), orderId);
            throw new ShopException("order.not.found", "Заказ не найден");
        }

        return orderMapper.map(order);
    }

    @Transactional
    public OrderResponseDto setStatus(Integer orderId, OrderStatus status) {
        var order = orderProcessorService.setStatus(orderId, status);
        return orderMapper.map(order);
    }
}
