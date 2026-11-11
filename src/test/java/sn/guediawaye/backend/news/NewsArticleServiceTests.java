package sn.guediawaye.backend.news;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class NewsArticleServiceTests {

    @Autowired
    private NewsArticleRepository repository;

    @Autowired
    private NewsArticleService service;

    @BeforeEach
    void clearArticles() {
        repository.deleteAll();
    }

    @Test
    void createsReadsUpdatesAndDeletesAnArticle() {
        NewsArticleRequest initial = request("Première actualité");
        NewsArticleResponse created = service.create(initial);

        assertNotNull(created.id());
        assertEquals("Première actualité", service.findById(created.id()).title());
        assertEquals(1, service.findAll().size());

        NewsArticleResponse updated = service.update(created.id(), request("Actualité modifiée"));
        assertEquals("Actualité modifiée", updated.title());

        service.delete(created.id());
        assertEquals(0, service.findAll().size());
    }

    @Test
    void limitsFeaturedArticlesToThree() {
        service.create(request("Première actualité", true));
        service.create(request("Deuxième actualité", true));
        service.create(request("Troisième actualité", true));

        assertThrows(ResponseStatusException.class, () -> service.create(request("Quatrième actualité", true)));
    }

    private NewsArticleRequest request(String title) {
        return request(title, false);
    }

    private NewsArticleRequest request(String title, boolean featured) {
        return new NewsArticleRequest(
                title.toLowerCase().replace(' ', '-').replace('é', 'e'),
                "Équipe",
                title,
                "Résumé de l’actualité",
                "Contenu détaillé de l’actualité.",
                "/actu_1.jpeg",
                List.of("/actu_1.jpeg"),
                null,
                featured,
                true,
                Instant.parse("2026-11-09T12:00:00Z"));
    }
}
