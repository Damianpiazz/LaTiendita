package com.latiendita.orderService;

import com.latiendita.orderService.exception.AdminRoleRequiredException;
import com.latiendita.orderService.exception.MissingGatewayHeaderException;
import java.util.Arrays;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class CurrentUser {

    public static final String HEADER = "X-User-Id";
    public static final String ROLES_HEADER = "X-User-Roles";
    public static final String ADMIN_ROLE = "admin";

    private CurrentUser() {}

    /**
     * `sub` del token de Authorizer (UUID string, no numérico). Cualquier valor
     * no vacío es válido: la validación de firma e issuer ya la hizo el gateway.
     */
    public static String id() {
        String value = readHeader(HEADER);
        if (value == null || value.isBlank()) {
            throw new MissingGatewayHeaderException();
        }
        return value.trim();
    }

    /** True when the gateway propagated the admin role in {@code X-User-Roles}. */
    public static boolean isAdmin() {
        String roles = readHeaderOrNull(ROLES_HEADER);
        if (roles == null || roles.isBlank()) {
            return false;
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .anyMatch(ADMIN_ROLE::equalsIgnoreCase);
    }

    /** Admin-only endpoints reject callers without the role propagated by the gateway. */
    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new AdminRoleRequiredException();
        }
    }

    private static String readHeader(String name) {
        if (!(RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes servletAttributes)) {
            throw new MissingGatewayHeaderException();
        }
        return servletAttributes.getRequest().getHeader(name);
    }

    private static String readHeaderOrNull(String name) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        return servletAttributes.getRequest().getHeader(name);
    }
}
