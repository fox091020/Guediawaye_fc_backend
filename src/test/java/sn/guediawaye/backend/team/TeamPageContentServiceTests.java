package sn.guediawaye.backend.team;

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
class TeamPageContentServiceTests {

    @Mock
    private TeamPageContentRepository repository;

    private TeamPageContentService service;

    @BeforeEach
    void setUp() {
        service = new TeamPageContentService(repository, new ObjectMapper());
    }

    @Test
    void returnsCurrentTeamPageInformationAsDefaultContent() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        TeamPageContentResponse response = service.findContent();

        assertEquals(21, response.items().size());
        assertEquals("group", response.items().getFirst().group());
        assertEquals(12, response.items().stream().filter(item -> item.group().equals("players")).count());
        assertEquals(8, response.items().stream().filter(item -> item.group().equals("staff")).count());
    }

    @Test
    void savesValidTeamContent() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        List<TeamPageItem> defaults = service.findContent().items();
        TeamPageContentResponse response = service.updateContent(new TeamPageContentRequest(defaults));

        assertEquals(defaults, response.items());
    }

    @Test
    void rejectsContentWithoutTheGroupSection() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        List<TeamPageItem> items = service.findContent().items().stream()
                .filter(item -> !item.group().equals("group"))
                .toList();

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new TeamPageContentRequest(items)));
    }
}
