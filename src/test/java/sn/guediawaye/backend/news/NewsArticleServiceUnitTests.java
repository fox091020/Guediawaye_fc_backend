package sn.guediawaye.backend.news;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NewsArticleServiceUnitTests {

    @Mock
    private NewsArticleRepository repository;

    @InjectMocks
    private NewsArticleService service;

    @Test
    void refusesMoreThanThreeFeaturedArticles() {
        when(repository.existsBySlug("fourth-article")).thenReturn(false);
        when(repository.countByFeaturedTrue()).thenReturn(3L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.create(request("fourth-article", true, null)));

        assertEquals(400, exception.getStatusCode().value());
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(NewsArticle.class));
    }

    @Test
    void rejectsNonHttpExternalLinks() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.create(request("unsafe-link", false, "javascript:alert(1)")));

        assertEquals(400, exception.getStatusCode().value());
        verify(repository, never()).existsBySlug("unsafe-link");
    }

    private NewsArticleRequest request(String slug, boolean featured, String externalUrl) {
        return new NewsArticleRequest(
                slug,
                "Équipe",
                "Titre de test",
                "Résumé",
                "Contenu",
                "/actualite.jpg",
                List.of("/actualite.jpg", "/galerie.jpg"),
                externalUrl,
                featured,
                true,
                Instant.parse("2026-11-09T12:00:00Z"));
    }
}
