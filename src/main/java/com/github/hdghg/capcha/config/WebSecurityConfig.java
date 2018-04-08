package com.github.hdghg.capcha.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Stub reactive security configuration.
 * 1. Enables WebFlux security
 * 2. Created stub users
 * 3. Guards /uploadPage from non-admins
 */
@EnableWebFluxSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange()
                .pathMatchers("/uploadPage").hasRole("ADMIN")
                .anyExchange().permitAll()
                .and()
            .formLogin();
        return http.build();
    }
}
