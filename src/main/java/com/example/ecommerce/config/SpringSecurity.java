package com.example.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;


@Configuration
public class SpringSecurity {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/auth/**",
                                "/health-check",
                                "/api/products",
                                "/api/products/*/reviews",
                                "/api/seller/apply",
                                "/api/seller/verify-otp",
                                "/api/categories/all",
                                "/api/products/all/best-seller",
                                "/api/products/all/featured"
                        ).permitAll()

                        // Admin-only routes
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // Seller-only routes
                        .requestMatchers("/api/seller/**").hasAuthority("SELLER")

                        // Authenticated routes
                        .requestMatchers("/api/**").authenticated()

                        // Any other requests
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtValidator(), BasicAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration configuration = new CorsConfiguration();

            configuration.setAllowedHeaders(Collections.singletonList("*"));
            configuration.setAllowedMethods(Collections.singletonList("*"));
            configuration.setAllowedOrigins(Collections.singletonList("https://gizmodeckco.vercel.app/"));
            configuration.setAllowCredentials(true);
            configuration.setExposedHeaders(Collections.singletonList("*"));
            configuration.setMaxAge(3600L);

            return configuration;
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}