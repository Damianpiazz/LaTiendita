package com.latiendita.shipmentService;

import com.latiendita.shipmentService.exception.MissingGatewayHeaderException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class CurrentUser {

    public static final String HEADER = "X-User-Id";

    private CurrentUser() {
    }

    /**
     * The Authorizer {@code sub}. The gateway already validated the token, so any
     * non-blank value is authoritative for this service.
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
