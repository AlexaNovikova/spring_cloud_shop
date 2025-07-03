package ru.otus.inventory.lib.client;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.otus.inventory.lib.api.InventoryServiceClient;
import ru.otus.inventory.lib.api.ProductAvailableQuantityResponseDto;
import ru.otus.inventory.lib.api.ProductBalanceResponseDto;

import java.util.List;
import java.util.UUID;

@Component
public class InventoryServiceClientImpl implements InventoryServiceClient {

    @Value("${webclient.inventoryService.url}")
    private String baseUrl;
    private RestClient client;


    @PostConstruct
    public void init() {
        client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public ProductBalanceResponseDto getActualBalance(List<Integer> productIds) {
        return client.get()
                .uri(BASE_INTERNAL_URL + BALANCE_URL,
                        uriBuilder -> uriBuilder
                                .queryParam("productIds", productIds.toArray()).build()
                )
                .retrieve()
                .body(ProductBalanceResponseDto.class);
    }

    @Override
    public ProductAvailableQuantityResponseDto getActualQuantity(Integer productId) {
        return client.get()
                .uri(BASE_INTERNAL_URL,
                        uriBuilder -> uriBuilder
                                .queryParam("productId", productId).build()
                )
                .retrieve()
                .body(ProductAvailableQuantityResponseDto.class);
    }
}
