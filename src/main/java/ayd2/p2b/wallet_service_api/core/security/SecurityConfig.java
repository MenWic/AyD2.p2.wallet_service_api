package ayd2.p2b.wallet_service_api.core.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint
    ) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable) //Ajustar segun requiera wallet_service_api
                .formLogin(AbstractHttpConfigurer::disable) //Ajustar segun requiera wallet_service_api
                .logout(AbstractHttpConfigurer::disable) //Ajustar segun requiera wallet_service_api
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        .requestMatchers(HttpMethod.POST, "/wallets").authenticated()
                        .requestMatchers(HttpMethod.GET, "/wallet/balance", "/wallet/transactions").authenticated()
                        .requestMatchers(HttpMethod.POST, "/wallet/top-up").authenticated()

                        .requestMatchers(HttpMethod.POST, "/payments/register").authenticated()
                        .requestMatchers(HttpMethod.GET, "/payments", "/payments/*").authenticated()

                        .requestMatchers(HttpMethod.GET, "/system/config").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/system/config").authenticated()

                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        return username -> {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
                    "UserDetailsService is not used by wallet-service"
            );
        };
    }
}
