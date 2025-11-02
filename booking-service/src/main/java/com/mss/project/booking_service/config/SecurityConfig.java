package com.mss.project.booking_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private final CustomJwtDecoder jwtDecoder;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final List<Map.Entry<String, HttpMethod>> SECURED_URLS = List.of(
            Map.entry("/payment", HttpMethod.POST),
            Map.entry("/payment/*", HttpMethod.GET),
            Map.entry("/payment/*", HttpMethod.PUT),
            Map.entry("/payment/*", HttpMethod.DELETE),
            Map.entry("/ticket", HttpMethod.POST),
            Map.entry("/ticket/*", HttpMethod.GET),
            Map.entry("/ticket/*", HttpMethod.PUT),
            Map.entry("/ticket/*", HttpMethod.DELETE),
            Map.entry("/tickets/*/cancel", HttpMethod.PUT),
            Map.entry("/booking", HttpMethod.POST),
            Map.entry("/booking/*", HttpMethod.GET),
            Map.entry("/booking/*", HttpMethod.PUT),
            Map.entry("/booking/*", HttpMethod.DELETE),
            Map.entry("/booking/history", HttpMethod.GET),
            // Admin endpoints - secured with ADMIN role
            Map.entry("/admin/**", HttpMethod.GET),
            Map.entry("/admin/**", HttpMethod.POST),
            Map.entry("/admin/**", HttpMethod.PUT),
            Map.entry("/admin/**", HttpMethod.DELETE));

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> {
                    SECURED_URLS.forEach(
                            entry -> authorize.requestMatchers(entry.getValue(), entry.getKey()).authenticated());
                    authorize.anyRequest().permitAll();
                });

        http.oauth2ResourceServer(resourceServer -> resourceServer
                .jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(jwtAuthenticationEntryPoint));

        http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(request -> {
            var cors = new CorsConfiguration();
            cors.setAllowedOrigins(List.of(allowedOrigins.split(",")));
            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
            cors.setAllowedHeaders(List.of("*"));
            cors.setAllowCredentials(true);
            return cors;
        }));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
