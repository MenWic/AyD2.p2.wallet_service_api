package ayd2.p2b.wallet_service_api.integration.persistence;

import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.entity.TransactionEntity;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.entity.WalletEntity;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.repository.TransactionJpaRepository;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.repository.WalletJpaRepository;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.specification.TransactionSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ayd2.p2b.wallet_service_api.TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class TransactionRepositoryAdapterTest {

    @Autowired
    private TransactionJpaRepository transactionRepository;

    @Autowired
    private WalletJpaRepository walletRepository;

    private static final UUID WALLET_USER = UUID.fromString("ee000000-0000-0000-0000-000000000001");

    @BeforeEach
    void seed() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();

        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(WALLET_USER);
        wallet.setBalance(new BigDecimal("500.00"));
        wallet.setCreatedAt(Instant.now());
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
    }

    @Test
    void should_persist_top_up_transaction_with_created_by() {
        UUID createdBy = UUID.randomUUID();
        TransactionEntity tx = buildTransaction(
                WALLET_USER, TransactionType.TOP_UP,
                new BigDecimal("100.00"), LocalDate.of(2026, 1, 10),
                null, createdBy);

        TransactionEntity saved = transactionRepository.save(tx);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedBy()).isEqualTo(createdBy);
        assertThat(saved.getType()).isEqualTo(TransactionType.TOP_UP);
        assertThat(saved.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void should_persist_payment_transaction_with_reference_payment_id() {
        UUID paymentId = UUID.randomUUID();
        TransactionEntity tx = buildTransaction(
                WALLET_USER, TransactionType.PAYMENT,
                new BigDecimal("-75.00"), LocalDate.of(2026, 2, 15),
                paymentId, UUID.randomUUID());

        TransactionEntity saved = transactionRepository.save(tx);

        assertThat(saved.getReferencePaymentId()).isEqualTo(paymentId);
        assertThat(saved.getAmount()).isNegative();
    }

    @Test
    void should_filter_by_transaction_type_top_up_excludes_payment() {
        transactionRepository.save(buildTransaction(
                WALLET_USER, TransactionType.TOP_UP, new BigDecimal("50.00"),
                LocalDate.of(2026, 1, 1), null, UUID.randomUUID()));
        transactionRepository.save(buildTransaction(
                WALLET_USER, TransactionType.PAYMENT, new BigDecimal("-50.00"),
                LocalDate.of(2026, 1, 2), UUID.randomUUID(), UUID.randomUUID()));

        Specification<TransactionEntity> spec = TransactionSpecification.forUser(WALLET_USER)
                .and(TransactionSpecification.withType(TransactionType.TOP_UP));
        Page<TransactionEntity> page = transactionRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getType()).isEqualTo(TransactionType.TOP_UP);
    }

    @Test
    void should_filter_by_date_range() {
        transactionRepository.save(buildTransaction(
                WALLET_USER, TransactionType.TOP_UP, new BigDecimal("10.00"),
                LocalDate.of(2026, 1, 5), null, UUID.randomUUID()));
        transactionRepository.save(buildTransaction(
                WALLET_USER, TransactionType.TOP_UP, new BigDecimal("20.00"),
                LocalDate.of(2026, 3, 20), null, UUID.randomUUID()));
        transactionRepository.save(buildTransaction(
                WALLET_USER, TransactionType.TOP_UP, new BigDecimal("30.00"),
                LocalDate.of(2026, 6, 1), null, UUID.randomUUID()));

        Specification<TransactionEntity> spec = TransactionSpecification.forUser(WALLET_USER)
                .and(TransactionSpecification.fromDate(LocalDate.of(2026, 2, 1)))
                .and(TransactionSpecification.toDate(LocalDate.of(2026, 4, 30)));

        Page<TransactionEntity> page = transactionRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void should_return_all_transactions_when_no_filters_are_provided() {
        transactionRepository.save(buildTransaction(
                WALLET_USER, TransactionType.TOP_UP, new BigDecimal("50.00"),
                LocalDate.of(2026, 1, 1), null, UUID.randomUUID()));
        transactionRepository.save(buildTransaction(
                WALLET_USER, TransactionType.PAYMENT, new BigDecimal("-30.00"),
                LocalDate.of(2026, 2, 1), UUID.randomUUID(), UUID.randomUUID()));

        Specification<TransactionEntity> spec = TransactionSpecification.forUser(WALLET_USER)
                .and(TransactionSpecification.withType(null))
                .and(TransactionSpecification.fromDate(null))
                .and(TransactionSpecification.toDate(null));

        Page<TransactionEntity> page = transactionRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void should_reject_top_up_with_null_created_by() {
        TransactionEntity tx = new TransactionEntity();
        tx.setId(UUID.randomUUID());
        tx.setWalletUserId(WALLET_USER);
        tx.setType(TransactionType.TOP_UP);
        tx.setAmount(new BigDecimal("50.00"));
        tx.setTransactionDate(LocalDate.of(2026, 1, 1));
        tx.setCreatedBy(null); // violates NOT NULL constraint
        tx.setCreatedAt(Instant.now());

        assertThatThrownBy(() -> transactionRepository.saveAndFlush(tx))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- fixture ---

    private TransactionEntity buildTransaction(
            UUID walletUserId, TransactionType type, BigDecimal amount,
            LocalDate date, UUID referencePaymentId, UUID createdBy) {

        TransactionEntity tx = new TransactionEntity();
        tx.setId(UUID.randomUUID());
        tx.setWalletUserId(walletUserId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setTransactionDate(date);
        tx.setReferencePaymentId(referencePaymentId);
        tx.setCreatedBy(createdBy);
        tx.setCreatedAt(Instant.now());
        return tx;
    }
}
