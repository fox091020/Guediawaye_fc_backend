package sn.guediawaye.backend.shop;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
public class ShopPageContentController {

    private final ShopPageContentService service;

    public ShopPageContentController(ShopPageContentService service) {
        this.service = service;
    }

    @GetMapping
    public ShopPageContentResponse findContent() {
        return service.findContent();
    }

    @PutMapping
    public ShopPageContentResponse updateContent(@RequestBody ShopPageContentRequest request) {
        return service.updateContent(request);
    }
}
