package com.jgmedellin.concept_coffee_shop.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig (val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        AntPathRequestMatcher("/api/v1/users/register"),
                        AntPathRequestMatcher("/api/v1/users/login"),
                        AntPathRequestMatcher("/swagger-ui/**"),
                        AntPathRequestMatcher("/v3/api-docs/**")
                    ).permitAll()
                    .requestMatchers(
                        AntPathRequestMatcher("/api/v1/users/*", "PUT"),
                        AntPathRequestMatcher("/api/v1/products", "GET"),
                        AntPathRequestMatcher("/api/v1/orders/history", "GET"),
                        AntPathRequestMatcher("/api/v1/orders/*", "GET"),
                    ).hasAnyRole("USER", "ADMIN", "SUPER")
                    .requestMatchers(
                        AntPathRequestMatcher("/api/v1/users/*", "GET"),
                        AntPathRequestMatcher("/api/v1/users", "GET"),
                        AntPathRequestMatcher("/api/v1/products/*", "PATCH"),
                        AntPathRequestMatcher("/api/v1/products", "POST"),
                        AntPathRequestMatcher("/api/v1/orders", "GET"),
                        AntPathRequestMatcher("/api/v1/orders/*", "PATCH"),
                    ).hasAnyRole("ADMIN", "SUPER")
                    .requestMatchers(
                        AntPathRequestMatcher("/api/v1/users/changeRole", "PATCH"),
                        AntPathRequestMatcher("/api/v1/users/*", "DELETE"),
                        AntPathRequestMatcher("/api/v1/products/*", "DELETE"),
                        AntPathRequestMatcher("/api/v1/orders/*", "DELETE"),
                    ).hasRole("SUPER")
                    .requestMatchers(
                        AntPathRequestMatcher("/api/v1/orders", "POST"),
                    ).hasRole("USER")
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}