package ayd2.p2b.wallet_service_api;

import ayd2.p2b.wallet_service_api.core.security.AuthenticatedUser;
import ayd2.p2b.wallet_service_api.core.security.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class WithMockJwtFactory implements WithSecurityContextFactory<WithMockJwt> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwt annotation) {
        Set<Role> roles = Arrays.stream(annotation.roles())
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        AuthenticatedUser principal = AuthenticatedUser.builder()
                .userId(UUID.fromString(annotation.userId()))
                .email(annotation.email())
                .roles(roles)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                        .collect(Collectors.toList())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
