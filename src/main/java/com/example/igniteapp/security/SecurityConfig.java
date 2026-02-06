package com.example.igniteapp.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

/**
 * Режимы:
 *  - app.security.mode=none      -> всё открыто
 *  - app.security.mode=keycloak  -> oauth2Login, защита ВСЕГО, включая /
 *  - app.security.mode=fakelogin -> formLogin, принимает любые логин/пароль
 */
@Configuration
public class SecurityConfig {

    // -------------------------
    // MODE: none (default)
    // -------------------------
    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "mode", havingValue = "none", matchIfMissing = true)
    SecurityFilterChain openAll(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests(a -> a.anyRequest().permitAll());
        return http.build();
    }

    // -------------------------
    // MODE: keycloak (OIDC login)
    // -------------------------
    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "mode", havingValue = "keycloak")
    SecurityFilterChain keycloakLogin(HttpSecurity http) throws Exception {
        http
                // Для простоты: API и UI через сессию после oauth2Login
                .csrf().disable()
                .authorizeRequests(a -> a
                        // если хочешь оставить Swagger открытым, раскомментируй:
                        // .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .antMatchers("/api/health", "/api/ready").permitAll()
                        .antMatchers("/api/shutdown").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .logout(l -> l.logoutSuccessUrl("/"));

        return http.build();
    }

    // -------------------------
    // MODE: fakelogin (dev only)
    // -------------------------
    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "mode", havingValue = "fakelogin")
    SecurityFilterChain fakeLogin(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests(a -> a
                        // страницу логина Spring Security оставляем доступной
                        .antMatchers("/api/health","/login", "/error", "/api/ready").permitAll()
                        .antMatchers("/api/shutdown").authenticated()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(acceptAnyCredentialsProvider())
                .formLogin(Customizer.withDefaults())
                .logout(l -> l.logoutSuccessUrl("/login"))
                .httpBasic();

        return http.build();
    }

    /**
     * AuthenticationProvider, который "верит на слово" любому логину/паролю.
     * ВНИМАНИЕ: использовать только на тестовых стендах.
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "mode", havingValue = "fakelogin")
    AuthenticationProvider acceptAnyCredentialsProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = (authentication.getName() == null || authentication.getName().isBlank())
                        ? "test-user"
                        : authentication.getName();

                // Любой пароль принимаем
                return new UsernamePasswordAuthenticationToken(
                        username,
                        authentication.getCredentials(),
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }
}
