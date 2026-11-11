package sn.guediawaye.backend.team;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/team")
public class TeamPageContentController {

    private final TeamPageContentService service;

    public TeamPageContentController(TeamPageContentService service) {
        this.service = service;
    }

    @GetMapping
    public TeamPageContentResponse findContent() {
        return service.findContent();
    }

    @PutMapping
    public TeamPageContentResponse updateContent(@Valid @RequestBody TeamPageContentRequest request) {
        return service.updateContent(request);
    }
}
