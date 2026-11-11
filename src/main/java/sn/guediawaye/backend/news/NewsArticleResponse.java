package sn.guediawaye.backend.news;

import java.time.Instant;
import java.util.List;

public record NewsArticleResponse(
        Long id,
        String slug,
        String category,
        String title,
        String summary,
        String content,
        String imageUrl,
        List<String> imageUrls,
        String externalUrl,
        boolean featured,
        boolean published,
        Instant publishedAt) {

    public static NewsArticleResponse from(NewsArticle article) {
        return new NewsArticleResponse(
                article.getId(),
                article.getSlug(),
                article.getCategory(),
                article.getTitle(),
                article.getSummary(),
                article.getContent(),
                article.getImageUrl(),
                List.copyOf(article.getImageUrls()),
                article.getExternalUrl(),
                article.isFeatured(),
                article.isPublished(),
                article.getPublishedAt());
    }
}
