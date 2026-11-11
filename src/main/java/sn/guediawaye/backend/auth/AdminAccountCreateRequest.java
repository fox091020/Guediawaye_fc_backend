package sn.guediawaye.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminAccountCreateRequest(
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "L'identifiant contient des caractères non autorisés.")
        String username,
        @NotBlank @Size(max = 100) String displayName,
        @NotBlank @Size(min = 12, max = 72) String password) {
}
