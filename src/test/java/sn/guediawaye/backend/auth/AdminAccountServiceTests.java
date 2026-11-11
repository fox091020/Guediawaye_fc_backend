package sn.guediawaye.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AdminAccountServiceTests {

    private AdminAccountRepository repository;
    private AdminAccountService service;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        repository = org.mockito.Mockito.mock(AdminAccountRepository.class);
        passwordEncoder = new BCryptPasswordEncoder(4);
        service = new AdminAccountService(repository, passwordEncoder);
    }

    @Test
    void createsSiteAdminWithHashedPasswordAndNormalizedUsername() {
        when(repository.existsByUsernameIgnoreCase("site.admin")).thenReturn(false);
        when(repository.countByRole(AdminRole.SITE_ADMIN)).thenReturn(0L);
        when(repository.save(any(AdminAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminAccountResponse response = service.createSiteAdmin(
                new AdminAccountCreateRequest("Site.Admin", "Gestionnaire", "Strong-password-28!"));

        assertEquals("site.admin", response.username());
        assertEquals(AdminRole.SITE_ADMIN, response.role());
        assertFalse(response.permanent());
        assertFalse(response.passwordChangeRequired());
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(
                account -> !account.getPassword().equals("Strong-password-28!")
                        && passwordEncoder.matches("Strong-password-28!", account.getPassword())));
    }

    @Test
    void refusesToCreateMoreThanThreeSiteAdmins() {
        when(repository.existsByUsernameIgnoreCase("fourth")).thenReturn(false);
        when(repository.countByRole(AdminRole.SITE_ADMIN)).thenReturn(3L);

        assertThrows(ResponseStatusException.class, () -> service.createSiteAdmin(
                new AdminAccountCreateRequest("fourth", "Gestionnaire", "Strong-password-28!")));
        verify(repository, never()).save(any(AdminAccount.class));
    }

    @Test
    void rejectsPasswordsThatDoNotMeetThePolicy() {
        assertThrows(ResponseStatusException.class, () -> service.validatePassword("weak"));
    }

    @Test
    void locksAccountAfterFiveFailedAttempts() {
        AdminAccount account = new AdminAccount("manager", "Manager", "hash", AdminRole.SITE_ADMIN, false, false);
        when(repository.findByUsernameIgnoreCase("manager")).thenReturn(Optional.of(account));
        when(repository.save(any(AdminAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        for (int attempt = 1; attempt < 5; attempt++) {
            assertFalse(service.recordFailedLogin("manager"));
        }
        assertTrue(service.recordFailedLogin("manager"));
        assertNotNull(account.getLockedUntil());
        assertFalse(account.isAccountNonLocked());
    }

    @Test
    void initializesDeveloperAsPermanentAndRequiresPasswordRotation() {
        when(repository.existsByRole(AdminRole.DEVELOPER)).thenReturn(false);
        when(repository.save(any(AdminAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminAccount account = service.initializeDeveloper("bmd", "temporary-secret");

        assertEquals("bmd", account.getUsername());
        assertEquals(AdminRole.DEVELOPER, account.getRole());
        assertTrue(account.isPermanent());
        assertTrue(account.isPasswordChangeRequired());
        assertTrue(passwordEncoder.matches("temporary-secret", account.getPassword()));
    }

    @Test
    void requiresAnEnvironmentPasswordWhenBootstrappingDeveloper() {
        when(repository.existsByRole(AdminRole.DEVELOPER)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> service.initializeDeveloper("bmd", ""));
        verify(repository, never()).save(any(AdminAccount.class));
    }

    @Test
    void updatesSiteAdminAndRequiresRotationWhenResettingPassword() {
        AdminAccount account = new AdminAccount("manager", "Ancien nom", "old-hash", AdminRole.SITE_ADMIN, false, false);
        when(repository.findById(12L)).thenReturn(Optional.of(account));
        when(repository.findByUsernameIgnoreCase("nouveau")).thenReturn(Optional.empty());
        when(repository.save(any(AdminAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminAccountResponse response = service.updateSiteAdmin(12L,
                new AdminAccountUpdateRequest("Nouveau", "Nouveau nom", "Better-Password-35!"));

        assertEquals("nouveau", response.username());
        assertEquals("Nouveau nom", response.displayName());
        assertTrue(response.passwordChangeRequired());
        assertTrue(passwordEncoder.matches("Better-Password-35!", account.getPassword()));
    }

    @Test
    void refusesToDeletePermanentDeveloperAccount() {
        AdminAccount developer = new AdminAccount("bmd", "bmd", "hash", AdminRole.DEVELOPER, true, false);
        when(repository.findById(1L)).thenReturn(Optional.of(developer));

        assertThrows(ResponseStatusException.class, () -> service.deleteSiteAdmin(1L));
        verify(repository, never()).delete(any(AdminAccount.class));
    }

    @Test
    void deletesSiteAdminAccount() {
        AdminAccount admin = new AdminAccount("manager", "Manager", "hash", AdminRole.SITE_ADMIN, false, false);
        when(repository.findById(12L)).thenReturn(Optional.of(admin));

        service.deleteSiteAdmin(12L);

        verify(repository).delete(admin);
    }
}
