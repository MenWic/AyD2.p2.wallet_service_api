package ayd2.p2b.wallet_service_api.feature.systemconfig.infrastructure.persistence.repository;

import ayd2.p2b.wallet_service_api.feature.systemconfig.infrastructure.persistence.entity.SystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigJpaRepository extends JpaRepository<SystemConfigEntity, Integer> {
}
