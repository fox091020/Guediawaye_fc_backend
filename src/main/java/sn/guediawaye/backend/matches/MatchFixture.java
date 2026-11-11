package sn.guediawaye.backend.matches;

public record MatchFixture(
        String id,
        String group,
        String date,
        String time,
        String opponentId,
        String venueType,
        String neutralStadium,
        String competition,
        String status,
        String homeScore,
        String awayScore,
        String watchUrl) {
}
