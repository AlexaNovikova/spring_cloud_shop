package ru.otus.lib.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseProductsModel {
    private Integer orderId;
    private ReleaseType type;

    public static ReleaseProductsModel initCollecting(Integer orderId) {
        return ReleaseProductsModel.builder()
                .orderId(orderId)
                .type(ReleaseType.COLLECTING)
                .build();
    }

    public static ReleaseProductsModel initRelease(Integer orderId) {
        return ReleaseProductsModel.builder()
                .orderId(orderId)
                .type(ReleaseType.RELEASE)
                .build();
    }
}
