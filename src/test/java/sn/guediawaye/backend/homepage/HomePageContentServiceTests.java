package sn.guediawaye.backend.homepage;

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
class HomePageContentServiceTests {

    @Mock
    private HomePageContentRepository repository;

    private HomePageContentService service;

    @BeforeEach
    void setUp() {
        service = new HomePageContentService(repository, new ObjectMapper());
    }

    @Test
    void returnsDefaultContentOnFirstRead() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        HomePageContentResponse response = service.findContent();

        assertEquals(11, response.items().size());
        assertEquals("hero", response.items().getFirst().group());
        assertEquals("events", response.items().get(6).group());
    }

    @Test
    void addsDefaultStadiumImageToPreviouslySavedHomepageContent() throws Exception {
        HomePageItem hero = new HomePageItem(
                "hero-1", "hero", "CLUB", "TITRE", "ACCROCHE", "Texte",
                "/hero.png", null, null, null, null, null,
                null, null, null, null, null, null, null);
        HomePageItem story = new HomePageItem(
                "story-1", "story", null, null, null, "Histoire",
                "/story.png", "Photo", null, null, null, null,
                null, null, null, "CLUB", "Notre histoire", "Lire", null);
        when(repository.findById(1L)).thenReturn(Optional.of(
                new HomePageContent(1L, new ObjectMapper().writeValueAsString(List.of(hero, story)))));

        HomePageContentResponse response = service.findContent();

        assertEquals(3, response.items().size());
        assertEquals("home-events-image", response.items().getLast().id());
        assertEquals("/OBG.jpeg", response.items().getLast().image());
    }

    @Test
    void replacesHomepageContent() {
        HomePageItem hero = new HomePageItem(
                "hero-1", "hero", "CLUB", "TITRE", "ACCROCHE", "Texte",
                "/hero.png", null, null, null, null, null,
                null, null, null, null, null, null, null);
        HomePageItem story = new HomePageItem(
                "story-1", "story", null, null, null, "Histoire",
                "/story.png", "Photo", null, null, null, null,
                null, null, null, "CLUB", "Notre histoire", "Lire", null);
        HomePageItem events = new HomePageItem(
                "home-events-image", "events", null, null, null, null,
                "/OBG.jpeg", "Photo du stade", null, null, null, null,
                null, null, null, null, null, null, "Photo du stade");
        HomePageContent existing = new HomePageContent(1L, "[]");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        HomePageContentResponse response = service.updateContent(new HomePageContentRequest(List.of(hero, story, events)));

        assertEquals(3, response.items().size());
    }

    @Test
    void rejectsContentWithoutRequiredSingletonSections() {
        HomePageItem hero = new HomePageItem(
                "hero-1", "hero", "CLUB", "TITRE", "ACCROCHE", "Texte",
                "/hero.png", null, null, null, null, null,
                null, null, null, null, null, null, null);

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new HomePageContentRequest(List.of(hero))));
    }
}
