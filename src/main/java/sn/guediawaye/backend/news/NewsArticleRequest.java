package sn.guediawaye.backend.news;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record NewsArticleRequest(
        @NotBlank @Size(max = 180) String slug,
        @NotBlank @Size(max = 80) String category,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 500) String summary,
        @NotBlank String content,
        @NotBlank @Size(max = 500) String imageUrl,
        @NotNull @Size(max = 50) List<@NotBlank @Size(max = 500) String> imageUrls,
        @Size(max = 1000) String externalUrl,
        boolean featured,
        boolean published,
        @NotNull Instant publishedAt) {
}
