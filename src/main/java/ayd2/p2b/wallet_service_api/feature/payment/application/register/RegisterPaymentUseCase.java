package ayd2.p2b.wallet_service_api.feature.payment.application.register;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.payment.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.request.RegisterPaymentRequest;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import ayd2.p2b.wallet_service_api.feature.systemconfig.SystemConfigRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterPaymentUseCase {

    private final WalletRepositoryPort walletRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final PaymentRepositoryPort paymentRepository;
    private final SystemConfigRepositoryPort configRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse execute(RegisterPaymentRequest request, String idempotencyKey, UUID createdBy) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(paymentMapper::toResponse)
                .orElseGet(() -> registerNew(request, idempotencyKey, createdBy));
    }

    private PaymentResponse registerNew(RegisterPaymentRequest request, String idempotencyKey, UUID createdBy) {
        WalletAccount wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "resource.not_found",
                        "Wallet not found for user: " + request.getUserId()
                ));

        BigDecimal commissionPercent = configRepository.find().getCommissionPercent();
        BigDecimal commissionAmount = request.getAmount()
                .multiply(commissionPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = request.getAmount().subtract(commissionAmount);

        wallet.debit(request.getAmount());
        walletRepository.save(wallet);

        PaymentData paymentData = PaymentData.builder()
                .id(UUID.randomUUID())
                .userId(request.getUserId())
                .congressId(request.getCongressId())
                .institutionId(request.getInstitutionId())
                .congressNameSnapshot(request.getCongressNameSnapshot())
                .institutionNameSnapshot(request.getInstitutionNameSnapshot())
                .commissionPercentSnapshot(commissionPercent)
                .amount(request.getAmount())
                .commissionAmount(commissionAmount)
                .netAmount(netAmount)
                .paymentDate(request.getPaymentDate())
                .idempotencyKey(idempotencyKey)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .build();

        PaymentData savedPayment = paymentRepository.save(paymentData);

        TransactionData transaction = TransactionData.builder()
                .id(UUID.randomUUID())
                .walletUserId(request.getUserId())
                .type(TransactionType.PAYMENT)
                .amount(request.getAmount().negate())
                .transactionDate(request.getPaymentDate())
                .referencePaymentId(savedPayment.getId())
                .createdAt(Instant.now())
                .build();

        transactionRepository.save(transaction);

        return paymentMapper.toResponse(savedPayment);
    }
}
