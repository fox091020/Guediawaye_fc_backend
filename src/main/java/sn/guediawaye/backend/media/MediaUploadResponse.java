package sn.guediawaye.backend.media;

public record MediaUploadResponse(String url, String fileName, String contentType, long size) {
}
