package com.ezyinfra.product.notification.whatsapp.service;

import com.ezyinfra.product.storage.model.StorageResult;
import com.ezyinfra.product.storage.service.StorageService;
import com.ezyinfra.product.storage.util.FluxToInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Non-blocking MediaService:
 * - returns Mono<StorageResult>
 * - will use FluxToInputStream -> InputStream for blocking storage implementations
 * - runs blocking calls on Schedulers.boundedElastic()
 */
@Slf4j
@Service
public class MediaService {

    private final WebClient webClient;
    private final StorageService storageService;
    private final int queueCapacity;

    public MediaService(WebClient.Builder webClientBuilder,
                        StorageService storageService,
                        // queue capacity for FluxToInputStream; tune to your workload
                        @org.springframework.beans.factory.annotation.Value("${media.queue-capacity:32}") int queueCapacity) {
        this.webClient = webClientBuilder.build();
        this.storageService = storageService;
        this.queueCapacity = Math.max(4, queueCapacity);
    }

    /**
     * Download media from mediaUrl and upload to storage in a fully non-blocking manner (returns a Mono).
     *
     * It will pick the blocking InputStream-based upload if available on the StorageService; otherwise it will
     * call the (Flux<DataBuffer>) upload if present.
     *
     * @param mediaUrl remote media URL (e.g. Twilio media URL)
     * @param contentType optional content-type (may be null)
     * @return Mono of StorageResult
     */
    public Mono<StorageResult> downloadAndUpload(String mediaUrl, String contentType) {
        String key = "media/" + UUID.randomUUID();

        // Step 1 — obtain response (headers + reactive body)
        Mono<ClientResponse> responseMono = webClient.get()
                .uri(mediaUrl)
                .exchangeToMono(Mono::just)
                .timeout(Duration.ofSeconds(60)); // configurable timeout for initial handshake

        return responseMono.flatMap(response -> {
            if (!response.statusCode().is2xxSuccessful()) {
                // read body for error diagnostics (bounded)
                return response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(body -> Mono.<StorageResult>error(new RuntimeException(
                                "Failed to download media: status=" + response.statusCode() + ", body=" + body)));
            }

            // Extract content-length header if present
            long contentLength = response.headers().contentLength().orElse(-1L);

            // Reactive bodyFlux
            Flux<DataBuffer> bodyFlux = response.bodyToFlux(DataBuffer.class);

            // Decide whether to use blocking InputStream-based upload or Flux-based upload.
            Optional<Method> blockingUploadMethod = findBlockingUploadMethod();

            if (blockingUploadMethod.isPresent()) {
                Method m = blockingUploadMethod.get();
                // Use FluxToInputStream and call blocking upload on boundedElastic
                return Mono.using(
                                // resource supplier: create InputStream adapter (this subscribes to the Flux)
                                () -> FluxToInputStream.fromFlux(bodyFlux, queueCapacity),
                                // using: perform blocking upload on boundedElastic scheduler
                                (InputStream is) -> Mono.fromCallable(() -> {
                                    try {
                                        // invoke the blocking upload: upload(InputStream, String, String, long, Map)
                                        Object res = m.invoke(storageService, is, key, contentType, contentLength, Map.<String,String>of());
                                        if (res instanceof StorageResult) {
                                            return (StorageResult) res;
                                        } else {
                                            throw new RuntimeException("Blocking upload method returned unexpected type: " + (res == null ? "null" : res.getClass()));
                                        }
                                    } catch (RuntimeException re) {
                                        throw re;
                                    } catch (Exception ex) {
                                        throw new RuntimeException("Blocking storage upload failed", ex);
                                    }
                                }).subscribeOn(Schedulers.boundedElastic()),
                                // cleanup: close the InputStream (also cancels underlying Flux subscription)
                                is -> {
                                    try { is.close(); } catch (Exception e) { log.debug("Failed closing stream", e); }
                                }
                        ).doOnSubscribe(s -> log.debug("Starting blocking upload (via InputStream) to key={} contentLength={}", key, contentLength))
                        .doOnSuccess(r -> log.info("Completed blocking upload key={} result={}", key, r))
                        .doOnError(err -> log.error("Blocking upload failed for key={}", key, err));
            } else {
                // No blocking upload method — assume StorageService.upload(Flux<DataBuffer>, ..) exists.
                // Call it on boundedElastic to be safe (some implementations may block).
                return Mono.fromCallable(() -> {
                            try {
                                // The StorageService.upload(Flux<DataBuffer>, ...) signature we expect:
                                // StorageResult upload(Flux<DataBuffer> source, String targetPath, String contentType, long contentLength, Map<String,String> metadata)
                                Method reactiveUpload = storageService.getClass().getMethod("upload", Flux.class, String.class, String.class, long.class, Map.class);
                                Object res = reactiveUpload.invoke(storageService, bodyFlux, key, contentType, contentLength, Map.<String,String>of());
                                if (res instanceof StorageResult) return (StorageResult) res;
                                throw new RuntimeException("Reactive upload returned unexpected type");
                            } catch (NoSuchMethodException nsme) {
                                // Defensive: storage service doesn't expose reactive upload signature as expected
                                throw new RuntimeException("StorageService does not expose a compatible upload method", nsme);
                            } catch (RuntimeException re) {
                                throw re;
                            } catch (Exception ex) {
                                throw new RuntimeException("Reactive storage upload failed", ex);
                            }
                        }).subscribeOn(Schedulers.boundedElastic())
                        .doOnSubscribe(s -> log.debug("Starting reactive upload (Flux) to key={} contentLength={}", key, contentLength))
                        .doOnSuccess(r -> log.info("Completed reactive upload key={} result={}", key, r))
                        .doOnError(err -> log.error("Reactive upload failed for key={}", key, err));
            }
        });
    }

    /**
     * Attempts to locate a blocking upload method with the signature:
     * upload(InputStream, String, String, long, Map<String,String>)
     *
     * @return Optional of Method if found
     */
    private Optional<Method> findBlockingUploadMethod() {
        try {
            Method m = storageService.getClass().getMethod("upload", InputStream.class, String.class, String.class, long.class, Map.class);
            return Optional.of(m);
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}