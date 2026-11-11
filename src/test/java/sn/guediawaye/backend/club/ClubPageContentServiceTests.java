package sn.guediawaye.backend.club;

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
class ClubPageContentServiceTests {

    @Mock
    private ClubPageContentRepository repository;

    private ClubPageContentService service;

    @BeforeEach
    void setUp() {
        service = new ClubPageContentService(repository, new ObjectMapper());
    }

    @Test
    void returnsExistingClubInformationAsDefaultContent() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ClubPageContentResponse response = service.findContent();

        assertEquals(20, response.items().size());
        assertEquals("identity", response.items().getFirst().group());
        assertEquals(3, response.items().stream().filter(item -> item.group().equals("history")).count());
    }

    @Test
    void savesValidClubContent() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        List<ClubPageItem> defaults = service.findContent().items();
        ClubPageContentResponse response = service.updateContent(new ClubPageContentRequest(defaults));

        assertEquals(defaults, response.items());
    }

    @Test
    void rejectsContentWithoutAllThreeHistoryMilestones() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        List<ClubPageItem> items = service.findContent().items().stream()
                .filter(item -> !item.id().equals("club-history-2005"))
                .toList();

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new ClubPageContentRequest(items)));
    }

    @Test
    void rejectsMoreThanNineFigures() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        List<ClubPageItem> defaults = service.findContent().items();
        ClubPageItem extraFigure = new ClubPageItem(
                "club-figure-extra", "figures", "Nouvelle Figure", null, null, null, null,
                null, null, null, null, "Description", null, null, "Rôle", "/figure.png");
        List<ClubPageItem> items = new java.util.ArrayList<>(defaults);
        items.add(extraFigure);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new ClubPageContentRequest(items)));

        assertEquals(400, exception.getStatusCode().value());
    }
}
