package sn.guediawaye.backend.news;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "news_article_seed_state")
public class NewsArticleSeedState {

    @Id
    private Long id;

    protected NewsArticleSeedState() {
    }

    public NewsArticleSeedState(Long id) {
        this.id = id;
    }
}
