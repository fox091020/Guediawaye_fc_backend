package sn.guediawaye.backend.club;

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
public class ClubPageContentService {

    private static final long CONTENT_ID = 1L;
    private static final TypeReference<List<ClubPageItem>> ITEM_LIST_TYPE = new TypeReference<>() {};

    private final ClubPageContentRepository repository;
    private final ObjectMapper objectMapper;

    public ClubPageContentService(ClubPageContentRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ClubPageContentResponse findContent() {
        return repository.findById(CONTENT_ID)
                .map(content -> new ClubPageContentResponse(deserialize(content.getItemsJson())))
                .orElseGet(() -> new ClubPageContentResponse(defaultItems()));
    }

    public ClubPageContentResponse updateContent(ClubPageContentRequest request) {
        validateItems(request.items());
        ClubPageContent content = repository.findById(CONTENT_ID)
                .orElseGet(() -> new ClubPageContent(CONTENT_ID, "[]"));
        content.setItemsJson(serialize(request.items()));
        repository.save(content);
        return new ClubPageContentResponse(request.items());
    }

    private void validateItems(List<ClubPageItem> items) {
        Set<String> ids = new HashSet<>();
        long identityCount = 0;
        long historyCount = 0;
        long figureCount = 0;
        for (ClubPageItem item : items) {
            if (item == null || isBlank(item.id()) || isBlank(item.group()) || !ids.add(item.id())) {
                throw invalidContent();
            }
            switch (item.group()) {
                case "identity" -> {
                    identityCount++;
                    require(item.name(), item.nickname(), item.yearFounded(), item.colors(),
                            item.logo(), item.title(), item.description());
                }
                case "history" -> {
                    historyCount++;
                    require(item.year(), item.label(), item.title(), item.summary(), item.description());
                }
                case "stats" -> require(item.label(), item.value());
                case "honours" -> require(item.title(), item.years(), item.description(), item.image());
                case "figures" -> {
                    figureCount++;
                    require(item.name(), item.role(), item.description(), item.image());
                }
                default -> throw invalidContent();
            }
        }
        if (identityCount != 1 || historyCount != 3 || figureCount > 9) {
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
                "Les données de la page du club sont incomplètes ou invalides.");
    }

    private String serialize(List<ClubPageItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Impossible de sérialiser le contenu de la page du club.", exception);
        }
    }

    private List<ClubPageItem> deserialize(String itemsJson) {
        try {
            return objectMapper.readValue(itemsJson, ITEM_LIST_TYPE);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Le contenu de la page du club stocké en base est invalide.", exception);
        }
    }

    private List<ClubPageItem> defaultItems() {
        return List.of(
                new ClubPageItem(
                        "club-identity", "identity", "Guediawaye FC", "Les Crabers", "1993",
                        "Bleu et jaune", "/logo-1.png", null, null, "PLUS QU’UN CLUB,\nUNE IDENTITÉ",
                        null,
                        "Fondé en 1993 à Guediawaye, le Guediawaye FC est un club professionnel sénégalais qui porte les couleurs, les rêves et la fierté de toute une ville.",
                        null, null, null, null),
                new ClubPageItem(
                        "club-history-1993", "history", null, null, null, null, null,
                        "1993", "La fondation", "Les premières années",
                        "Le Guediawaye FC naît avec une ambition simple : représenter fièrement sa ville et ses couleurs.",
                        "Le club est fondé à Guediawaye par des passionnés déterminés à construire une identité sportive forte et à rassembler toute une ville autour du football.",
                        null, null, null, null),
                new ClubPageItem(
                        "club-history-2005", "history", null, null, null, null, null,
                        "2005", "Une équipe qui grandit", "Saison de promotion",
                        "Le club franchit les étapes et installe durablement son identité dans le football sénégalais.",
                        "Cette saison marque un tournant important : le collectif gagne en maturité, affirme son style et franchit un nouveau palier dans la compétition nationale.",
                        null, null, null, null),
                new ClubPageItem(
                        "club-history-2021", "history", null, null, null, null, null,
                        "2021", "La consécration", "Coupe du Sénégal",
                        "Les Crabers écrivent une nouvelle page de leur histoire avec un collectif uni et ambitieux.",
                        "Porté par un groupe solidaire et l’énergie de ses supporters, le Guediawaye FC confirme ses ambitions et inscrit un nouveau succès majeur à son histoire.",
                        null, null, null, null),
                new ClubPageItem("club-stat-founded", "stats", null, null, null, null, null, null,
                        "ANNÉE DE FONDATION", null, null, null, "1993", null, null, null),
                new ClubPageItem("club-stat-seasons", "stats", null, null, null, null, null, null,
                        "SAISONS EN LIGUE 1", null, null, null, "20", null, null, null),
                new ClubPageItem("club-stat-honours", "stats", null, null, null, null, null, null,
                        "TITRES MAJEURS", null, null, null, "3", null, null, null),
                new ClubPageItem("club-stat-supporters", "stats", null, null, null, null, null, null,
                        "TITRES AMERS", null, null, null, "1 900 000", null, null, null),
                new ClubPageItem("club-honour-national", "honours", null, null, null, null, null, null,
                        null, "Championnat National", null,
                        "Deux titres nationaux qui récompensent la régularité et le travail collectif des Crabes.",
                        null, "2021, 2023", null, "/trophe-1.png"),
                new ClubPageItem("club-honour-cup", "honours", null, null, null, null, null, null,
                        null, "Coupe du Sénégal", null,
                        "Une consécration nationale qui symbolise l’ambition et la détermination du club.",
                        null, "2021, 2023", null, "/trophe-2.png"),
                new ClubPageItem("club-honour-major", "honours", null, null, null, null, null, null,
                        null, "Titres majeurs", null,
                        "Trois titres majeurs inscrits au palmarès du Guediawaye FC au fil des générations.",
                        null, "3", null, "/trophe-3.png"),
                new ClubPageItem("club-figure-pape-diop", "figures", "Pape DIOP", null, null, null, null,
                        null, null, null, null,
                        "Un buteur généreux qui a marqué les grandes rencontres des Crabes.",
                        null, null, "Attaquant emblématique", "/legende-1.webp"),
                new ClubPageItem("club-figure-mamadou-sarr", "figures", "Mamadou SARR", null, null, null, null,
                        null, null, null, null, "Un leader respecté, symbole de courage et de fidélité au club.",
                        null, null, "Capitaine historique", "/legende-2.webp"),
                new ClubPageItem("club-figure-ibrahima-ndiaye", "figures", "Ibrahima NDIAYE", null, null, null, null,
                        null, null, null, null, "Un joueur élégant qui a porté le collectif par son talent et sa vision.",
                        null, null, "Maître du milieu", "/legende-3.webp"),
                new ClubPageItem("club-figure-abdoulaye-ba", "figures", "Abdoulaye BA", null, null, null, null,
                        null, null, null, null, "Une présence solide et un engagement constant au service du collectif.",
                        null, null, "Défenseur légendaire", "/legende-4.webp"),
                new ClubPageItem("club-figure-cheikh-thiam", "figures", "Cheikh THIAM", null, null, null, null,
                        null, null, null, null, "Un joueur explosif dont les accélérations ont fait vibrer les supporters.",
                        null, null, "Ailier des grands soirs", "/legende-1.webp"),
                new ClubPageItem("club-figure-oumar-gueye", "figures", "Oumar GUEYE", null, null, null, null,
                        null, null, null, null, "Un dernier rempart courageux, reconnu pour ses arrêts décisifs.",
                        null, null, "Gardien historique", "/legende-2.webp"),
                new ClubPageItem("club-figure-diop", "figures", "G. DIOP", null, null, null, null,
                        null, null, null, null, "Un technicien qui a développé le talent du groupe et renforcé son identité.",
                        null, null, "Entraîneur bâtisseur", "/legende-3.webp"),
                new ClubPageItem("club-figure-ndiaye", "figures", "A. NDIAYE", null, null, null, null,
                        null, null, null, null, "Un homme de l’ombre investi dans la construction et la transmission du club.",
                        null, null, "Dirigeant engagé", "/legende-4.webp"),
                new ClubPageItem("club-figure-fall", "figures", "M. FALL", null, null, null, null,
                        null, null, null, null, "Un homme fidèle dont l’engagement a contribué à unir les générations.",
                        null, null, "Figure du collectif", "/legende-1.webp"));
    }
}
