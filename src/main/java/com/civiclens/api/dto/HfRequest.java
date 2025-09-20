package com.civiclens.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HfRequest {
    private List<String> inputs;
    private Parameters parameters;

    @Data
    @AllArgsConstructor
    public static class Parameters {
        @JsonProperty("candidate_labels")
        private List<String> candidateLabels;
    }
}
