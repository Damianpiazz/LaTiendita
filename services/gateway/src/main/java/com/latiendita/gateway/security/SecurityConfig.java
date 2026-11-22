package com.latiendita.gateway.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
public class SecurityConfig {

    /**
     * Rol de Authorizer para el catálogo (--roles=admin,cliente en
     * runtime/docker/compose.yaml). Se compara sin el prefijo ROLE_ que agrega
     * el converter: hasRole("admin") exige la authority "ROLE_admin".
     */
    public static final String ADMIN_ROLE = "admin";

    public static final String[] PUBLIC_PATHS = {
            "/api/auth/**",
            "/api/webhooks/**",
            "/actuator/health/**",
            "/actuator/prometheus",
            "/actuator/info"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtProperties properties)
            throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .requestMatchers("/api/products/admin/**").hasRole(ADMIN_ROLE)
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**")
                    .permitAll()
                // Las lecturas de reseñas son públicas (sección "Reviews" del detalle
                // de producto); la moderación administrativa exige el rol admin.
                .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                .requestMatchers("/api/admin/reviews/**").hasRole(ADMIN_ROLE)
                // Perfil propio y autoprovision del cliente: cualquier usuario autenticado.
                // Van ANTES del listado admin para que /me no caiga en /api/customers/*.
                .requestMatchers(HttpMethod.POST, "/api/customers").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/customers/me").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/customers/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/customers/me/newsletter").authenticated()
                // Listado y consulta por id de customers son operaciones de administración.
                .requestMatchers(HttpMethod.GET, "/api/customers").hasRole(ADMIN_ROLE)
                // El prefijo /admin es explícito y no debe caer en /api/customers/*.
                .requestMatchers("/api/customers/admin/**").hasRole(ADMIN_ROLE)
                .requestMatchers(HttpMethod.GET, "/api/customers/*").hasRole(ADMIN_ROLE)
                .requestMatchers("/api/admin/promotions/**").hasRole(ADMIN_ROLE)
                // Las consultas globales del admin de pedidos no son del cliente actual.
                .requestMatchers("/api/orders/admin/**").hasRole(ADMIN_ROLE)
                .requestMatchers("/api/payments/admin/**").hasRole(ADMIN_ROLE)
                .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**")
                    .hasRole(ADMIN_ROLE)
                .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**")
                    .hasRole(ADMIN_ROLE)
                .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**")
                    .hasRole(ADMIN_ROLE)
                .anyRequest().authenticated())
            .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt
                .decoder(jwtDecoder(properties))
                .jwtAuthenticationConverter(jwtAuthenticationConverter(properties))));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(properties.allowedOrigins());
        config.setAllowedMethods(properties.allowedMethods());
        config.setAllowedHeaders(properties.allowedHeaders());
        config.setAllowCredentials(properties.allowCredentials());
        config.setMaxAge(properties.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    NimbusJwtDecoder jwtDecoder(JwtProperties properties) {
        SecretKeySpec key = new SecretKeySpec(
                properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256).build();
        // Firma + exp/iat + issuer estricto: el token debe venir de Authorizer
        // (iss = AUTHORIZER_URL), no de cualquier JWT firmado con el mismo secreto.
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }

    /**
     * Claim de roles (Authorizer: "roles", configurable) → authorities con
     * prefijo ROLE_. Sin el claim, el token queda autenticado sin roles.
     */
    private static JwtAuthenticationConverter jwtAuthenticationConverter(
            JwtProperties properties) {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(properties.rolesClaim());
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
