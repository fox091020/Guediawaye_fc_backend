package sn.guediawaye.backend.team;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TeamPageContentRequest(@NotEmpty List<@Valid TeamPageItem> items) {
}
