package ru.otus.inventory.lib.api;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAvailableQuantityResponseDto {
    private Integer quantity;
}
