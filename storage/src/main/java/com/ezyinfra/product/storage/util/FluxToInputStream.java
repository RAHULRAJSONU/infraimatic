package com.ezyinfra.product.storage.util;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adapter: converts a Flux<DataBuffer> into a blocking InputStream while respecting backpressure.
 *
 * - Copies each DataBuffer into a byte[] (avoids retaining pooled buffers).
 * - Uses a bounded ArrayBlockingQueue to provide backpressure to the upstream.
 * - Caller MUST close the returned InputStream to cancel the subscription and free resources.
 */
public final class FluxToInputStream {

    private static final DataChunk POISON_PILL = new DataChunk(new byte[0], false, null, true);

    private FluxToInputStream() {}

    /**
     * Create an InputStream adapter backed by the provided Flux<DataBuffer>.
     *
     * @param flux          source flux
     * @param queueCapacity capacity of the internal queue (tune for your workload)
     * @return InputStream that reads from the flux (blocking)
     */
    public static InputStream fromFlux(Flux<DataBuffer> flux, int queueCapacity) {
        final ArrayBlockingQueue<DataChunk> queue = new ArrayBlockingQueue<>(Math.max(8, queueCapacity));
        final AtomicBoolean closed = new AtomicBoolean(false);

        // Subscribe to the flux and copy DataBuffer contents into byte[] wrappers.
        Disposable sub = flux.subscribe(db -> {
            try {
                int len = db.readableByteCount();
                if (len <= 0) {
                    // still release buffer if pooled (defensive)
                    DataBufferUtils.release(db);
                    return;
                }
                byte[] bytes = new byte[len];
                // copy bytes
                db.read(bytes, 0, len);
                // release DataBuffer (important for pooled implementations)
                DataBufferUtils.release(db);
                // put into queue (blocks if queue full)
                queue.put(new DataChunk(bytes, false, null, false));
            } catch (Throwable t) {
                // push an error marker
                try {
                    queue.put(new DataChunk(null, true, t, false));
                } catch (InterruptedException ignore) {
                    Thread.currentThread().interrupt();
                }
            }
        }, err -> {
            try {
                queue.put(new DataChunk(null, true, err, false));
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }, () -> {
            try {
                queue.put(POISON_PILL);
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        });

        return new InputStream() {
            private byte[] current = null;
            private int pos = 0;
            private boolean finished = false;

            private DataChunk take() throws IOException {
                if (finished) return POISON_PILL;
                try {
                    DataChunk ch = queue.take();
                    if (ch.isError) {
                        // Cancel subscription on upstream error
                        sub.dispose();
                        throw new IOException("Upstream error", ch.error);
                    }
                    return ch;
                } catch (InterruptedException e) {
                    sub.dispose();
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted waiting for data", e);
                }
            }

            private void ensureCurrent() throws IOException {
                if (current != null && pos < current.length) return;
                if (finished) return;
                DataChunk ch = take();
                if (ch == POISON_PILL) {
                    finished = true;
                    current = null;
                    pos = 0;
                    return;
                }
                current = ch.bytes;
                pos = 0;
            }

            @Override
            public int read() throws IOException {
                ensureCurrent();
                if (finished) return -1;
                return current[pos++] & 0xff;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                ensureCurrent();
                if (finished) return -1;
                int avail = current.length - pos;
                int toCopy = Math.min(len, avail);
                System.arraycopy(current, pos, b, off, toCopy);
                pos += toCopy;
                return toCopy;
            }

            @Override
            public void close() throws IOException {
                super.close();
                if (closed.compareAndSet(false, true)) {
                    try {
                        sub.dispose();
                    } catch (Exception ignored) {}
                    queue.clear();
                }
            }
        };
    }

    private static final class DataChunk {
        final byte[] bytes;
        final boolean isError;
        final Throwable error;
        final boolean isPoison;

        DataChunk(byte[] bytes, boolean isError, Throwable error, boolean isPoison) {
            this.bytes = bytes;
            this.isError = isError;
            this.error = error;
            this.isPoison = isPoison;
        }
    }
}