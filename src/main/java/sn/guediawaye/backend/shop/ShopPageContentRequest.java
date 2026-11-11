package sn.guediawaye.backend.shop;

import java.util.List;

public record ShopPageContentRequest(
        ShopManager manager,
        List<ShopCategory> categories,
        List<ShopProduct> products) {
}
