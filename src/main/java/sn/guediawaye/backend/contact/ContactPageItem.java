package sn.guediawaye.backend.contact;

public record ContactPageItem(
        String id,
        String group,
        String phone,
        String phoneDetails,
        String email,
        String emailDetails,
        String address,
        String addressDetails,
        String mapUrl,
        String question,
        String answer) {
}
