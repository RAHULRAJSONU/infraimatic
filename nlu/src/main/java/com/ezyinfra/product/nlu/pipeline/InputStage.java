package com.ezyinfra.product.nlu.pipeline;

import com.ezyinfra.product.nlu.service.AudioTranscriptionService;
import com.ezyinfra.product.notification.whatsapp.service.MediaService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class InputStage implements PipelineStage {

    private final MediaService mediaService;
    private final AudioTranscriptionService transcriptionService;

    public InputStage(MediaService mediaService, AudioTranscriptionService transcriptionService) {
        this.mediaService = mediaService;
        this.transcriptionService = transcriptionService;
    }

    @Override
    public String name() {
        return "input-stage";
    }

    @Override
    public Mono<StageResult> process(PipelineContext ctx) {

        String body = ctx.getEvent().getOrDefault("Body", "").trim();
        if (!body.isBlank()) {
            ctx.setText(normalize(body));
            return Mono.just(StageResult.next(ctx));
        }

        int numMedia = Integer.parseInt(ctx.getEvent().getOrDefault("NumMedia", "0"));
        if (numMedia == 0) {
            ctx.setResponse("Please send text or a voice message.");
            return Mono.just(StageResult.stop(ctx));
        }

        String mediaUrl = ctx.getEvent().get("MediaUrl0");
        String contentType = ctx.getEvent().get("MediaContentType0");

        if (mediaUrl == null || !contentType.startsWith("audio")) {
            ctx.setResponse("Unsupported media type.");
            return Mono.just(StageResult.stop(ctx));
        }

        return mediaService.downloadAndUpload(mediaUrl, contentType)
                .flatMap(media -> {
                    ctx.setMedia(media);
                    return Mono.fromCallable(() ->
                            transcriptionService
                                    .transcribe(media, contentType)
                                    .orElse("")
                    ).subscribeOn(Schedulers.boundedElastic());
                })
                .map(this::normalize)
                .map(text -> {
                    if (text.isBlank()) {
                        ctx.setResponse("🎧 I couldn't understand the audio. Please try again.");
                        return StageResult.stop(ctx);
                    }
                    ctx.setText(text);
                    return StageResult.next(ctx);
                });
    }

    private String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("\\b(uh|um|er|ah)\\b", "")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
