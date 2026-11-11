package sn.guediawaye.backend.news;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Entity
@Table(name = "news_articles")
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 180, unique = true)
    private String slug;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "news_article_images", joinColumns = @JoinColumn(name = "article_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "image_url", nullable = false, length = 500)
    private List<String> imageUrls = new ArrayList<>();

    @Column(length = 1000)
    private String externalUrl;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean featured;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean published = true;

    @Column(nullable = false)
    private Instant publishedAt;

    protected NewsArticle() {
    }

    public NewsArticle(
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
        this.slug = slug;
        this.category = category;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.imageUrl = imageUrl;
        this.imageUrls = new ArrayList<>(imageUrls);
        this.externalUrl = externalUrl;
        this.featured = featured;
        this.published = published;
        this.publishedAt = publishedAt;
    }

    void updateFrom(NewsArticleRequest request) {
        slug = request.slug();
        category = request.category();
        title = request.title();
        summary = request.summary();
        content = request.content();
        imageUrl = request.imageUrl();
        imageUrls.clear();
        imageUrls.addAll(request.imageUrls());
        externalUrl = request.externalUrl();
        featured = request.featured();
        published = request.published();
        publishedAt = request.publishedAt();
    }

    @PrePersist
    void setPublicationTimeIfMissing() {
        if (publishedAt == null) {
            publishedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSlug() {
        return slug;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public boolean isFeatured() {
        return featured;
    }

    public boolean isPublished() {
        return published;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
