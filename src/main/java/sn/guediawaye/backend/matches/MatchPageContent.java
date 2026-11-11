package sn.guediawaye.backend.matches;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "match_page_content")
public class MatchPageContent {

    @Id
    private Long id;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String contentJson;

    protected MatchPageContent() {
    }

    public MatchPageContent(Long id, String contentJson) {
        this.id = id;
        this.contentJson = contentJson;
    }

    public String getContentJson() {
        return contentJson;
    }

    public void setContentJson(String contentJson) {
        this.contentJson = contentJson;
    }
}
