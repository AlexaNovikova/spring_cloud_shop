package ru.otus.product.service.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.otus.product.lib.api.ProductDto;
import ru.otus.product.lib.api.ProductItemDto;
import ru.otus.product.lib.api.ProductListResponseDto;
import ru.otus.product.service.mapper.ProductMapper;
import ru.otus.product.service.model.entity.Category;
import ru.otus.product.service.model.entity.Product;
import ru.otus.product.service.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final CategoryService categoryService;

    private final ProductMapper productMapper;

    public ProductListResponseDto findAll(Specification<Product> spec) {
        ProductListResponseDto productListResponseDto = new ProductListResponseDto();
        List<ProductDto> productItemDtos =  productRepository
                .findAll(spec)
                .stream()
                .map(productMapper::map)
                .collect(Collectors.toList());
        productListResponseDto.setProducts(productItemDtos);
        return productListResponseDto;
    }

    public Optional<ProductDto> findById(Long id) {
        return productRepository.findById(id).map(productMapper::map);
    }

    @Transactional
    public ProductDto createNewProduct(ProductDto productDto) {
        Product product = new Product();
        product.setPrice(productDto.getPrice());
        product.setTitle(productDto.getTitle());
        Category category = categoryService
                .findByTitle(productDto.getCategoryTitle())
                .orElseThrow(() ->
                        new EntityNotFoundException("Category doesn't exists product.categoryTitle = "
                                + productDto.getCategoryTitle()));
        product.setCategory(category);
        productRepository.save(product);
        return productMapper.map(product);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        Product product = productRepository.findById(productDto.getId()).orElseThrow(() ->
                new EntityNotFoundException("Product doesn't exists id: "
                        + productDto.getId() + " (for update)"));
        product.setPrice(productDto.getPrice());
        product.setTitle(productDto.getTitle());
        Category category = categoryService
                .findByTitle(productDto.getCategoryTitle())
                .orElseThrow(() -> new EntityNotFoundException
                        ("Category doesn't exists product.categoryTitle = "
                                + productDto.getCategoryTitle() + " (Product creation)"));
        product.setCategory(category);
        return productMapper.map(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductListResponseDto getByIds(List<Long> productIds) {
            var products = productRepository.findAllById(productIds);
            if (CollectionUtils.isEmpty(productIds)) {
                return new ProductListResponseDto(new ArrayList<>());
            }
            return new ProductListResponseDto
                    (products.stream().map(productMapper::map).toList());
    }
}
