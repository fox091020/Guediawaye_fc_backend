package sn.guediawaye.backend.shop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shop_page_content")
public class ShopPageContent {

    @Id
    private Long id;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String contentJson;

    protected ShopPageContent() {
    }

    public ShopPageContent(Long id, String contentJson) {
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
