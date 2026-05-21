package ayd2.p2b.wallet_service_api;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtFactory.class)
public @interface WithMockJwt {
    String userId() default "00000000-0000-0000-0000-000000000001";
    String email() default "admin@test.com";
    String[] roles() default {"SYSTEM_ADMIN"};
}
