package ayd2.p2b.wallet_service_api.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "integration.conference")
public class ConferenceIntegrationProperties {

  /**
   * Shared secret token that conference-service must include as the
   * X-Service-Token header when calling Wallet internal endpoints
   * (for example POST /payments/register and GET /internal/reports/*).
   * Set via environment variable CONFERENCE_SERVICE_TOKEN.
   * Leave blank locally to disable the endpoint until properly configured.
   */
  private String serviceToken;
}
