package sn.guediawaye.backend.auth;

import java.time.Instant;

public record AdminAccountResponse(
        Long id,
        String username,
        String displayName,
        AdminRole role,
        boolean enabled,
        boolean permanent,
        boolean passwordChangeRequired,
        Instant createdAt,
        Instant lastLoginAt) {

    static AdminAccountResponse from(AdminAccount account) {
        return new AdminAccountResponse(
                account.getId(),
                account.getUsername(),
                account.getDisplayName(),
                account.getRole(),
                account.isEnabled(),
                account.isPermanent(),
                account.isPasswordChangeRequired(),
                account.getCreatedAt(),
                account.getLastLoginAt());
    }
}
