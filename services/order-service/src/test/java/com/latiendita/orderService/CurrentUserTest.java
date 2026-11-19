package com.latiendita.orderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.latiendita.orderService.exception.AdminRoleRequiredException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class CurrentUserTest {

    @AfterEach
    void resetCurrentUser() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldDetectAdminRoleAmongSeveralRolesIgnoringCase() {
        setHeaders("1", "cliente, ADMIN ,vendedor");

        assertThat(CurrentUser.isAdmin()).isTrue();
        CurrentUser.requireAdmin();
    }

    @Test
    void shouldRejectAdminEndpointsWithoutAdminRole() {
        setHeaders("1", "cliente");

        assertThat(CurrentUser.isAdmin()).isFalse();
        assertThatThrownBy(CurrentUser::requireAdmin)
                .isInstanceOf(AdminRoleRequiredException.class);
    }

    @Test
    void shouldRejectAdminEndpointsWithoutRolesHeader() {
        setHeaders("1", null);

        assertThatThrownBy(CurrentUser::requireAdmin)
                .isInstanceOf(AdminRoleRequiredException.class);
    }

    private void setHeaders(String userId, String roles) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", userId);
        if (roles != null) {
            request.addHeader("X-User-Roles", roles);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
