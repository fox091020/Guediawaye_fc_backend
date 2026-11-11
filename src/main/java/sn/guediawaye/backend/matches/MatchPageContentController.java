package sn.guediawaye.backend.matches;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchPageContentController {

    private final MatchPageContentService service;

    public MatchPageContentController(MatchPageContentService service) {
        this.service = service;
    }

    @GetMapping
    public MatchPageContentResponse findContent() {
        return service.findContent();
    }

    @PutMapping
    public MatchPageContentResponse updateContent(@Valid @RequestBody MatchPageContentRequest request) {
        return service.updateContent(request);
    }
}
