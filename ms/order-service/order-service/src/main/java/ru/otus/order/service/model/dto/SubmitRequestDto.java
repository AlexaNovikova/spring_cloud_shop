package ru.otus.order.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.payment.lib.api.PaymentMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRequestDto {
    private PaymentMethod paymentMethod;
    private String cardNumber;
}
