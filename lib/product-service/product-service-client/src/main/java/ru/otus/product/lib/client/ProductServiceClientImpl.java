package ru.otus.product.lib.client;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import ru.otus.product.lib.api.ProductDto;
import ru.otus.product.lib.api.ProductListResponseDto;
import ru.otus.product.lib.api.ProductServiceClient;

import java.util.List;

@Component
public class ProductServiceClientImpl implements ProductServiceClient {

    @Value("${webclient.productService.url}")
    private String baseUrl;
    private RestClient client;


    @PostConstruct
    public void init() {
        client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public ProductListResponseDto getAllProducts(MultiValueMap<String, String> params) {
        return client.get()
                .uri(BASE_URL + ALL_URL,
                        uriBuilder -> uriBuilder
                                .queryParams(params)
                                .build()
                )
                .retrieve()
                .body(ProductListResponseDto.class);
    }

    @Override
    public ProductDto getOneProductById(Long id) {
        return client.get()
                .uri(BASE_URL + "/" + id)
                .retrieve()
                .body(ProductDto.class);
    }

    @Override
    public ProductListResponseDto getAllProductsByIds(List<Long> productIds) {
        return client.get()
                .uri(BASE_URL,
                        uriBuilder -> uriBuilder
                                .queryParam("productIds", productIds).build()
                )
                .retrieve()
                .body(ProductListResponseDto.class);
    }
}