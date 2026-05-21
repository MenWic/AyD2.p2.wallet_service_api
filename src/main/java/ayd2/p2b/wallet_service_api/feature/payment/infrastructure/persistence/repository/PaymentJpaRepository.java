package ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.repository;

import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID>, JpaSpecificationExecutor<PaymentEntity> {

    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);
}
