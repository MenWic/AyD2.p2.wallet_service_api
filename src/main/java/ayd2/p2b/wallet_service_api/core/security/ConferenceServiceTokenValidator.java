package ayd2.p2b.wallet_service_api.core.security;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.core.properties.ConferenceIntegrationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Validates the X-Service-Token header sent by conference-service on internal
 * calls.
 * Responsibility is narrowly scoped: compare the header value against the
 * configured token.
 * No payment domain or persistence logic lives here.
 */
@Component
@RequiredArgsConstructor
public class ConferenceServiceTokenValidator {

  private final ConferenceIntegrationProperties conferenceIntegrationProperties;

  /**
   * Validates that {@code headerToken} matches the configured service token.
   *
   * @param headerToken value of the X-Service-Token header (may be null if
   *                    missing)
   * @throws ApiException 403 if the header is absent, blank, or does not match
   *                      the configured token
   * @throws ApiException 500 if the server-side token is not configured
   *                      (misconfiguration)
   */
  public void validate(String headerToken) {
    String configured = conferenceIntegrationProperties.getServiceToken();
    if (configured == null || configured.isBlank()) {
      throw new ApiException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "system.internal_error",
          "Service-to-service token is not configured on this server");
    }
    if (headerToken == null || headerToken.isBlank() || !configured.equals(headerToken)) {
      throw new ApiException(
          HttpStatus.FORBIDDEN,
          "auth.forbidden",
          "Missing or invalid X-Service-Token");
    }
  }
}
