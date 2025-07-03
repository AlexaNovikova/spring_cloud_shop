package ru.otus.product.lib.api;


import org.springframework.util.MultiValueMap;

import java.util.List;


public interface ProductServiceClient {

    String BASE_URL = "/api/v1/product";

    String ALL_URL = "all";

    ProductListResponseDto getAllProducts( MultiValueMap<String, String> params);

    ProductDto getOneProductById(Long id);

    ProductListResponseDto getAllProductsByIds(List<Long> productIds);
}
