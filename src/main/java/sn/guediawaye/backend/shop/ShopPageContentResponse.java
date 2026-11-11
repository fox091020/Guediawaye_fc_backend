package sn.guediawaye.backend.shop;

import java.util.List;

public record ShopPageContentResponse(
        ShopManager manager,
        List<ShopCategory> categories,
        List<ShopProduct> products) {
}
