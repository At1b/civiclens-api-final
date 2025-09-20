package com.civiclens.api.service;

import com.civiclens.api.dto.HfRequest;
import com.civiclens.api.dto.HfResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AiCategorizationService {
    private final WebClient webClient;
    private final String huggingFaceApiKey;

    // We'll use a popular zero-shot classification model
    private static final String MODEL_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";

    public AiCategorizationService(WebClient.Builder webClientBuilder, @Value("${HUGGINGFACE_API_KEY}") String huggingFaceApiKey) {
        this.huggingFaceApiKey = huggingFaceApiKey;
        this.webClient = webClientBuilder.baseUrl(MODEL_URL).build();
    }

    public Mono<String> categorizeGrievance(String description, List<String> categories) {
        HfRequest.Parameters params = new HfRequest.Parameters(categories);
        HfRequest hfRequest = new HfRequest(List.of(description), params);

        return webClient.post()
                .header("Authorization", "Bearer " + huggingFaceApiKey)
                .bodyValue(hfRequest)
                .retrieve()
                .bodyToFlux(HfResponse.class) // The API returns an array, so we use Flux
                .next() // We only need the first element of the array
                .map(response -> {
                    if (response != null && !response.getLabels().isEmpty()) {
                        // The first label is the one with the highest score
                        return response.getLabels().get(0);
                    }
                    return "Uncategorized"; // Default category if AI fails
                });
    }
}
