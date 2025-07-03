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

    private final String RECIPE_CASH_NAME = "dishRecipes";

    private final InventoryRepository inventoryRepository;
    private final ReservedProductRepository reservedProductRepository;
//    private final MenuServiceClient menuServiceClient;
    private final CacheManager cacheManager;
    private final ProductRepository productRepository;
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
//        var dishQuantityMap = model.getDishQuantityMap();
//        var dishIds = new ArrayList<>(dishQuantityMap.keySet());
        var productQuantityMap = model.getProductQuantityMap();
        var productIds = new ArrayList<>(productQuantityMap.keySet());
        var orderId = model.getOrderId();
        log.debug("Trying to check products for dishes with ids: {} for order with id: {}", productIds, orderId);
/* todo потом кэшировать
        var cachedRecipes = getCachedRecipes(dishIds);
        var missingIds = dishIds.stream()
                .filter(id -> !cachedRecipes.containsKey(id))
                .toList();

        if (!missingIds.isEmpty()) {
            var freshRecipes = menuServiceClient.getRecipes(dishIds);
            freshRecipes.getDishProductQuantityMap().entrySet().forEach(dish -> {
                cacheManager.getCache(RECIPE_CASH_NAME).put(dish.getKey(), dish.getValue());
            });

            cachedRecipes.putAll(freshRecipes.getDishProductQuantityMap());
        }*/
//
//        var recipes = menuServiceClient.getRecipes(dishIds);
//
//        var productQuantityList = recipes.getDishProductQuantityMap().entrySet().stream()
//                .map(dpqEntry -> {
//                    var dishId = dpqEntry.getKey();
//                    var dishQuantity = dishQuantityMap.getOrDefault(dishId, 1);
//                    return dpqEntry.getValue().entrySet().stream()
//                            .collect(Collectors.toMap(
//                                    i -> i.getKey(),
//                                    i -> i.getValue() * dishQuantity,
//                                    Integer::sum
//                            ));
//                })
//                .flatMap(innerMap -> innerMap.entrySet().stream())
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue,
//                        Integer::sum
//                ));
//
//        var productIds = productQuantityList.keySet();
        var inventories = inventoryRepository.findAllById(productIds);
        if (CollectionUtils.isEmpty(inventories)) {
//            log.error("Unable to cook all dishes with ids: {}. Inventory is empty for order with id: {}", dishIds, orderId);
//            kafkaProducerService.send(
//                    BusinessTopics.ORDER_RESERVATION_CONFIRMATION,
//                    ReservationConfirmationModel.error(orderId, "Inventory is empty")
//            );
//            return;
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
//
//    @Transactional
//    public void processRelease(ReleaseIngredientsModel model) {
//        var orderId = model.getOrderId();
//        var releaseType = model.getType();
//
//        if (ReleaseType.COOKING == releaseType) {
//            var reservedProducts = reservedProductRepository.findAllByOrderId(orderId);
//            if (CollectionUtils.isEmpty(reservedProducts)) {
//                log.warn("Products for order with id {} already released", orderId);
//                return;
//            }
//            var productMap = reservedProducts.stream()
//                    .collect(Collectors.toMap(
//                            i -> i.getId().getProductId(),
//                            i -> i.getQuantity(),
//                            (i1, i2) -> i1 + i2));
//            var productIds = productMap.keySet();
//            var inventories = inventoryRepository.findForUpdateAllByProductIds(productIds);
//            if (CollectionUtils.isEmpty(inventories)) {
//                log.warn("Inventory does not contain products with ids: {}", productIds);
//                return;
//            }
//
//            inventories.forEach(i -> i.decrementQuantity(productMap.getOrDefault(i.getProductId(), 0)));
//
//            inventoryRepository.saveAll(inventories);
//            inventoryRepository.flush();
//        }
//
//        reservedProductRepository.deleteAllByOrderId(orderId);
//    }
/*
    private Map<Integer, Map<Integer, Integer>> getCachedRecipes(List<Integer> ids) {
        Cache cache = cacheManager.getCache(RECIPE_CASH_NAME);
        return ids.stream()
                .map(id -> {
                    Cache.ValueWrapper wrapper = cache.get(id);
                    return wrapper != null ? (RecipeResponseDto) wrapper.get() : null;
                })
                .filter(v -> v != null)
                .collect(Collectors.toMap(RecipeResponseDto::getDishId, Function.identity()));
    }*/
}
