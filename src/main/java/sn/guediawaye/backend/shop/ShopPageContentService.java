package sn.guediawaye.backend.shop;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
public class ShopPageContentService {

    private static final long CONTENT_ID = 1L;
    private static final TypeReference<ShopPageContentRequest> CONTENT_TYPE = new TypeReference<>() {};

    private final ShopPageContentRepository repository;
    private final ObjectMapper objectMapper;

    public ShopPageContentService(ShopPageContentRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ShopPageContentResponse findContent() {
        return repository.findById(CONTENT_ID)
                .map(content -> toResponse(deserialize(content.getContentJson())))
                .orElseGet(() -> toResponse(defaultContent()));
    }

    public ShopPageContentResponse updateContent(ShopPageContentRequest request) {
        validateContent(request);
        ShopPageContent content = repository.findById(CONTENT_ID)
                .orElseGet(() -> new ShopPageContent(CONTENT_ID, "{}"));
        content.setContentJson(serialize(request));
        repository.save(content);
        return toResponse(request);
    }

    private void validateContent(ShopPageContentRequest content) {
        if (content == null || content.manager() == null
                || !"manager".equals(content.manager().group())
                || !"shop-manager".equals(content.manager().id())
                || content.categories() == null || content.categories().isEmpty()
                || content.products() == null) {
            throw invalidContent();
        }

        Set<String> categoryIds = new HashSet<>();
        Set<String> categoryNames = new HashSet<>();
        for (ShopCategory category : content.categories()) {
            if (category == null || isBlank(category.id()) || !categoryIds.add(category.id())
                    || !"categories".equals(category.group())
                    || isBlank(category.name()) || isBlank(category.image())
                    || !categoryNames.add(category.name().trim().toLowerCase(Locale.ROOT))) {
                throw invalidContent();
            }
        }

        Set<String> productIds = new HashSet<>();
        for (ShopProduct product : content.products()) {
            if (product == null || isBlank(product.id()) || !productIds.add(product.id())
                    || !"products".equals(product.group()) || isBlank(product.name())
                    || isBlank(product.price()) || isBlank(product.image())
                    || !categoryIds.contains(product.categoryId())) {
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
                "Les informations du gérant, les catégories ou les produits sont incomplets ou invalides.");
    }

    private ShopPageContentResponse toResponse(ShopPageContentRequest content) {
        return new ShopPageContentResponse(content.manager(), content.categories(), content.products());
    }

    private String serialize(ShopPageContentRequest content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Impossible de sérialiser le contenu de la boutique.", exception);
        }
    }

    private ShopPageContentRequest deserialize(String contentJson) {
        try {
            return objectMapper.readValue(contentJson, CONTENT_TYPE);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Le contenu de la boutique stocké en base est invalide.", exception);
        }
    }

    private ShopPageContentRequest defaultContent() {
        ShopManager manager = new ShopManager("shop-manager", "manager", "", "", "", "");
        List<ShopCategory> categories = List.of(
                new ShopCategory("category-maillots", "categories", "Maillots", "/maillot2.png"),
                new ShopCategory("category-t-shirts", "categories", "T-shirts", "/maillot1.png"),
                new ShopCategory("category-sweats", "categories", "Sweats", "/sweat.png"),
                new ShopCategory("category-accessoires", "categories", "Accessoires", "/accessoir.png"),
                new ShopCategory("category-echarpes", "categories", "Écharpes", "/maillot3.png"),
                new ShopCategory("category-autres", "categories", "Autres produits", "/accessoir.png"));
        List<ShopProduct> products = List.of(
                product("product-home-jersey", "Maillot domicile 2024/2025", "category-maillots", "25 000 FCFA", "/accessoir.png"),
                product("product-away-jersey", "Maillot extérieur 2024/2025", "category-maillots", "25 000 FCFA", "/maillot2.png"),
                product("product-polo", "Polo officiel", "category-t-shirts", "12 000 FCFA", "/maillot1.png"),
                product("product-cap", "Casquette officielle", "category-accessoires", "8 000 FCFA", "/maillot3.png"),
                product("product-scarf", "Écharpe officielle", "category-echarpes", "7 000 FCFA", "/sweat.png"));
        return new ShopPageContentRequest(manager, categories, products);
    }

    private ShopProduct product(String id, String name, String categoryId, String price, String image) {
        return new ShopProduct(id, "products", name, categoryId, price, "", image, true);
    }
}
