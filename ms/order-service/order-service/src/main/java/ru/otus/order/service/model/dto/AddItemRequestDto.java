package ru.otus.order.service.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequestDto {
    @NotNull
    private Integer productId;
    private Integer quantity;
}
