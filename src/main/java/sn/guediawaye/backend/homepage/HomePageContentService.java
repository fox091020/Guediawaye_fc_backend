package sn.guediawaye.backend.homepage;

import java.util.HashSet;
import java.util.ArrayList;
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
public class HomePageContentService {

    private static final long CONTENT_ID = 1L;
    private static final TypeReference<List<HomePageItem>> ITEM_LIST_TYPE = new TypeReference<>() {};

    private final HomePageContentRepository repository;
    private final ObjectMapper objectMapper;

    public HomePageContentService(HomePageContentRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public HomePageContentResponse findContent() {
        return repository.findById(CONTENT_ID)
                .map(content -> {
                    List<HomePageItem> items = new ArrayList<>(deserialize(content.getItemsJson()));
                    if (items.stream().noneMatch(item -> "events".equals(item.group()))) {
                        items.add(defaultEventsImage());
                    }
                    return new HomePageContentResponse(items);
                })
                .orElseGet(() -> new HomePageContentResponse(defaultItems()));
    }

    public HomePageContentResponse updateContent(HomePageContentRequest request) {
        validateItems(request.items());
        HomePageContent content = repository.findById(CONTENT_ID)
                .orElseGet(() -> new HomePageContent(CONTENT_ID, "[]"));
        content.setItemsJson(serialize(request.items()));
        repository.save(content);
        return new HomePageContentResponse(request.items());
    }

    private void validateItems(List<HomePageItem> items) {
        Set<String> ids = new HashSet<>();
        long heroCount = 0;
        long storyCount = 0;
        long eventsCount = 0;

        for (HomePageItem item : items) {
            if (item == null || isBlank(item.id()) || isBlank(item.group()) || !ids.add(item.id())) {
                throw invalidContent();
            }
            switch (item.group()) {
                case "hero" -> {
                    heroCount++;
                    require(item.kicker(), item.headline(), item.highlightedTitle(), item.description(), item.image());
                }
                case "stats" -> require(item.value(), item.label(), item.detail());
                case "story" -> {
                    storyCount++;
                    require(item.eyebrow(), item.title(), item.description(), item.image(), item.imageAlt(), item.buttonText());
                }
                case "shop" -> require(item.name(), item.image(), item.imageAlt());
                case "events" -> {
                    eventsCount++;
                    require(item.name(), item.image(), item.imageAlt());
                }
                default -> throw invalidContent();
            }
        }

        if (heroCount != 1 || storyCount != 1 || eventsCount != 1) {
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
                "Les données de la page d'accueil sont incomplètes ou invalides.");
    }

    private String serialize(List<HomePageItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Impossible de sérialiser le contenu de la page d'accueil.", exception);
        }
    }

    private List<HomePageItem> deserialize(String itemsJson) {
        try {
            return objectMapper.readValue(itemsJson, ITEM_LIST_TYPE);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Le contenu de la page d'accueil stocké en base est invalide.", exception);
        }
    }

    private List<HomePageItem> defaultItems() {
        return List.of(
                new HomePageItem(
                        "home-hero", "hero", "GUEDIAWAYE FC", "PLUS QU’UN CLUB,", "UNE FAMILLE",
                        "Le Guediawaye FC, fier representant de notre ville,\nincarne la passion, la determination et l’unite.",
                        "/hero-background.png", null, null, null, null, null,
                        null, null, null, null, null, null, null),
                new HomePageItem(
                        "home-stat-years", "stats", null, null, null, null, null, null, null, null, null, null,
                        "32", "Annees d’existence", "Depuis 1993", null, null, null, null),
                new HomePageItem(
                        "home-stat-supporters", "stats", null, null, null, null, null, null, null, null, null, null,
                        "+50 000", "Supporters", "Une seule famille", null, null, null, null),
                new HomePageItem(
                        "home-stat-titles", "stats", null, null, null, null, null, null, null, null, null, null,
                        "3", "Titres nationaux", "Un palmares en construction", null, null, null, null),
                new HomePageItem(
                        "home-stat-passion", "stats", null, null, null, null, null, null, null, null, null, null,
                        "1", "Passion", "Les Crabers pour toujours", null, null, null, null),
                new HomePageItem(
                        "home-story", "story", null, null, null,
                        "Fonde en 1993 a Guediawaye, le Guediawaye FC est un club professionnel senegalais qui se bat avec fierte pour porter les couleurs de sa ville. Grace a son talent, son travail et le soutien de ses supporters, le club continue d’ecrire son histoire au plus haut niveau du football senegalais.",
                        "/histoire-image.png", "Supporters du Guediawaye FC dans le stade",
                        null, null, null, null, null, null, null, "A PROPOS", "NOTRE HISTOIRE",
                        "Lire l’histoire du club", null),
                defaultEventsImage(),
                new HomePageItem(
                        "home-shop-shirt-1", "shop", null, null, null, null, "/maillot1.png", "Maillot 1",
                        null, null, null, null, null, null, null, null, null, null, "Maillot 1"),
                new HomePageItem(
                        "home-shop-shirt-2", "shop", null, null, null, null, "/maillot2.png", "Maillot 2",
                        null, null, null, null, null, null, null, null, null, null, "Maillot 2"),
                new HomePageItem(
                        "home-shop-shirt-3", "shop", null, null, null, null, "/maillot3.png", "Maillot 3",
                        null, null, null, null, null, null, null, null, null, null, "Maillot 3"),
                new HomePageItem(
                        "home-shop-accessory", "shop", null, null, null, null, "/accessoir.png", "Accessoire",
                        null, null, null, null, null, null, null, null, null, null, "Accessoire"));
    }

    private HomePageItem defaultEventsImage() {
        return new HomePageItem(
                "home-events-image", "events", null, null, null, null,
                "/OBG.jpeg", "Photo du stade", null, null, null, null,
                null, null, null, null, null, null, "Photo du stade");
    }
}
