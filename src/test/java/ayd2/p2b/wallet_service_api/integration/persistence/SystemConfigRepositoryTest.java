package ayd2.p2b.wallet_service_api.integration.persistence;

import ayd2.p2b.wallet_service_api.feature.systemconfig.infrastructure.persistence.entity.SystemConfigEntity;
import ayd2.p2b.wallet_service_api.feature.systemconfig.infrastructure.persistence.repository.SystemConfigJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ayd2.p2b.wallet_service_api.TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class SystemConfigRepositoryTest {

    @Autowired
    private SystemConfigJpaRepository repository;

    @Test
    void should_find_singleton_seeded_by_flyway() {
        Optional<SystemConfigEntity> result = repository.findById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getCommissionPercent()).isEqualByComparingTo("10.00");
    }

    @Test
    void should_update_singleton_without_inserting_new_row() {
        SystemConfigEntity entity = repository.findById(1).orElseThrow();
        entity.setCommissionPercent(new BigDecimal("20.00"));
        entity.setUpdatedBy(UUID.randomUUID());
        entity.setUpdatedAt(Instant.now());

        repository.saveAndFlush(entity);

        long count = repository.count();
        Optional<SystemConfigEntity> updated = repository.findById(1);

        assertThat(count).isEqualTo(1);
        assertThat(updated).isPresent();
        assertThat(updated.get().getCommissionPercent()).isEqualByComparingTo("20.00");
    }
}
