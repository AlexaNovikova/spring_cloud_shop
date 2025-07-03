package ru.otus.product.service.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.otus.product.service.repository.specifications.ProductSpecifications;
import ru.otus.product.service.service.ProductService;
import ru.otus.product.lib.api.ProductListResponseDto;
import ru.otus.product.lib.api.ProductDto;
import ru.otus.product.lib.api.ProductServiceClient;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping(ProductServiceClient.BASE_URL)
@RequiredArgsConstructor
public class ProductController implements ProductServiceClient {

    private final ProductService productService;

    @GetMapping("/all")
    public ProductListResponseDto getAllProducts(@RequestParam MultiValueMap<String, String> params) {
        return productService.findAll(ProductSpecifications.build(params));
    }

    @Override
    @GetMapping("/{id}")
    public ProductDto getOneProductById(@PathVariable Long id) {
        return productService
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product doesn't exists id: " + id));
    }

    @Override
    @GetMapping
    public ProductListResponseDto getAllProductsByIds(@RequestParam List<Long> productIds) {
        return productService.getByIds(productIds);
    }

    @PostMapping
    public ProductDto createNewProduct(@RequestBody @Validated ProductDto productDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(
                    String.valueOf(bindingResult
                            .getAllErrors()
                            .stream()
                            .map(ObjectError::getDefaultMessage)
                            .collect(Collectors.toList())));
        }
        return productService.createNewProduct(productDto);
    }

    @PutMapping
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        productService.deleteById(id);
    }
}
