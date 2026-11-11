package sn.guediawaye.backend.contact;

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
class ContactPageContentServiceTests {

    @Mock
    private ContactPageContentRepository repository;

    private ContactPageContentService service;

    @BeforeEach
    void setUp() {
        service = new ContactPageContentService(repository, new ObjectMapper());
    }

    @Test
    void returnsExistingContactInformationAsDefaultContent() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ContactPageContentResponse response = service.findContent();

        assertEquals(5, response.items().size());
        assertEquals("contact", response.items().getFirst().group());
        assertEquals(4, response.items().stream().filter(item -> item.group().equals("faq")).count());
    }

    @Test
    void savesValidContactContent() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        List<ContactPageItem> defaults = service.findContent().items();
        ContactPageContentResponse response = service.updateContent(new ContactPageContentRequest(defaults));

        assertEquals(defaults, response.items());
    }

    @Test
    void rejectsContentWithoutContactInformation() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        List<ContactPageItem> items = service.findContent().items().stream()
                .filter(item -> !item.group().equals("contact"))
                .toList();

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new ContactPageContentRequest(items)));
    }
}
