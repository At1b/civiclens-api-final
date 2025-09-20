package com.civiclens.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore fields we don't need
public class HfResponse {
    private String sequence;
    private List<String> labels;
    private List<Double> scores;
}
