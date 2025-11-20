package com.ezyinfra.product.storage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Result returned by StorageService implementations after a successful upload.
 *
 * Immutable and serializable. Use the {@link Builder} to create instances.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class StorageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;         // backend id (object key / drive fileId / path)
    private final String url;        // public/view url if available
    private final long sizeBytes;    // -1 if unknown
    private final String contentType; // MIME type, may be null
    private final Map<String, String> metadata; // optional metadata returned by backend

    @JsonCreator
    public StorageResult(
            @JsonProperty("id") String id,
            @JsonProperty("url") String url,
            @JsonProperty("sizeBytes") long sizeBytes,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("metadata") Map<String, String> metadata) {
        this.id = id;
        this.url = url;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    /** Convenience factory when metadata not required */
    public static StorageResult of(String id, String url, long sizeBytes, String contentType) {
        return new Builder()
                .id(id)
                .url(url)
                .sizeBytes(sizeBytes)
                .contentType(contentType)
                .build();
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Size in bytes. If unknown, returns -1.
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * Immutable metadata map (may be empty).
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Convert result to a Map useful for logging/metrics/DLQ payloads.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("url", url);
        m.put("sizeBytes", sizeBytes);
        m.put("contentType", contentType);
        m.put("metadata", metadata);
        return m;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageResult that = (StorageResult) o;
        return sizeBytes == that.sizeBytes &&
                Objects.equals(id, that.id) &&
                Objects.equals(url, that.url) &&
                Objects.equals(contentType, that.contentType) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, sizeBytes, contentType, metadata);
    }

    @Override
    public String toString() {
        return "StorageResult{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", sizeBytes=" + sizeBytes +
                ", contentType='" + contentType + '\'' +
                ", metadata=" + metadata +
                '}';
    }

    /** Builder for StorageResult */
    public static final class Builder {
        private String id;
        private String url;
        private long sizeBytes = -1L;
        private String contentType;
        private Map<String, String> metadata;

        public Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder sizeBytes(long sizeBytes) {
            this.sizeBytes = sizeBytes;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public StorageResult build() {
            return new StorageResult(id, url, sizeBytes, contentType, metadata);
        }
    }
}