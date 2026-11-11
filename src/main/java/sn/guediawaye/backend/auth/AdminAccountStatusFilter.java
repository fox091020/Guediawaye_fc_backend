package sn.guediawaye.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class AdminAccountStatusFilter extends OncePerRequestFilter {

    private final AdminAccountRepository repository;

    public AdminAccountStatusFilter(AdminAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean protectedRequest = request.getRequestURI().startsWith("/api/auth/")
                || !HttpMethod.GET.matches(request.getMethod());
        if (protectedRequest
                && authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<AdminAccount> account = repository.findByUsernameIgnoreCase(authentication.getName());
            if (account.filter(AdminAccount::isEnabled).filter(AdminAccount::isAccountNonLocked).isEmpty()) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Le compte est bloqué ou indisponible.");
                return;
            }
            boolean passwordChangeRequest = request.getRequestURI().equals("/api/auth/password");
            if (account.map(AdminAccount::isPasswordChangeRequired).orElse(false)
                    && !passwordChangeRequest
                    && !request.getRequestURI().equals("/api/auth/me")
                    && !request.getRequestURI().equals("/api/auth/csrf")
                    && !request.getRequestURI().equals("/api/auth/logout")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Le changement du mot de passe est obligatoire.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
