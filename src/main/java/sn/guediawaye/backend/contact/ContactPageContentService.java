package sn.guediawaye.backend.contact;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional
public class ContactPageContentService {

    private static final long CONTENT_ID = 1L;
    private static final TypeReference<List<ContactPageItem>> ITEM_LIST_TYPE = new TypeReference<>() {};

    private final ContactPageContentRepository repository;
    private final ObjectMapper objectMapper;

    public ContactPageContentService(ContactPageContentRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ContactPageContentResponse findContent() {
        return repository.findById(CONTENT_ID)
                .map(content -> new ContactPageContentResponse(deserialize(content.getItemsJson())))
                .orElseGet(() -> new ContactPageContentResponse(defaultItems()));
    }

    public ContactPageContentResponse updateContent(ContactPageContentRequest request) {
        validateItems(request.items());
        ContactPageContent content = repository.findById(CONTENT_ID)
                .orElseGet(() -> new ContactPageContent(CONTENT_ID, "[]"));
        content.setItemsJson(serialize(request.items()));
        repository.save(content);
        return new ContactPageContentResponse(request.items());
    }

    private void validateItems(List<ContactPageItem> items) {
        Set<String> ids = new HashSet<>();
        long contactCount = 0;
        for (ContactPageItem item : items) {
            if (item == null || isBlank(item.id()) || !ids.add(item.id())) {
                throw invalidContent();
            }
            if ("contact".equals(item.group())) {
                contactCount++;
                require(item.phone(), item.email(), item.address(), item.addressDetails());
            } else if ("faq".equals(item.group())) {
                require(item.question(), item.answer());
            } else {
                throw invalidContent();
            }
        }
        if (contactCount != 1) {
            throw invalidContent();
        }
    }

    private void require(String... values) {
        for (String value : values) {
            if (isBlank(value)) {
                throw invalidContent();
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ResponseStatusException invalidContent() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Les coordonnées ou les questions fréquentes sont incomplètes ou invalides.");
    }

    private String serialize(List<ContactPageItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Impossible de sérialiser le contenu de la page contact.", exception);
        }
    }

    private List<ContactPageItem> deserialize(String itemsJson) {
        try {
            return objectMapper.readValue(itemsJson, ITEM_LIST_TYPE);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Le contenu de la page contact stocké en base est invalide.", exception);
        }
    }

    private List<ContactPageItem> defaultItems() {
        return List.of(
                new ContactPageItem(
                        "contact-coordinates", "contact",
                        "+221 77 123 45 67", "Lun - Ven : 8h00 - 18h00",
                        "contact@guediawaye-fc.sn", "Réponse sous 24h",
                        "Stade Municipal de Guediawaye", "Guediawaye, Dakar - Sénégal",
                        "https://maps.google.com/?q=Stade+Municipal+de+Guediawaye", null, null),
                new ContactPageItem(
                        "contact-faq-ticket", "faq", null, null, null, null, null, null, null,
                        "Comment acheter un billet de match ?",
                        "Les billets sont disponibles via la billetterie du site ou directement aux points de vente du stade avant chaque rencontre."),
                new ContactPageItem(
                        "contact-faq-join", "faq", null, null, null, null, null, null, null,
                        "Comment rejoindre le club ?",
                        "Vous pouvez nous écrire via le formulaire de contact en précisant votre demande. Notre équipe vous indiquera les prochaines étapes."),
                new ContactPageItem(
                        "contact-faq-stadium", "faq", null, null, null, null, null, null, null,
                        "Où se trouve le stade municipal de Guediawaye ?",
                        "Le stade municipal se trouve à Guediawaye, Dakar. Consultez la carte dans la section coordonnées pour préparer votre venue."),
                new ContactPageItem(
                        "contact-faq-shop", "faq", null, null, null, null, null, null, null,
                        "Comment contacter la boutique officielle ?",
                        "Contactez notre équipe par email ou téléphone. Nous vous renseignerons sur les produits disponibles, les tailles et la livraison."));
    }
}
