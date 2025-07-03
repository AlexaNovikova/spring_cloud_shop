package ru.otus.product.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemDto {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String categoryTitle;
}