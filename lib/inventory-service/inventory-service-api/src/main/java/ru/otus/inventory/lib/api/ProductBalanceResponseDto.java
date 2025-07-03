package ru.otus.inventory.lib.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBalanceResponseDto {
    private Map<Integer, Integer> balance;
}
