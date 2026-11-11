package sn.guediawaye.backend.matches;

import java.util.List;

public record MatchPageContentResponse(List<MatchOpponent> opponents, List<MatchFixture> matches) {
}
