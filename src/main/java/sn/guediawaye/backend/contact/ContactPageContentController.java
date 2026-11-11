package sn.guediawaye.backend.contact;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactPageContentController {

    private final ContactPageContentService service;

    public ContactPageContentController(ContactPageContentService service) {
        this.service = service;
    }

    @GetMapping
    public ContactPageContentResponse findContent() {
        return service.findContent();
    }

    @PutMapping
    public ContactPageContentResponse updateContent(@Valid @RequestBody ContactPageContentRequest request) {
        return service.updateContent(request);
    }
}
