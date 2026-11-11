package sn.guediawaye.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class AdminAccountStatusFilterTests {

    private final AdminAccountRepository repository = org.mockito.Mockito.mock(AdminAccountRepository.class);
    private final AdminAccountStatusFilter filter = new AdminAccountStatusFilter(repository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsWriteRequestsFromBlockedAccounts() throws Exception {
        AdminAccount blocked = new AdminAccount("manager", "Manager", "hash", AdminRole.SITE_ADMIN, false, false);
        blocked.setEnabled(false);
        when(repository.findByUsernameIgnoreCase("manager")).thenReturn(Optional.of(blocked));
        authenticate("manager");
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/shop");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void keepsPublicReadsAvailableWithoutCheckingAdminStatus() throws Exception {
        authenticate("manager");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/shop");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(repository, never()).findByUsernameIgnoreCase("manager");
        assertEquals(request, chain.getRequest());
    }

    @Test
    void forcesPasswordRotationBeforeAdminWrites() throws Exception {
        AdminAccount account = new AdminAccount("manager", "Manager", "hash", AdminRole.SITE_ADMIN, false, true);
        when(repository.findByUsernameIgnoreCase("manager")).thenReturn(Optional.of(account));
        authenticate("manager");
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/shop");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        username, "",
                        List.of(new SimpleGrantedAuthority("ROLE_SITE_ADMIN"))));
    }
}
