package sn.guediawaye.backend.club;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/club")
public class ClubPageContentController {

    private final ClubPageContentService service;

    public ClubPageContentController(ClubPageContentService service) {
        this.service = service;
    }

    @GetMapping
    public ClubPageContentResponse findContent() {
        return service.findContent();
    }

    @PutMapping
    public ClubPageContentResponse updateContent(@Valid @RequestBody ClubPageContentRequest request) {
        return service.updateContent(request);
    }
}
