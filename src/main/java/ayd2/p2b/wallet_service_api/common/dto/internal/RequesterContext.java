package ayd2.p2b.wallet_service_api.common.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
@AllArgsConstructor
public class RequesterContext {
    UUID userId;
    Set<String> roles;

    public static RequesterContext of(UUID userId, Set<String> roles) {
        return new RequesterContext(userId, roles);
    }
}
