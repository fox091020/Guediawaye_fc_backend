package sn.guediawaye.backend.team;

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
public class TeamPageContentService {

    private static final long CONTENT_ID = 1L;
    private static final TypeReference<List<TeamPageItem>> ITEM_LIST_TYPE = new TypeReference<>() {};

    private final TeamPageContentRepository repository;
    private final ObjectMapper objectMapper;

    public TeamPageContentService(TeamPageContentRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public TeamPageContentResponse findContent() {
        return repository.findById(CONTENT_ID)
                .map(content -> new TeamPageContentResponse(deserialize(content.getItemsJson())))
                .orElseGet(() -> new TeamPageContentResponse(defaultItems()));
    }

    public TeamPageContentResponse updateContent(TeamPageContentRequest request) {
        validateItems(request.items());
        TeamPageContent content = repository.findById(CONTENT_ID)
                .orElseGet(() -> new TeamPageContent(CONTENT_ID, "[]"));
        content.setItemsJson(serialize(request.items()));
        repository.save(content);
        return new TeamPageContentResponse(request.items());
    }

    private void validateItems(List<TeamPageItem> items) {
        Set<String> ids = new HashSet<>();
        long groupCount = 0;
        for (TeamPageItem item : items) {
            if (item == null || isBlank(item.id()) || isBlank(item.group()) || !ids.add(item.id())) {
                throw invalidContent();
            }
            switch (item.group()) {
                case "group" -> {
                    groupCount++;
                    require(item.title(), item.description(), item.image(), item.playerCount(),
                            item.technicalStaffCount(), item.medicalStaffCount(), item.managementCount());
                }
                case "players" -> require(item.number(), item.name(), item.role(), item.image());
                case "staff" -> require(item.name(), item.gender(), item.role(), item.image());
                default -> throw invalidContent();
            }
        }
        if (groupCount != 1) {
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
                "Les données de la page équipe et staff sont incomplètes ou invalides.");
    }

    private String serialize(List<TeamPageItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Impossible de sérialiser le contenu de la page équipe.", exception);
        }
    }

    private List<TeamPageItem> deserialize(String itemsJson) {
        try {
            return objectMapper.readValue(itemsJson, ITEM_LIST_TYPE);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Le contenu de la page équipe stocké en base est invalide.", exception);
        }
    }

    private List<TeamPageItem> defaultItems() {
        return List.of(
                new TeamPageItem("team-group", "group", "UN COLLECTIF\nDE TALENTS",
                        "Un effectif composé de jeunes espoirs et de joueurs expérimentés, pour un équilibre parfait sur le terrain.",
                        "/hero-club.jpg", "25", "10", "3", "5", null, null, null, null),
                new TeamPageItem("player-1", "players", null, null, "/img_joueur.jpg", null, null, null, null, "01", "S. DIAGNE", "Gardien de but", null),
                new TeamPageItem("player-2", "players", null, null, "/img_joueur.jpg", null, null, null, null, "02", "M. FALL", "Défenseur central", null),
                new TeamPageItem("player-3", "players", null, null, "/img_joueur.jpg", null, null, null, null, "03", "A. DIALLO", "Défenseur central", null),
                new TeamPageItem("player-4", "players", null, null, "/img_joueur.jpg", null, null, null, null, "04", "I. SARR", "Latéral droit", null),
                new TeamPageItem("player-5", "players", null, null, "/img_joueur.jpg", null, null, null, null, "05", "M. TALL", "Latéral gauche", null),
                new TeamPageItem("player-6", "players", null, null, "/img_joueur.jpg", null, null, null, null, "06", "P. NDIAYE", "Milieu défensif", null),
                new TeamPageItem("player-8", "players", null, null, "/img_joueur.jpg", null, null, null, null, "08", "L. GAYE", "Milieu relayeur", null),
                new TeamPageItem("player-10", "players", null, null, "/img_joueur.jpg", null, null, null, null, "10", "B. DIOP", "Milieu offensif", null),
                new TeamPageItem("player-11", "players", null, null, "/img_joueur.jpg", null, null, null, null, "11", "A. MBAYE", "Ailier droit", null),
                new TeamPageItem("player-12", "players", null, null, "/img_joueur.jpg", null, null, null, null, "12", "S. NDIAYE", "Ailier gauche", null),
                new TeamPageItem("player-9", "players", null, null, "/img_joueur.jpg", null, null, null, null, "09", "M. SOW", "Attaquant", null),
                new TeamPageItem("player-7", "players", null, null, "/img_joueur.jpg", null, null, null, null, "07", "K. FAYE", "Attaquant", null),
                new TeamPageItem("staff-1", "staff", null, null, "/legende-1.webp", null, null, null, null, null, "G. DIOP", "Entraîneur principal", "Homme"),
                new TeamPageItem("staff-2", "staff", null, null, "/legende-2.webp", null, null, null, null, null, "A. NDIAYE", "Entraîneur adjoint", "Homme"),
                new TeamPageItem("staff-3", "staff", null, null, "/legende-3.webp", null, null, null, null, null, "L. DIALLO", "Préparateur physique", "Homme"),
                new TeamPageItem("staff-4", "staff", null, null, "/legende-4.webp", null, null, null, null, null, "S. GUEYE", "Entraîneur des gardiens", "Homme"),
                new TeamPageItem("staff-5", "staff", null, null, "/legende-1.webp", null, null, null, null, null, "D. SOW", "Analyste vidéo", "Homme"),
                new TeamPageItem("staff-6", "staff", null, null, "/legende-2.webp", null, null, null, null, null, "B. FALL", "Médecin", "Homme"),
                new TeamPageItem("staff-7", "staff", null, null, "/legende-3.webp", null, null, null, null, null, "M. DIALLO", "Kinésithérapeute", "Homme"),
                new TeamPageItem("staff-8", "staff", null, null, "/legende-4.webp", null, null, null, null, null, "A. TALL", "Team manager", "Homme"));
    }
}
