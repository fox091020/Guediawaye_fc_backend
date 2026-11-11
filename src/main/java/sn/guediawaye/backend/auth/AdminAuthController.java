package sn.guediawaye.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AdminAccountService accountService;

    public AdminAuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            AdminAccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.accountService = accountService;
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getToken());
    }

    @GetMapping("/me")
    public ResponseEntity<AdminAccountResponse> currentAccount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() instanceof String) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(accountService.findCurrentAccount(authentication.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username().trim(), request.password()));
        } catch (AuthenticationException exception) {
            boolean lockedByThisAttempt = accountService.recordFailedLogin(request.username().trim());
            HttpStatus status = exception instanceof LockedException || lockedByThisAttempt
                    ? HttpStatus.LOCKED
                    : HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status).body(
                    new AuthError(status == HttpStatus.LOCKED
                            ? "Compte temporairement bloqué. Réessayez dans 15 minutes."
                            : "Identifiant ou mot de passe invalide."));
        }

        accountService.recordSuccessfulLogin(authentication.getName());
        if (servletRequest.getSession(false) != null) {
            servletRequest.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);
        return ResponseEntity.ok(accountService.findCurrentAccount(authentication.getName()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public AdminAccountResponse changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        return accountService.changePassword(authentication.getName(), request);
    }

    @GetMapping("/admins")
    public List<AdminAccountResponse> siteAdmins() {
        return accountService.findSiteAdmins();
    }

    @PostMapping("/admins")
    public AdminAccountResponse createSiteAdmin(@Valid @RequestBody AdminAccountCreateRequest request) {
        return accountService.createSiteAdmin(request);
    }

    @PutMapping("/admins/{id}")
    public AdminAccountResponse updateSiteAdmin(
            @PathVariable long id,
            @Valid @RequestBody AdminAccountUpdateRequest request) {
        return accountService.updateSiteAdmin(id, request);
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteSiteAdmin(@PathVariable long id) {
        accountService.deleteSiteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admins/{id}/enabled")
    public AdminAccountResponse setSiteAdminEnabled(
            @PathVariable long id,
            @RequestBody AdminAccountEnabledRequest request) {
        return accountService.setSiteAdminEnabled(id, request.enabled());
    }

    public record AuthError(String message) {
    }
}
