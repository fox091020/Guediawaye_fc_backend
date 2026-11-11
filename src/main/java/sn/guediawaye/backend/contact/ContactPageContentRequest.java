package sn.guediawaye.backend.contact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ContactPageContentRequest(@NotEmpty List<@Valid ContactPageItem> items) {
}
