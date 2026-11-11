package sn.guediawaye.backend.homepage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "home_page_content")
public class HomePageContent {

    @Id
    private Long id;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String itemsJson;

    protected HomePageContent() {
    }

    public HomePageContent(Long id, String itemsJson) {
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
