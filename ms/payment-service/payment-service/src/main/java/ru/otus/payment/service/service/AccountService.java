package ru.otus.payment.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.common.error.ShopException;
import ru.otus.lib.kafka.model.CreateAccountModel;
import ru.otus.lib.kafka.model.PaymentConfirmationModel;
import ru.otus.lib.kafka.model.PaymentProcessModel;
import ru.otus.lib.kafka.service.BusinessTopics;
import ru.otus.lib.kafka.service.KafkaProducerService;
import ru.otus.payment.service.mapper.AccountMapper;
import ru.otus.payment.service.model.dto.AccountResponseDto;
import ru.otus.payment.service.model.dto.FillUpAccountRequestDto;
import ru.otus.payment.service.model.entity.Payment;
import ru.otus.payment.service.repository.AccountRepository;
import ru.otus.payment.service.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final PaymentRepository paymentRepository;
    private final AccountMapper mapper;
    private final KafkaProducerService kafkaProducerService;

    public AccountResponseDto get(UUID userId) {
        var account = repository.findById(userId);
        if (account.isEmpty()) {
            log.error("Account for the user with id {} not found", userId);
            throw new ShopException("account.not.found", "Счет не найден");
        }

        return mapper.map(account.get());
    }

    @Transactional
    public AccountResponseDto fillUp(UUID userId, FillUpAccountRequestDto dto) {
        var accountOpt = repository.findById(userId);
        if (accountOpt.isEmpty()) {
            log.error("Account for the user with id {} not found", userId);
            throw new ShopException("account.not.found", "Счет не найден");
        }

        var account = accountOpt.get();
        account.setAmount(account.getAmount().add(dto.getAmount()));
        var updatedAccount = repository.save(account);
        return mapper.map(updatedAccount);
    }

    @Transactional
    public void handlePaymentProcess(PaymentProcessModel model) {
        var userId = model.getUserId();
        var orderId = model.getOrderId();
        var accountOpt = repository.findById(userId);
        if (accountOpt.isEmpty()) {
            log.error("Account for the user with id {} not found", userId);
            var errorModel = PaymentConfirmationModel.error(orderId, "Account not found");
            kafkaProducerService.send(BusinessTopics.ORDER_PAYMENT_CONFIRMATION, errorModel);
            return;
        }

        var paymentAlreadyPassed = paymentRepository.existsByOrderIdAndAccountUserId(orderId, accountOpt.get().getUserId());
        if (paymentAlreadyPassed) {
            log.warn("Payment already processed. Skip it");
            return;
        }

        var account = accountOpt.get();
        var currentUserAmount = account.getAmount();
        var amountAfterPayment = currentUserAmount.subtract(model.getAmount());

        if (amountAfterPayment.compareTo(BigDecimal.ZERO) == -1) {
            log.error("Insufficient funds for user with id: {} to pay for the order with id: {}", userId, orderId);
            var errorModel = PaymentConfirmationModel.error(orderId, "Insufficient funds");
            kafkaProducerService.send(BusinessTopics.ORDER_PAYMENT_CONFIRMATION, errorModel);
            return;
        } else {
            log.debug("Current amount is enough to pay for the order");
            account.setAmount(amountAfterPayment);

            var payment = new Payment();
            payment.setAccount(account);
            payment.setOrderId(orderId);
            payment.setAmount(model.getAmount());
            repository.save(account);
            paymentRepository.save(payment);

            var successModel = PaymentConfirmationModel.success(orderId);
            kafkaProducerService.send(BusinessTopics.ORDER_PAYMENT_CONFIRMATION, successModel);
            return;
        }
    }

    @Transactional
    public void handleCreateAccount(CreateAccountModel model) {
        var userId = model.getUserId();
        log.debug("Trying to create new account for user with id: {}", userId);
        var accountOpt = repository.findById(userId);
        if (accountOpt.isPresent()) {
            log.warn("Account for the user with id {} already exists", userId);
            return;
        }

        log.debug("Account for user with id {} not found. Creating it...", userId);
        var account = mapper.map(userId);
        repository.save(account);
    }
}
