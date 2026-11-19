package com.latiendita.reviewService;

import com.latiendita.reviewService.exception.AdminRoleRequiredException;
import com.latiendita.reviewService.exception.MissingGatewayHeaderException;
import java.util.Arrays;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Reads the identity headers injected by the gateway. The gateway has already
 * validated the JWT, so {@code X-User-Id} is authoritative.
 */
public final class CurrentUser {

    public static final String ID_HEADER = "X-User-Id";
    public static final String ROLES_HEADER = "X-User-Roles";
    public static final String ADMIN_ROLE = "admin";

    private CurrentUser() {
    }

    public static String id() {
        String value = readHeader(ID_HEADER);
        if (value == null || value.isBlank()) {
            throw new MissingGatewayHeaderException();
        }
        return value.trim();
    }

    /** Moderation requires the admin role propagated by the gateway. */
    public static void requireAdmin() {
        String roles = readHeader(ROLES_HEADER);
        if (roles == null || roles.isBlank()) {
            throw new AdminRoleRequiredException();
        }
        boolean admin = Arrays.stream(roles.split(","))
                .map(String::trim)
                .anyMatch(ADMIN_ROLE::equalsIgnoreCase);
        if (!admin) {
            throw new AdminRoleRequiredException();
        }
    }

    private static String readHeader(String name) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            throw new MissingGatewayHeaderException();
        }
        return servletAttributes.getRequest().getHeader(name);
    }
}
