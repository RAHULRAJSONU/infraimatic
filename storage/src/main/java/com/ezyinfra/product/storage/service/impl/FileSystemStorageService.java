package com.ezyinfra.product.storage.service.impl;

import com.ezyinfra.product.storage.model.StorageResult;
import com.ezyinfra.product.storage.service.StorageService;
import com.ezyinfra.product.storage.util.FluxToInputStream;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(prefix = "storage.fs", name = "enabled", havingValue = "true")
public class FileSystemStorageService implements StorageService {

    private final Path rootDir;
    private final MeterRegistry meter;
    private final int queueCapacity;
    private final DataBufferFactory dataBufferFactory;

    public FileSystemStorageService(
            @Value("${storage.fs.root-dir}") String rootDir,
            @Value("${storage.fs.queue-capacity:32}") int queueCapacity,
            MeterRegistry meter
    ) {
        this.rootDir = Paths.get(rootDir).toAbsolutePath().normalize();
        this.queueCapacity = Math.max(4, queueCapacity);
        this.meter = meter;
        this.dataBufferFactory = new DefaultDataBufferFactory();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create storage root directory: " + rootDir, e);
        }
    }

    @Override
    public StorageResult upload(Flux<DataBuffer> source,
                                String targetPath,
                                String contentType,
                                long contentLength,
                                Map<String, String> metadata) {

        long startTime = System.nanoTime();

        String relativePath = (targetPath == null || targetPath.isBlank())
                ? UUID.randomUUID().toString()
                : targetPath.replace("\\", "/");

        Path finalPath = resolveSafe(rootDir, relativePath);

        try {
            Path parent = finalPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String tempFileName = finalPath.getFileName().toString()
                    + "." + UUID.randomUUID() + ".tmp";

            Path tempPath = (finalPath.getParent() != null
                    ? finalPath.getParent()
                    : rootDir
            ).resolve(tempFileName);

            long bytesWritten;
            try (InputStream in = FluxToInputStream.fromFlux(source, queueCapacity);
                 OutputStream out = Files.newOutputStream(
                         tempPath,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING,
                         StandardOpenOption.WRITE)) {

                bytesWritten = in.transferTo(out);
            }

            try {
                Files.move(tempPath, finalPath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
            }

            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
            meter.timer("storage.upload.duration", "backend", "fs")
                    .record(elapsedMs, TimeUnit.MILLISECONDS);
            meter.counter("storage.upload.success", "backend", "fs").increment();

            String id = rootDir.relativize(finalPath).toString()
                    .replace(File.separatorChar, '/');

            long size = contentLength > 0 ? contentLength : bytesWritten;

            return new StorageResult(
                    id,
                    id, // logical location; no public URL
                    size,
                    contentType,
                    metadata != null ? metadata : Map.of()
            );
        } catch (IOException e) {
            meter.counter("storage.upload.failure", "backend", "fs").increment();
            throw new UncheckedIOException("Failed to store file to filesystem", e);
        }
    }

    @Override
    public Optional<String> generatePresignedUrl(String id, long expiresSeconds) {
        return Optional.empty();
    }

    @Override
    public void delete(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        try {
            Path path = resolveSafe(rootDir, id.replace("\\", "/"));
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // log if needed
        }
    }

    @Override
    public Optional<String> getPublicUrl(String id) {
        return Optional.empty();
    }

    // ================== Retrieval helpers ==================

    public InputStream openStream(String id) throws IOException {
        Path path = resolveSafe(rootDir, id.replace("\\", "/"));
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    public byte[] readAllBytes(String id) throws IOException {
        Path path = resolveSafe(rootDir, id.replace("\\", "/"));
        return Files.readAllBytes(path);
    }

    public Path getPath(String id) {
        return resolveSafe(rootDir, id.replace("\\", "/"));
    }

    /**
     * Reactive download as Flux<DataBuffer>.
     * Uses DataBufferUtils.read to stream from file without loading it entirely.
     */
    public Flux<DataBuffer> downloadAsFlux(String id) {
        Path path = resolveSafe(rootDir, id.replace("\\", "/"));
        if (!Files.exists(path)) {
            return Flux.error(new NoSuchFileException(path.toString()));
        }
        // bufferSize can be tuned (e.g. 4KB, 16KB, 64KB)
        int bufferSize = 16 * 1024;
        return DataBufferUtils.read(path, dataBufferFactory, bufferSize);
    }

    // ================== internal helper ==================

    private Path resolveSafe(Path root, String subPath) {
        Path resolved = root.resolve(subPath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path, outside of storage root: " + subPath);
        }
        return resolved;
    }
}
