package sn.guediawaye.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminAccountUpdateRequest(
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "L'identifiant contient des caractères non autorisés.")
        String username,
        @NotBlank @Size(max = 100) String displayName,
        @Size(max = 72) String password) {
}
