package com.latiendita.cartService;

import com.latiendita.cartService.exception.MissingGatewayHeaderException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class CurrentUser {

    public static final String HEADER = "X-User-Id";

    private CurrentUser() {}

    /**
     * `sub` claim of the Authorizer token (UUID string, not numeric). Any
     * non-blank value is accepted: signature and issuer were verified upstream.
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
