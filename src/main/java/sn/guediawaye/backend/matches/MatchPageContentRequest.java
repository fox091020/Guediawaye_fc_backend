package sn.guediawaye.backend.matches;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record MatchPageContentRequest(
        @NotEmpty List<@Valid MatchOpponent> opponents,
        @NotNull List<@Valid MatchFixture> matches) {
}
