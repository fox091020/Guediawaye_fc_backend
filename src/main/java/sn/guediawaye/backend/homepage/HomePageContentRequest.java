package sn.guediawaye.backend.homepage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record HomePageContentRequest(@NotEmpty List<@Valid HomePageItem> items) {
}
