package sn.guediawaye.backend.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ShopPageContentServiceTests {

    @Mock
    private ShopPageContentRepository repository;

    private ShopPageContentService service;

    @BeforeEach
    void setUp() {
        service = new ShopPageContentService(repository, new ObjectMapper());
    }

    @Test
    void exposesExistingShopProductsAndCategoriesAsDefaults() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ShopPageContentResponse response = service.findContent();

        assertEquals(6, response.categories().size());
        assertEquals(5, response.products().size());
    }

    @Test
    void savesShopManagerProductsAndCategories() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        ShopPageContentResponse defaults = service.findContent();
        ShopManager manager = new ShopManager(
                "shop-manager", "manager", "Awa", "Fall", "+221 77 123 45 67", "boutique@example.com");

        ShopPageContentResponse response = service.updateContent(
                new ShopPageContentRequest(manager, defaults.categories(), defaults.products()));

        assertEquals(manager, response.manager());
        assertEquals(defaults.categories(), response.categories());
        assertEquals(defaults.products(), response.products());
    }

    @Test
    void rejectsProductThatReferencesUnknownCategory() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        ShopPageContentResponse defaults = service.findContent();
        ShopProduct invalidProduct = new ShopProduct(
                "product-invalid", "products", "Produit", "unknown-category",
                "1 000 FCFA", "", "/product.png", true);

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new ShopPageContentRequest(
                        defaults.manager(), defaults.categories(), List.of(invalidProduct))));
    }

    @Test
    void rejectsCategoryDeletionWhileProductsStillUseIt() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        ShopPageContentResponse defaults = service.findContent();
        List<ShopCategory> categories = defaults.categories().stream()
                .filter(category -> !category.id().equals(defaults.products().getFirst().categoryId()))
                .toList();

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateContent(new ShopPageContentRequest(
                        defaults.manager(), categories, defaults.products())));
    }
}
