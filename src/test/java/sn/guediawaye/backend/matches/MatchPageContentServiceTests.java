package sn.guediawaye.backend.matches;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MatchPageContentServiceTests {

    @Mock
    private MatchPageContentRepository repository;

    private MatchPageContentService service;

    @BeforeEach
    void setUp() {
        service = new MatchPageContentService(repository, new ObjectMapper());
    }

    @Test
    void returnsExistingTeamsAndFixturesAsDefaultContent() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        MatchPageContentResponse response = service.findContent();

        assertEquals(8, response.opponents().size());
        assertEquals(9, response.matches().size());
    }

    @Test
    void savesValidMatchContent() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        MatchPageContentResponse defaults = service.findContent();
        MatchPageContentResponse response = service.updateContent(
                new MatchPageContentRequest(defaults.opponents(), defaults.matches()));

        assertEquals(defaults, response);
    }

    @Test
    void rejectsFixtureForUnknownOpponent() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        MatchPageContentResponse defaults = service.findContent();
        MatchFixture invalidMatch = new MatchFixture(
                "match-unknown-opponent", "matches", "2026-11-15", "16:00",
                "unknown-opponent", "home", "", "Championnat National", "scheduled", "", "", "");
        List<MatchFixture> matches = new java.util.ArrayList<>(defaults.matches());
        matches.add(invalidMatch);

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new MatchPageContentRequest(defaults.opponents(), matches)));
    }

    @Test
    void requiresStadiumForNeutralFixtures() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        MatchPageContentResponse defaults = service.findContent();
        MatchFixture neutralMatch = new MatchFixture(
                "match-neutral", "matches", "2026-11-15", "16:00",
                defaults.opponents().getFirst().id(), "neutral", "", "Coupe du Sénégal", "scheduled", "", "", "");

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new MatchPageContentRequest(defaults.opponents(), List.of(neutralMatch))));
    }

    @Test
    void rejectsMoreThanOneLiveMatch() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        MatchPageContentResponse defaults = service.findContent();
        MatchFixture firstLive = new MatchFixture(
                "match-live-1", "matches", "2026-11-15", "16:00",
                defaults.opponents().getFirst().id(), "home", "", "Championnat National", "live", "", "", "");
        MatchFixture secondLive = new MatchFixture(
                "match-live-2", "matches", "2026-11-16", "16:00",
                defaults.opponents().get(1).id(), "away", "", "Championnat National", "live", "", "", "");

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new MatchPageContentRequest(
                        defaults.opponents(), List.of(firstLive, secondLive))));
    }

    @Test
    void savesOptionalMatchWatchingUrl() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        MatchPageContentResponse defaults = service.findContent();
        MatchFixture match = new MatchFixture(
                "match-with-stream", "matches", "2026-11-15", "16:00",
                defaults.opponents().getFirst().id(), "home", "", "Championnat National",
                "scheduled", "", "", "https://example.com/live");

        MatchPageContentResponse response = service.updateContent(
                new MatchPageContentRequest(defaults.opponents(), List.of(match)));

        assertEquals("https://example.com/live", response.matches().getFirst().watchUrl());
    }

    @Test
    void rejectsNonHttpWatchingUrl() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        MatchPageContentResponse defaults = service.findContent();
        MatchFixture match = new MatchFixture(
                "match-with-invalid-stream", "matches", "2026-11-15", "16:00",
                defaults.opponents().getFirst().id(), "home", "", "Championnat National",
                "scheduled", "", "", "javascript:alert(1)");

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new MatchPageContentRequest(defaults.opponents(), List.of(match))));
    }
}
