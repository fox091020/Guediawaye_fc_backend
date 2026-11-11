package sn.guediawaye.backend.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAccountService {

    private static final int MAX_SITE_ADMINS = 3;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private final AdminAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(AdminAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AdminAccountResponse findCurrentAccount(String username) {
        return AdminAccountResponse.from(findAccount(username));
    }

    @Transactional(readOnly = true)
    public List<AdminAccountResponse> findSiteAdmins() {
        return repository.findAllByRoleOrderByCreatedAtAsc(AdminRole.SITE_ADMIN).stream()
                .map(AdminAccountResponse::from)
                .toList();
    }

    @Transactional
    public AdminAccountResponse createSiteAdmin(AdminAccountCreateRequest request) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet identifiant est déjà utilisé.");
        }
        if (repository.countByRole(AdminRole.SITE_ADMIN) >= MAX_SITE_ADMINS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La limite de trois profils administrateur est atteinte.");
        }
        validatePassword(request.password());
        AdminAccount account = new AdminAccount(
                username,
                request.displayName().trim(),
                passwordEncoder.encode(request.password()),
                AdminRole.SITE_ADMIN,
                false,
                false);
        return AdminAccountResponse.from(repository.save(account));
    }

    @Transactional
    public AdminAccountResponse setSiteAdminEnabled(long id, boolean enabled) {
        AdminAccount account = repository.findById(id)
                .filter(candidate -> candidate.getRole() == AdminRole.SITE_ADMIN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil administrateur introuvable."));
        account.setEnabled(enabled);
        return AdminAccountResponse.from(repository.save(account));
    }

    @Transactional
    public AdminAccountResponse updateSiteAdmin(long id, AdminAccountUpdateRequest request) {
        AdminAccount account = repository.findById(id)
                .filter(candidate -> candidate.getRole() == AdminRole.SITE_ADMIN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil administrateur introuvable."));
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        repository.findByUsernameIgnoreCase(username)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet identifiant est déjà utilisé.");
                });
        account.setUsername(username);
        account.setDisplayName(request.displayName().trim());
        if (request.password() != null && !request.password().isBlank()) {
            validatePassword(request.password());
            account.setPasswordHash(passwordEncoder.encode(request.password()));
            account.setPasswordChangeRequired(true);
            account.setFailedLoginAttempts(0);
            account.setLockedUntil(null);
        }
        return AdminAccountResponse.from(repository.save(account));
    }

    @Transactional
    public void deleteSiteAdmin(long id) {
        AdminAccount account = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil administrateur introuvable."));
        if (account.isPermanent() || account.getRole() != AdminRole.SITE_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce compte permanent ne peut pas être supprimé.");
        }
        repository.delete(account);
    }

    @Transactional
    public boolean recordFailedLogin(String username) {
        return repository.findByUsernameIgnoreCase(username).map(account -> {
            if (!account.isEnabled() || !account.isAccountNonLocked()) {
                return !account.isAccountNonLocked();
            }
            int failedAttempts = account.getFailedLoginAttempts() + 1;
            account.setFailedLoginAttempts(failedAttempts >= MAX_FAILED_ATTEMPTS ? 0 : failedAttempts);
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                account.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
            }
            repository.save(account);
            return failedAttempts >= MAX_FAILED_ATTEMPTS;
        }).orElse(false);
    }

    @Transactional
    public void recordSuccessfulLogin(String username) {
        AdminAccount account = findAccount(username);
        account.setFailedLoginAttempts(0);
        account.setLockedUntil(null);
        account.setLastLoginAt(Instant.now());
        repository.save(account);
    }

    @Transactional
    public AdminAccountResponse changePassword(String username, ChangePasswordRequest request) {
        AdminAccount account = findAccount(username);
        if (!passwordEncoder.matches(request.currentPassword(), account.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mot de passe actuel est incorrect.");
        }
        validatePassword(request.newPassword());
        if (passwordEncoder.matches(request.newPassword(), account.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nouveau mot de passe doit être différent de l'ancien.");
        }
        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        account.setPasswordChangeRequired(false);
        return AdminAccountResponse.from(repository.save(account));
    }

    @Transactional
    public AdminAccount initializeDeveloper(String username, String initialPassword) {
        if (repository.existsByRole(AdminRole.DEVELOPER)) {
            return repository.findFirstByRole(AdminRole.DEVELOPER)
                    .orElseThrow(() -> new IllegalStateException("Le profil développeur n'a pas pu être chargé."));
        }
        if (initialPassword == null || initialPassword.isBlank()) {
            throw new IllegalStateException(
                    "Configurez GFC_DEVELOPER_INITIAL_PASSWORD pour créer le compte développeur permanent.");
        }
        AdminAccount account = new AdminAccount(
                username.trim().toLowerCase(Locale.ROOT),
                username.trim(),
                passwordEncoder.encode(initialPassword),
                AdminRole.DEVELOPER,
                true,
                true);
        return repository.save(account);
    }

    public void validatePassword(String password) {
        if (password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72
                || !UPPERCASE.matcher(password).find()
                || !LOWERCASE.matcher(password).find()
                || !DIGIT.matcher(password).find()
                || !SPECIAL.matcher(password).find()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le mot de passe doit contenir 12 caractères minimum, au plus 72 octets, une majuscule, une minuscule, un chiffre et un caractère spécial.");
        }
    }

    private AdminAccount findAccount(String username) {
        return repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte administrateur introuvable."));
    }
}
