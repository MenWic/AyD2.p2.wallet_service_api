package ayd2.p2b.wallet_service_api.feature.payment.application.register;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.payment.application.port.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.RegisterPaymentCommand;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.port.SystemConfigRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Narrow transactional component that performs the atomic write for a new payment:
 * wallet debit, payment save (saveAndFlush), and PAYMENT transaction save.
 *
 * Keeping this in a separate @Transactional component ensures that if
 * PaymentRepositoryPort.save() throws DuplicatePaymentIdempotencyKeyException,
 * the entire unit of work (debit + payment + transaction) rolls back before
 * the outer RegisterPaymentUseCase applies race-recovery re-read logic.
 */
@Component
@RequiredArgsConstructor
public class RegisterPaymentWriteOperation {

    private final WalletRepositoryPort walletRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final PaymentRepositoryPort paymentRepository;
    private final SystemConfigRepositoryPort configRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse write(RegisterPaymentCommand command, String idempotencyKey,
            RequesterContext requester) {

        WalletAccount wallet = walletRepository.findByUserId(command.getUserId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "resource.not_found",
                        "Wallet not found for user: " + command.getUserId()));

        BigDecimal commissionPercent = configRepository.find().getCommissionPercent();
        BigDecimal commissionAmount = command.getAmount()
                .multiply(commissionPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = command.getAmount().subtract(commissionAmount);

        wallet.debit(command.getAmount());
        walletRepository.save(wallet);

        PaymentData paymentData = PaymentData.builder()
                .id(UUID.randomUUID())
                .userId(command.getUserId())
                .congressId(command.getCongressId())
                .institutionId(command.getInstitutionId())
                .congressNameSnapshot(command.getCongressNameSnapshot())
                .institutionNameSnapshot(command.getInstitutionNameSnapshot())
                .commissionPercentSnapshot(commissionPercent)
                .amount(command.getAmount())
                .commissionAmount(commissionAmount)
                .netAmount(netAmount)
                .paymentDate(command.getPaymentDate())
                .idempotencyKey(idempotencyKey)
                .createdBy(requester.getUserId())
                .createdAt(Instant.now())
                .build();

        // saveAndFlush inside the adapter surfaces the unique-key violation here,
        // inside this transaction, so the whole unit of work rolls back on conflict.
        PaymentData savedPayment = paymentRepository.save(paymentData);

        TransactionData transaction = TransactionData.builder()
                .id(UUID.randomUUID())
                .walletUserId(command.getUserId())
                .type(TransactionType.PAYMENT)
                .amount(command.getAmount().negate())
                .transactionDate(command.getPaymentDate())
                .referencePaymentId(savedPayment.getId())
                .createdBy(requester.getUserId())
                .createdAt(Instant.now())
                .build();

        transactionRepository.save(transaction);

        return paymentMapper.toResponse(savedPayment);
    }
}
