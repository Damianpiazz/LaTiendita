package com.latiendita.paymentService;

import com.latiendita.paymentService.exception.MissingGatewayHeaderException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class CurrentUser {

    public static final String HEADER = "X-User-Id";

    private CurrentUser() {}

    /**
     * `sub` del token de Authorizer (UUID string, no numérico). Cualquier valor
     * no vacío es válido: la validación de firma e issuer ya la hizo el gateway.
     */
    public static String id() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            throw new MissingGatewayHeaderException();
        }
        String value = servletAttributes.getRequest().getHeader(HEADER);
        if (value == null || value.isBlank()) {
            throw new MissingGatewayHeaderException();
        }
        return value.trim();
    }
}
