package ru.otus.order.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDto {
    private UUID userId;
    private BigDecimal totalPrice;
    private List<ItemResponseDto> items;
}
