package com.latiendita.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jwt")
public record JwtProperties(String secret, String issuer, String rolesClaim) {

    public JwtProperties {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("jwt.secret debe tener al menos 32 caracteres (HS256)");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException(
                    "jwt.issuer es obligatorio (debe coincidir con AUTHORIZER_URL de Authorizer)");
        }
        // Authorizer emite "roles"; el claim es configurable para poder migrar de IdP.
        if (rolesClaim == null || rolesClaim.isBlank()) {
            rolesClaim = "roles";
        }
    }
}
