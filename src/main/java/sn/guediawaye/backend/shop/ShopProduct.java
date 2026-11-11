package sn.guediawaye.backend.shop;

public record ShopProduct(
        String id,
        String group,
        String name,
        String categoryId,
        String price,
        String description,
        String image,
        boolean published) {
}
