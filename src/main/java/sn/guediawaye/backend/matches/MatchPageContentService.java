package sn.guediawaye.backend.matches;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
public class MatchPageContentService {

    private static final long CONTENT_ID = 1L;
    private static final TypeReference<MatchPageContentRequest> CONTENT_TYPE = new TypeReference<>() {};

    private final MatchPageContentRepository repository;
    private final ObjectMapper objectMapper;

    public MatchPageContentService(MatchPageContentRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public MatchPageContentResponse findContent() {
        return repository.findById(CONTENT_ID)
                .map(content -> toResponse(deserialize(content.getContentJson())))
                .orElseGet(() -> toResponse(defaultContent()));
    }

    public MatchPageContentResponse updateContent(MatchPageContentRequest request) {
        validateContent(request);
        MatchPageContent content = repository.findById(CONTENT_ID)
                .orElseGet(() -> new MatchPageContent(CONTENT_ID, "{}"));
        content.setContentJson(serialize(request));
        repository.save(content);
        return toResponse(request);
    }

    private void validateContent(MatchPageContentRequest content) {
        if (content == null || content.opponents() == null || content.opponents().isEmpty()
                || content.matches() == null) {
            throw invalidContent();
        }

        Set<String> opponentIds = new HashSet<>();
        for (MatchOpponent opponent : content.opponents()) {
            if (opponent == null || isBlank(opponent.id()) || !opponentIds.add(opponent.id())
                    || !"opponents".equals(opponent.group())) {
                throw invalidContent();
            }
            require(opponent.name(), opponent.logo(), opponent.stadium());
        }

        Set<String> matchIds = new HashSet<>();
        long liveMatchCount = 0;
        for (MatchFixture match : content.matches()) {
            if (match == null || isBlank(match.id()) || !matchIds.add(match.id())
                    || !"matches".equals(match.group())
                    || isBlank(match.venueType()) || isBlank(match.status())
                    || !opponentIds.contains(match.opponentId())
                    || !Set.of("home", "away", "neutral").contains(match.venueType())
                    || !Set.of("scheduled", "live", "completed").contains(match.status())) {
                throw invalidContent();
            }
            require(match.date(), match.time(), match.competition());
            try {
                LocalDate.parse(match.date());
                LocalTime.parse(match.time());
            } catch (DateTimeParseException exception) {
                throw invalidContent();
            }
            if ("neutral".equals(match.venueType())) {
                require(match.neutralStadium());
            }
            if (!isBlank(match.watchUrl()) && !isValidWatchUrl(match.watchUrl())) {
                throw invalidContent();
            }
            if ("live".equals(match.status())) {
                liveMatchCount++;
            }
        }
        if (liveMatchCount > 1) {
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

    private boolean isValidWatchUrl(String value) {
        try {
            URI uri = URI.create(value);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private ResponseStatusException invalidContent() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Les équipes ou les informations des matchs sont incomplètes ou invalides.");
    }

    private MatchPageContentResponse toResponse(MatchPageContentRequest content) {
        return new MatchPageContentResponse(content.opponents(), content.matches());
    }

    private String serialize(MatchPageContentRequest content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Impossible de sérialiser le calendrier des matchs.", exception);
        }
    }

    private MatchPageContentRequest deserialize(String contentJson) {
        try {
            return objectMapper.readValue(contentJson, CONTENT_TYPE);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Le calendrier stocké en base est invalide.", exception);
        }
    }

    private MatchPageContentRequest defaultContent() {
        List<MatchOpponent> opponents = List.of(
                new MatchOpponent("opponent-jaaraf", "opponents", "ASC Jaaraf", "/jaraaf-logo.png", "Stade Demba Diop"),
                new MatchOpponent("opponent-goree", "opponents", "US Gorée", "/bg-match.jpeg", "Stade de Ngor"),
                new MatchOpponent("opponent-teungueth", "opponents", "Teungueth FC", "/bg_equipe.jpeg", "Stade Ngalandou Diouf"),
                new MatchOpponent("opponent-diambars", "opponents", "Diambars FC", "/actu_1.jpeg", "Stade Fodé Wade"),
                new MatchOpponent("opponent-pikine", "opponents", "AS Pikine", "/actu_2.webp", "Stade Alassane Djigo"),
                new MatchOpponent("opponent-generation-foot", "opponents", "Génération Foot", "/hero-background-2.png", "Stade Lat Dior"),
                new MatchOpponent("opponent-casa-sports", "opponents", "Casa Sports", "/actu_3.png", "Stade Aline Sitoé Diatta"),
                new MatchOpponent("opponent-dakar-sc", "opponents", "Dakar Sacré-Cœur", "/histoire-image.png", "Stade de la Cité des Parcelles Assainies"));

        List<MatchFixture> matches = List.of(
                match("match-2025-10-25-jaaraf", "2025-10-25", "opponent-jaaraf", "home", "1", "1"),
                match("match-2025-11-02-goree", "2025-11-02", "opponent-goree", "away", "", ""),
                match("match-2025-11-08-teungueth", "2025-11-08", "opponent-teungueth", "away", "", ""),
                match("match-2025-11-15-diambars", "2025-11-15", "opponent-diambars", "home", "", ""),
                match("match-2025-11-22-pikine", "2025-11-22", "opponent-pikine", "away", "", ""),
                match("match-2025-10-18-generation-foot", "2025-10-18", "opponent-generation-foot", "away", "0", "2"),
                match("match-2025-10-12-casa-sports", "2025-10-12", "opponent-casa-sports", "home", "1", "0"),
                match("match-2025-10-05-goree", "2025-10-05", "opponent-goree", "away", "1", "1"),
                match("match-2025-09-28-dakar-sc", "2025-09-28", "opponent-dakar-sc", "home", "3", "1"));
        return new MatchPageContentRequest(opponents, matches);
    }

    private MatchFixture match(String id, String date, String opponentId, String venueType, String homeScore, String awayScore) {
        return new MatchFixture(
                id, "matches", date, "16:00", opponentId, venueType, "",
                "Championnat National", "completed", homeScore, awayScore, "");
    }
}
