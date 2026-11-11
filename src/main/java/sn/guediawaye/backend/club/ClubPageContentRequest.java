package sn.guediawaye.backend.club;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ClubPageContentRequest(@NotEmpty List<@Valid ClubPageItem> items) {
}
