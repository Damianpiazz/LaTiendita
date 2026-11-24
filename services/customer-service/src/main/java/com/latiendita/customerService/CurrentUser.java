package com.latiendita.customerService;

import com.latiendita.customerService.exception.MissingGatewayHeaderException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Reads the identity headers injected by the gateway. The gateway has already
 * validated the JWT (signature and issuer), so the {@code sub} claimed in
 * {@code X-User-Id} is authoritative and client-supplied identity fields are
 * never trusted.
 */
public final class CurrentUser {

    public static final String ID_HEADER = "X-User-Id";
    public static final String EMAIL_HEADER = "X-User-Email";

    private CurrentUser() {
    }

    /** The Authorizer {@code sub}; never null for a request that passed the gateway. */
    public static String id() {
        String value = readHeader(ID_HEADER);
        if (value == null || value.isBlank()) {
            throw new MissingGatewayHeaderException();
        }
        return value.trim();
    }

    /** The Authorizer email; may be null when the gateway did not forward it. */
    public static String email() {
        String value = readHeader(EMAIL_HEADER);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String readHeader(String name) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            throw new MissingGatewayHeaderException();
        }
        return servletAttributes.getRequest().getHeader(name);
    }
}
