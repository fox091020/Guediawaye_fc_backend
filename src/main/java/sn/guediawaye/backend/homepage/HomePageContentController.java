package sn.guediawaye.backend.homepage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/homepage")
public class HomePageContentController {

    private final HomePageContentService service;

    public HomePageContentController(HomePageContentService service) {
        this.service = service;
    }

    @GetMapping
    public HomePageContentResponse findContent() {
        return service.findContent();
    }

    @PutMapping
    public HomePageContentResponse updateContent(@Valid @RequestBody HomePageContentRequest request) {
        return service.updateContent(request);
    }
}
