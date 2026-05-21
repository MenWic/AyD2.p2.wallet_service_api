package ayd2.p2b.wallet_service_api.feature.wallet.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionData {

    private UUID id;
    private UUID walletUserId;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private UUID referencePaymentId;
    private Instant createdAt;
}
