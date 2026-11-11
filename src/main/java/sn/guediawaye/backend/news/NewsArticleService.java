package sn.guediawaye.backend.news;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class NewsArticleService {

    private final NewsArticleRepository repository;

    public NewsArticleService(NewsArticleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<NewsArticleResponse> findAll() {
        return repository.findAllByOrderByPublishedAtDesc().stream()
                .map(NewsArticleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NewsArticleResponse findById(Long id) {
        return NewsArticleResponse.from(findArticle(id));
    }

    public NewsArticleResponse create(NewsArticleRequest request) {
        validateArticle(request, null);
        NewsArticle article = new NewsArticle(
                request.slug(),
                request.category(),
                request.title(),
                request.summary(),
                request.content(),
                request.imageUrl(),
                request.imageUrls(),
                request.externalUrl(),
                request.featured(),
                request.published(),
                request.publishedAt());
        return NewsArticleResponse.from(repository.save(article));
    }

    public NewsArticleResponse update(Long id, NewsArticleRequest request) {
        NewsArticle existing = findArticle(id);
        validateArticle(request, id);
        existing.updateFrom(request);
        return NewsArticleResponse.from(repository.save(existing));
    }

    public void delete(Long id) {
        repository.delete(findArticle(id));
    }

    private NewsArticle findArticle(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Actualité introuvable"));
    }

    private void validateArticle(NewsArticleRequest request, Long excludedId) {
        validateExternalUrl(request.externalUrl());

        boolean duplicateSlug = excludedId == null
                ? repository.existsBySlug(request.slug())
                : repository.existsBySlugAndIdNot(request.slug(), excludedId);
        if (duplicateSlug) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse d’actualité est déjà utilisée.");
        }

        if (request.featured()) {
            long featuredCount = repository.countByFeaturedTrue();
            if (excludedId != null && findArticle(excludedId).isFeatured()) {
                featuredCount--;
            }
            if (featuredCount >= 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trois actualités au maximum peuvent être mises en avant.");
            }
        }
    }

    private void validateExternalUrl(String externalUrl) {
        if (externalUrl == null || externalUrl.isBlank()) {
            return;
        }
        try {
            URI uri = URI.create(externalUrl);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le lien externe doit être une adresse HTTP ou HTTPS valide.",
                    exception);
        }
    }
}
