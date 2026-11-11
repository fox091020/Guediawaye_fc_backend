package sn.guediawaye.backend.team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_page_content")
public class TeamPageContent {

    @Id
    private Long id;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String itemsJson;

    protected TeamPageContent() {
    }

    public TeamPageContent(Long id, String itemsJson) {
        this.id = id;
        this.itemsJson = itemsJson;
    }

    public String getItemsJson() {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson) {
        this.itemsJson = itemsJson;
    }
}
