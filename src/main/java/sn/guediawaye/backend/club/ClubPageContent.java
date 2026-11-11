package sn.guediawaye.backend.club;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "club_page_content")
public class ClubPageContent {

    @Id
    private Long id;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String itemsJson;

    protected ClubPageContent() {
    }

    public ClubPageContent(Long id, String itemsJson) {
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
