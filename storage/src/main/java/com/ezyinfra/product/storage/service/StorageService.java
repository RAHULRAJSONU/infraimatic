package com.ezyinfra.product.storage.service;

import com.ezyinfra.product.storage.model.StorageResult;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;
import java.io.InputStream;

public interface StorageService {
    /**
     * Upload using a reactive stream. Implementations MUST NOT buffer the entire content.
     * Implementations may choose to consume the InputStream directly (via FluxToInputStream)
     * or consume the Flux<DataBuffer>.
     *
     * @param source             reactive stream of DataBuffer
     * @param targetPath         backend logical path / filename
     * @param contentType        mime-type if known
     * @param contentLength      if known, pass; -1 if unknown
     * @param metadata           optional metadata
     * @return StorageResult with id/url/size
     */
    StorageResult upload(Flux<DataBuffer> source, String targetPath, String contentType, long contentLength, Map<String,String> metadata);

    default Optional<String> generatePresignedUrl(String id, long expiresSeconds) { return Optional.empty(); }

    void delete(String id);

    Optional<String> getPublicUrl(String id);
}