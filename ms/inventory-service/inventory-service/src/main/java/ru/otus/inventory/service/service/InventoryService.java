package ru.otus.inventory.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.otus.inventory.lib.api.ProductAvailableQuantityResponseDto;
import ru.otus.inventory.lib.api.ProductBalanceResponseDto;
import ru.otus.inventory.service.model.entity.Inventory;
import ru.otus.inventory.service.model.entity.ReservedProduct;
import ru.otus.inventory.service.model.entity.ReservedProductId;
import ru.otus.inventory.service.repository.InventoryRepository;
import ru.otus.inventory.service.repository.ProductRepository;
import ru.otus.inventory.service.repository.ReservedProductRepository;
import ru.otus.lib.kafka.model.ReservationConfirmationModel;
import ru.otus.lib.kafka.model.ReservationProcessModel;
import ru.otus.lib.kafka.service.BusinessTopics;
import ru.otus.lib.kafka.service.KafkaProducerService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReservedProductRepository reservedProductRepository;
    private final KafkaProducerService kafkaProducerService;

    public ProductBalanceResponseDto getBalance(List<Integer> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            log.warn("No products found for ids: {}", productIds);
            return new ProductBalanceResponseDto(Map.of());
        }

        var values = inventoryRepository.findAllById(productIds);
        if (CollectionUtils.isEmpty(values)) {
            log.warn("No product balance found for ids: {}", productIds);
            return new ProductBalanceResponseDto(Map.of());
        }

        var reservedProducts = reservedProductRepository.findAllByProductIds(productIds);
        var reservedProductMap = reservedProducts.stream()
                .collect(Collectors.toMap(
                        rp -> rp.getId().getProductId(),
                        rp -> rp.getQuantity(),
                        Integer::sum)
                );

        var map = values.stream().collect(Collectors.toMap(
                i -> i.getProductId(),
                i -> i.getQuantity() - reservedProductMap.getOrDefault(i.getProductId(), 0),
                Integer::sum
        ));
        return new ProductBalanceResponseDto(map);
    }

    @Transactional
    public void processReservation(ReservationProcessModel model) {
        var productQuantityMap = model.getProductQuantityMap();
        var productIds = new ArrayList<>(productQuantityMap.keySet());
        var orderId = model.getOrderId();
        log.debug("Trying to check products for dishes with ids: {} for order with id: {}", productIds, orderId);

        var inventories = inventoryRepository.findAllById(productIds);
        if (CollectionUtils.isEmpty(inventories)) {
            log.error("Unable to reserve all products with ids: {}. Inventory is empty for order with id: {}", productIds, orderId);
            kafkaProducerService.send(
                    BusinessTopics.ORDER_RESERVATION_CONFIRMATION,
                    ReservationConfirmationModel.error(orderId, "Inventory is empty")
            );
            return;
        }

        var reservedProducts = reservedProductRepository.findAllByProductIds(productIds);
        var reservedProductMap = reservedProducts.stream()
                .collect(Collectors.toMap(
                        rp -> rp.getId().getProductId(),
                        rp -> rp.getQuantity(),
                        Integer::sum)
                );

        //каких продуктов не хватает
        var shortageIds = inventories.stream()
                .filter(i -> {
                    var productId = i.getProductId();
                    var requestedQuantity = productQuantityMap.get(productId);
                    var reservedQuantity = reservedProductMap.getOrDefault(productId, 0);
                    return requestedQuantity == null || requestedQuantity > i.getQuantity() - reservedQuantity;
                })
                .map(Inventory::getProductId)
                .toList();

        if (!CollectionUtils.isEmpty(shortageIds)) {
            log.error("Unable to reserve products, there is lack of products with ids: {} for the order with id: {}", shortageIds, orderId);
            kafkaProducerService.send(
                    BusinessTopics.ORDER_RESERVATION_CONFIRMATION,
                    ReservationConfirmationModel.error(orderId, "Some inventory is empty or are not enough")
            );
            return;
        }

        log.debug("Shortage products id list is empty! Everything is fine, reserving products...");
        var inventoryMap = inventories.stream().collect(Collectors.toMap(Inventory::getProductId, Inventory::getProduct));

        var reservedProductList = productQuantityMap.entrySet().stream()
                .map(entry -> {
                    var id = ReservedProductId.builder()
                            .orderId(orderId)
                            .build();
                    return ReservedProduct.builder()
                            .id(id)
                            .quantity(entry.getValue())
                            .product(inventoryMap.get(entry.getKey()))
                            .build();
                })
                .collect(Collectors.toList());
        reservedProductRepository.saveAll(reservedProductList);

        kafkaProducerService.send(
                BusinessTopics.ORDER_RESERVATION_CONFIRMATION,
                ReservationConfirmationModel.success(orderId)
        );
    }

    public ProductAvailableQuantityResponseDto getBalanceForProduct(Integer productId) {
        var inventory = inventoryRepository.findByProductId(productId);
        var responce = new ProductAvailableQuantityResponseDto();
        responce.setQuantity(inventory.getQuantity());
        return responce;

    }

}
