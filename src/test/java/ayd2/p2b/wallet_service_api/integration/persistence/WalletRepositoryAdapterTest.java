package ayd2.p2b.wallet_service_api.integration.persistence;

import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.entity.WalletEntity;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.repository.WalletJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
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
class WalletRepositoryAdapterTest {

    @Autowired
    private WalletJpaRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void should_create_wallet_with_zero_balance() {
        WalletEntity entity = buildWallet(UUID.randomUUID(), BigDecimal.ZERO);
        WalletEntity saved = repository.save(entity);

        Optional<WalletEntity> found = repository.findByUserId(saved.getUserId());
        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void should_update_balance_after_top_up() {
        UUID userId = UUID.randomUUID();
        WalletEntity entity = buildWallet(userId, BigDecimal.ZERO);
        repository.save(entity);

        WalletEntity loaded = repository.findByUserId(userId).orElseThrow();
        loaded.setBalance(new BigDecimal("150.00"));
        loaded.setUpdatedAt(Instant.now());
        repository.save(loaded);

        WalletEntity updated = repository.findByUserId(userId).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void should_reject_negative_balance_via_db_constraint() {
        WalletEntity entity = buildWallet(UUID.randomUUID(), new BigDecimal("-1.00"));
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_increment_version_on_update() {
        UUID userId = UUID.randomUUID();
        WalletEntity entity = buildWallet(userId, BigDecimal.ZERO);
        WalletEntity saved = repository.save(entity);
        long initialVersion = saved.getVersion();

        saved.setBalance(new BigDecimal("50.00"));
        saved.setUpdatedAt(Instant.now());
        WalletEntity updated = repository.saveAndFlush(saved);

        assertThat(updated.getVersion()).isGreaterThan(initialVersion);
    }

    @Test
    void should_find_wallet_by_user_id() {
        UUID userId = UUID.randomUUID();
        repository.save(buildWallet(userId, new BigDecimal("200.00")));

        Optional<WalletEntity> found = repository.findByUserId(userId);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getBalance()).isEqualByComparingTo("200.00");
    }

    // --- fixture ---

    private WalletEntity buildWallet(UUID userId, BigDecimal balance) {
        WalletEntity entity = new WalletEntity();
        entity.setUserId(userId);
        entity.setBalance(balance);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
